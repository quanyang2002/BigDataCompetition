package practice.ds.dsPractice1

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{SlidingProcessingTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.util.Properties
import scala.collection.JavaConversions.iterableAsScalaIterable

object Task5 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers", "192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset", "latest")
    val config = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()

    val sourceStream = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_detail", new SimpleStringSchema(), kafkaProp))
    val ds1 = sourceStream
      .map(data => {
        val obj = JSON.parseObject(data)
        (obj.getString("product_id"), obj.getString("product_cnt").toInt)
      }).keyBy(_._1)
//      .window(SlidingProcessingTimeWindows.of(Time.seconds(60), Time.seconds(10)))
      .sum(1)
      .keyBy(_ => true)

      .process(new KeyedProcessFunction[Boolean, (String, Int), String] {
        var listState: ListState[(String, Int)] = _


        override def open(parameters: Configuration): Unit = {
          listState=getRuntimeContext.getListState(new ListStateDescriptor[(String, Int)]("name",classOf[(String,Int)]))
        }

        override def processElement(i: (String, Int), context: KeyedProcessFunction[Boolean, (String, Int), String]#Context, collector: Collector[String]): Unit = {
            listState.add(i)
          val list =  listState.get().toList
         if(list.size>=3){
           val stringToInt: Map[String, Int] = list.groupBy(_._1).mapValues(v => v.maxBy(-_._2)._2)
           val list1: List[(String, Int)] = stringToInt.toList.sortBy(-_._2)
           println(s"list1.head = ${list1.head},${list1(1)},${list1(2)}")

           list1(2)
         }
        }

      })
    //      .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10)))
    //      .process(new ProcessAllWindowFunction[(String,Int),String,TimeWindow] {
    //        override def process(context: Context, elements: Iterable[(String, Int)], out: Collector[String]): Unit = {
    //          val list = elements.toSeq.sortBy(-_._2).toList
    //          var i = 0
    //          val result = new StringBuilder()
    //          for (i <- 0 to 2){
    //            if (i != 2){
    //              result.append(list(i)._1 + ":" + list(i)._2 + ",")
    //            }else{
    //              result.append(list(i)._1 + ":" + list(i)._2)
    //            }
    //          }
    //          out.collect("[" + result.toString() + "]")
    //        }
    //      }).map(data => ("top3itemamount",data))
//    ds1.map(n => println(n))
//    ds1.addSink(new RedisSink(config, new MyRedis()))

    env.execute()
  }

  class MyRedis() extends RedisMapper[(String, String)] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String, String)): String = t._1

    override def getValueFromData(t: (String, String)): String = t._2
  }

}
