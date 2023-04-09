package practice.ds.dsPractice1

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
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
    kafkaProp.setProperty("auto.offset.reset","latest")
    val config = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()

    val sourcestream = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_master", new SimpleStringSchema(), kafkaProp))
    sourcestream.map(
      data => {
        val value = JSON.parseObject(data)
        (value.getString("order_id"),1)
      }
    ).keyBy(0)
      .reduce((a,b) =>(a._1,1))
      .keyBy(_ => true)
      .sum(1)
//      .map(data => (data._1,data._2))
//      .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(5)))
//      .sum(1)
      .map(data => ("totalcount",data._2))
      .addSink(new RedisSink(config,new MyRedis()))

    env.execute()
  }
  class MyRedis() extends RedisMapper[(String,Int)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String, Int)): String = t._1

    override def getValueFromData(t: (String, Int)): String = t._2 + ""
  }

}
