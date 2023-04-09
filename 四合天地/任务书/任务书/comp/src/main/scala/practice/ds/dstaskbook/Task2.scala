package practice.ds.dstaskbook

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

import java.util.Properties

object Task2 {

  def main(args: Array[String]): Unit = {

    val environment = StreamExecutionEnvironment.getExecutionEnvironment
    environment.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    val config = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val kafkaStream = environment.addSource(new FlinkKafkaConsumer[(String)]("order1", new SimpleStringSchema(), kafkaProp))
    kafkaStream
      .map(data => {
        val strings = data.split(",")
        (strings(4),strings(3).toInt)
      }).filter(_._1 == "1006")
      .keyBy(_ => true)
      .sum(1)
//      .addSink(new RedisSink(config,new MyRedis()))
      .print()

    environment.execute()
  }
  class MyRedis() extends RedisMapper[(String,Int)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String, Int)): String = "totalrefundordercount"

    override def getValueFromData(t: (String, Int)): String = t._2 + ""
  }

}
