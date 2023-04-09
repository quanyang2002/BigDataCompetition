package practice.ds.dsPractice2

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.util.Properties
import scala.collection.JavaConversions.iterableAsScalaIterable

object Task4 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    kafkaProp.setProperty("group.id","g1")
    val redisconf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()

    val sourceStream = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_master", new SimpleStringSchema(), kafkaProp))
    sourceStream.map(data => {
      val nObject = JSON.parseObject(data)
      nObject.getString("order_id")
    })
      .keyBy(_ => true)
      .process(new ProcessFunction[String,Int] {
      var liststate:ListState[(String)] = _

      override def open(parameters: Configuration): Unit = {
        liststate = getRuntimeContext.getListState(new ListStateDescriptor[String]("name",classOf[String]))
      }
      override def processElement(i: String, context: ProcessFunction[String, Int]#Context, collector: Collector[Int]): Unit = {
        val list = liststate.get().toList
        if (list.indexOf(i) == -1){
          liststate.add(i)
        }
        collector.collect(liststate.get().toList.size)
      }
    }).map(data => ("totalcount",data+""))
      .addSink(new RedisSink[(String, String)](redisconf,new MyRedis()))

    env.execute()
  }
  class MyRedis() extends RedisMapper[(String,String)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String, String)): String = t._1

    override def getValueFromData(t: (String, String)): String = t._2
  }

}
