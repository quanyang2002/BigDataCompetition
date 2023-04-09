package practice.real

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

import java.text.SimpleDateFormat
import java.util.Properties

object Task1 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val redisConf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[String]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
    val mapStream = kafkaStream.filter(_.contains("order_master"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        val str = JSON.parseObject(nObject.getString("data"))
        (str.getString("shipping_user"), str.getString("order_money"), str.getString("create_time"))
      })
      .map(data => {
        val format = new SimpleDateFormat("yyyyMMddHHmmss")
        val cur_timestamp = format.parse(data._3).getTime
        (data._1, data._2.toDouble, cur_timestamp)
      }).assignAscendingTimestamps(data => data._3)
    mapStream
      .keyBy(_._1)
      .window(TumblingEventTimeWindows.of(Time.seconds(60)))
      .reduce((a,b) => (a._1,a._2 + b._2,{
        if (a._3 < b._3){
          a._3
        }else{
          b._3
        }
      })).addSink(new RedisSink[(String, Double, Long)](redisConf,new MyRedis()))

    env.execute()

  }
  class MyRedis() extends RedisMapper[(String,Double,Long)] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.HSET,"totalprice")

    override def getKeyFromData(t: (String, Double, Long)): String = t._1

    override def getValueFromData(t: (String, Double, Long)): String = t._2 + ""
  }

}
