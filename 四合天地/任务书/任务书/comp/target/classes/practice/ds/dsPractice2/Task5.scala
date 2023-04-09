package practice.ds.dsPractice2

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.util.Collector
import practice.ds.dsPractice2.Task4.MyRedis

import java.util.Properties
import scala.collection.JavaConversions.iterableAsScalaIterable

object Task5 {

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
    val source = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_detail", new SimpleStringSchema(), kafkaProp))

    source
      .map(data => {
        val nObject = JSON.parseObject(data)
        val product_id = nObject.getString("product_id")
        val product_cnt = nObject.getString("product_cnt").toInt
        (product_id,product_cnt)
      }).keyBy(_._1)
      .sum(1)
      .keyBy(_ => true)
      .process(new KeyedProcessFunction[Boolean,(String,Int),String] {
        var liststate:ListState[(String,Int)] = _

        override def open(parameters: Configuration): Unit = {
          liststate = getRuntimeContext.getListState(new ListStateDescriptor[(String,Int)]("name",classOf[(String,Int)]))
        }
        override def processElement(i: (String, Int), context: KeyedProcessFunction[Boolean, (String, Int), String]#Context, collector: Collector[String]): Unit = {
          liststate.add(i)
          val list = liststate.get().toList
          if (list.size >= 3){
            val stringToInt = list.groupBy(_._1).mapValues(v => v.maxBy(_._2)._2)
            val list1 = stringToInt.toList.sortBy(-_._2)
            collector.collect(s"[${list1(0)._1}:${list1(0)._2},${list1(1)._1}:${list1(1)._2},${list1(2)._1}:${list1(2)._2}]")
          }
        }
      }).map(data => ("top3itemamount",data))
      .addSink(new RedisSink[(String, String)](redisconf,new MyRedis()))

    env.execute()
  }

}
