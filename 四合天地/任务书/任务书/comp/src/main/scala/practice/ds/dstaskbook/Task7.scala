package practice.ds.dstaskbook

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Properties

object Task7 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers", "101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset", "latest")
    val redisConf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("order1", new SimpleStringSchema(), kafkaProp))
    kafkaStream
      .map(data => {
        val strings = data.split(",")
        (strings(1), strings(3).toInt, strings(4), strings(10), strings(11))
      }).assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5))
      .withTimestampAssigner(new SerializableTimestampAssigner[(String, Int, String, String, String)] {
        override def extractTimestamp(t: (String, Int, String, String, String), l: Long): Long = {
          val simpleFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
          if (t._4 == "") {
            simpleFormat.parse(t._3).getTime
          } else if (t._3 >= t._4) {
            simpleFormat.parse(t._3).getTime
          } else {
            simpleFormat.parse(t._4).getTime
          }
        }
      })).filter(_._3 != "1003")
      .filter(_._3 != "1005")
      .filter(_._3 != "1006")
      .keyBy(_._1)
      .window(SlidingEventTimeWindows.of(Time.seconds(60), Time.seconds(10)))
      .sum(1)
      .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .process(new ProcessAllWindowFunction[(String, Int, String, String, String), String, TimeWindow] {
        override def process(context: Context, elements: Iterable[(String, Int, String, String, String)], out: Collector[String]): Unit = {
          var list = elements.toSeq
          val list1 = list.sortBy(-_._2).toList
          var result = new StringBuilder()
          var i = 0
          if (list1.length >= 2) {
            for (i <- 0 to 1) {
              if (i != 1) {
                result.append(list1(i)._1 + ":" + list1(i)._2 + ",")
              } else {
                result.append(list1(i)._1 + ":" + list1(i)._2)
              }
            }
            out.collect("[" + result.toString() + "]")
          }
        }
      }).print()
//      .addSink(new RedisSink(redisConf,new MyRedis()))

    env.execute()

  }
  class MyRedis() extends RedisMapper[(String)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: String): String = "top2userconsumption"

    override def getValueFromData(t: String): String = t
  }
}
