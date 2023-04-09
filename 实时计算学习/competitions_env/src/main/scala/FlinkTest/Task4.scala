package FlinkTest

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import java.util.Properties

object Task4 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[String]
    ("fact_order_master", new SimpleStringSchema(), kafkaProp))
    val conf = new FlinkJedisPoolConfig.Builder().setHost("192.168.3.55").setPort(6379).setPassword("123456").build()
    val resultStream = kafkaStream.map(data => (data, 1)).keyBy(_._1).timeWindow(Time.seconds(10L))
      .sum(2).map(data => ("totalcount", data._2.toLong))
    resultStream
      .addSink(new RedisSink[(String,Long)](conf,new MyRedis()))
    resultStream.print("totalcount")
    env.execute()
  }
  class MyRedis() extends RedisMapper[(String,Long)] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String,Long)): String = t._1

    override def getValueFromData(t: (String,Long)): String = t._2+""
  }
}
