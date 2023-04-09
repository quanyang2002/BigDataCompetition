package practice.ds.dstaskbook

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, TumblingEventTimeWindows, TumblingProcessingTimeWindows}
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

object Task8 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val config = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val sourceStream = env.addSource(new FlinkKafkaConsumer[(String)]("detail", new SimpleStringSchema(), kafkaProp))
    sourceStream
      .map(data => {
        val strings = data.split(",")
        (strings(3).split(" ")(0),strings(5).toInt*strings(6).toInt,strings(7))
      }).assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5))
    .withTimestampAssigner(new SerializableTimestampAssigner[(String,Int,String)] {
      override def extractTimestamp(t: (String, Int, String), l: Long): Long = {
        val simpleFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val date = simpleFormat.parse(t._3).getTime
        date
      }
    }))
      .keyBy(_._1)
      .window(SlidingEventTimeWindows.of(Time.seconds(60),Time.seconds(10)))
      .sum(1)
      .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .process(new ProcessAllWindowFunction[(String,Int,String),(String),TimeWindow] {
        override def process(context: Context, elements: Iterable[(String, Int, String)], out: Collector[String]): Unit = {
          val list = elements.toSeq.sortBy(-_._2).toList
          var result = new StringBuilder()
          if (list.length >= 3){
            var i = 0
            for (i <- 0 to 2){
              if (i!=2){
                result.append(list(i)._1 + ":" + list(i)._2 + ",")
              }else{
                result.append(list(i)._1 + ":" + list(i)._2)
              }
            }
          }
          out.collect("[" + result.toString() + "]")
        }
      })
//      .addSink(new RedisSink(config,new MyRedis()))
      .print()

    env.execute()
  }
  class MyRedis() extends RedisMapper[(String)] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: String): String = "top3itemconsumption"

    override def getValueFromData(t: String): String = t
  }

}
