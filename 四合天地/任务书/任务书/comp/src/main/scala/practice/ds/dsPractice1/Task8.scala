package practice.ds.dsPractice1

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.util.Properties
import scala.collection.JavaConversions.iterableAsScalaIterable

object Task8 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    kafkaProp.setProperty("group.id","g1")
    val config = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val sourceStream = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_master", new SimpleStringSchema(), kafkaProp))
    sourceStream
      .map(data => {
        val nObject = JSON.parseObject(data)
        val user = nObject.getString("shipping_user")
        val order_money = nObject.getString("order_money").toDouble
        (user,order_money)
      }).keyBy(_._1)
      .sum(1)
      .keyBy(_ => true)
      .process(new KeyedProcessFunction[Boolean,(String,Double),String] {
        var liststate:ListState[(String,Double)] = _

        override def open(parameters: Configuration): Unit = {
          liststate = getRuntimeContext.getListState(new ListStateDescriptor[(String, Double)]("name",classOf[(String,Double)]))
        }
        override def processElement(i: (String, Double), context: KeyedProcessFunction[Boolean, (String, Double), String]#Context, collector: Collector[String]): Unit = {
          liststate.add(i)
          val list = liststate.get().toList
          if (list.size >= 3){
            val stringToDouble = list.groupBy(_._1).mapValues(v => v.maxBy(-_._2)._2)
            val list1 = stringToDouble.toList.sortBy(-_._2)
            collector.collect(s"[${list1(0)._1}:${list1(0)._2},${list1(1)._1}:${list1(1)._2}]")
          }
        }
      }).map(data => ("top2userconsumption",data))
      .addSink(new RedisSink[(String, String)](config,new MyRedis()))

    env.execute()
  }
  class MyRedis() extends RedisMapper[(String,String)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String, String)): String = t._1

    override def getValueFromData(t: (String, String)): String = t._2
  }

}
