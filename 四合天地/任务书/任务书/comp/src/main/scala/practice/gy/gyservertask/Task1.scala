package practice.gy.gyservertask

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Properties

object Task1 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val kafkaProp =  new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    val redisConf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val sourceStream = env.addSource(new FlinkKafkaConsumer[(String)]("ProduceRecord", new SimpleStringSchema(), kafkaProp))
    // 假设0标志代表已经检验的设备
    sourceStream.filter(_.split(",")(9) == "0")
      .map(data => {
        val strings = data.split(",")
        val format = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
        val time = format.parse(strings(4)).getTime
        (strings(1),strings(0),time,1)
      })
//      .assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps()
//    .withTimestampAssigner(new SerializableTimestampAssigner[(String,String,Long,Int)] {
//      override def extractTimestamp(t: (String, String, Long,Int), l: Long): Long = t._3/1000
//    }))
      .map(data => (data._1,1))
      .keyBy(0)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
      .sum(1)
//      .reduce((a,b) => (a._1,a._2 + b._2))
//      .addSink(new RedisSink[(String, Int)](redisConf,new MyRedis()))
      .print()
    env.execute()
  }
  class MyRedis() extends RedisMapper[(String,Int)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.HSET,"totalproduce")

    override def getKeyFromData(t: (String, Int)): String = t._1

    override def getValueFromData(t: (String, Int)): String = t._2 + ""
  }

}
