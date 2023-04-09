package practice.gy.gyservertask

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.text.SimpleDateFormat
import java.util.{Date, Properties}
import scala.collection.mutable

object Task2 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val redisConf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("ChangeRecord", new SimpleStringSchema(), kafkaProp))
    kafkaStream
      .map(data => {
        val strings = data.split(",")
        (strings(1),strings(3),strings(4),strings(5))
      }).assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps()
    .withTimestampAssigner(new SerializableTimestampAssigner[(String,String,String,String)] {
      override def extractTimestamp(t: (String, String, String, String), l: Long): Long = {
        val format = new SimpleDateFormat("yy-mm-dd hh:mm:ss")
        val time = format.parse(t._3).getTime
        time/1000
      }
    }))
      .filter(_._2 == "待机")
      .map(data => {
        (data._1,data._2,data._3,data._4,{
          val format = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
          ((format.parse(data._4).getTime - format.parse(data._3).getTime)/30000).toInt
        })
      })
      .filter(_._5 <= 3)
      .process(new ProcessFunction[(String,String,String,String,Int),String] {
        override def processElement(i: (String, String, String, String, Int), context: ProcessFunction[(String, String, String, String, Int), String]#Context, collector: Collector[String]): Unit = {
          var idx = 0
          for (idx <- 0 to i._5-1){
            val result = new StringBuilder()
            result.append(i._1 + ",")
            result.append(i._3 + ":" + "设备" + i._1 + " " + "连续30秒为预警状态请尽快处理！")
            collector.collect(result.toString())
          }
        }
      })
      .print()
//      .addSink(new RedisSink(redisConf,new MyRedis()))
    env.execute()
  }
  class MyRedis() extends RedisMapper[String] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.HSET,"warning30sMachine")

    override def getKeyFromData(t: String): String = t.split(",")(0)

    override def getValueFromData(t: String): String = t.split(",")(1)
  }
}
