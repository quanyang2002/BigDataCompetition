package practice.ds.dsservertask

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

import java.text.SimpleDateFormat
import java.util.Properties

object Task4 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    var redisConf = new FlinkJedisPoolConfig.Builder().setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val sourceStream = env.addSource(new FlinkKafkaConsumer[String]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
    sourceStream
      .filter(_.contains("order_master"))
      .map(data => {
      val value = JSON.parseObject(JSON.parseObject(data).getString("data"))
      val format = new SimpleDateFormat("yyyymmddhhmmss")
      val time = format.parse(value.getString("create_time")).getTime
      (1,value.getString("order_id"),time)
    }).assignAscendingTimestamps(data => data._3)
      .keyBy(1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(10L)))
      .reduce((a,b) => (1,a._2,{
        if (a._3 < b._3){
          a._3
        }else{
          b._3
        }
      })).keyBy(0)
      .sum(0)
      .map(data => ("totalcount",data._1.toLong))
      .addSink(new RedisSink[(String,Long)](redisConf,new MyRedis()))
    env.execute()
  }
  class MyRedis() extends RedisMapper[(String,Long)] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String, Long)): String = t._1

    override def getValueFromData(t: (String, Long)): String = t._2 + ""
  }

}
