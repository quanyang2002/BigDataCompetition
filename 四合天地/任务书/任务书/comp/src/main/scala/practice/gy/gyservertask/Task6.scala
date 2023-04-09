package practice.gy.gyservertask

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.text.SimpleDateFormat
import java.util.{Date, Properties}
import scala.collection.mutable

object Task6 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    val RedisConf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("EnvironmentData", new SimpleStringSchema(), kafkaProp))
    kafkaStream
      .map(data => {
        val strings = data.split(",")
        (strings(1),strings(5).toDouble)
      })
      .keyBy(_._1)
      .windowAll(TumblingProcessingTimeWindows.of(Time.minutes(3)))
      .process(new ProcessAllWindowFunction[(String,Double),String,TimeWindow] {
        override def process(context: Context, elements: Iterable[(String, Double)], out: Collector[String]): Unit = {
          var flag = 0
          var i = 0
          val simpleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
          val list = elements.toList
          for (i <- 0 to list.length-1){
            if (list(i)._2 <= 37){
              flag = 1
            }
          }
          val str = simpleFormat.format(new Date)
          val result = new StringBuilder()
          if (flag == 0){
            result.append(list(0)._1 + "-" + str + "，设备" + list(0)._1 + "连续三分钟温度高于38度请及时处理！")
            out.collect(result.toString())
          }
        }
      })
//      .addSink(new RedisSink(RedisConf,new MyRedis()))
      .print()


    env.execute()
  }

  class MyRedis() extends RedisMapper[(String)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.HSET,"env_temperature_monitor")

    override def getKeyFromData(t: String): String = t.split("，")(0)

    override def getValueFromData(t: String): String = t.split("，")(1)
  }

}
