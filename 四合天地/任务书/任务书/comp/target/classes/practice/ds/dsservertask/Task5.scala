package practice.ds.dsservertask

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, SlidingProcessingTimeWindows, TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function._
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.text.SimpleDateFormat
import java.util.Properties
import scala.collection.JavaConversions.mapAsJavaMap
import scala.collection.mutable.ListBuffer

object Task5 {

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
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
    kafkaStream
      .filter(_.contains("order_detail"))
      .map(data => {
        val str = JSON.parseObject(data).getString("data")
        val nObject = JSON.parseObject(str)
        (nObject.getString("product_id"),nObject.getString("product_cnt").toInt,{
          val format = new SimpleDateFormat("yymmddhhmmss")
          val time = format.parse(nObject.getString("create_time")).getTime
          time
        })
      }).assignAscendingTimestamps(data => data._3)
      .windowAll(SlidingProcessingTimeWindows.of(Time.seconds(10),Time.seconds(5)))
      .process(new ProcessAllWindowFunction[(String,Int,Long),String,TimeWindow] {
        override def process(context: Context, elements: Iterable[(String, Int,Long)], out: Collector[String]): Unit = {
          var mapList = new ListBuffer[(String,Int)]
          var productCountMap = Map[String,Int]()
          elements.foreach(
            el => productCountMap.get(el._1) match {
              case Some(count) => productCountMap.put(el._1,count+el._2)
              case None => productCountMap.put(el._1,el._2)
            }
          )
          productCountMap.keys.foreach(
            k => productCountMap.get(k) match {
              case Some(count) => mapList += ((k,count))
              case None => mapList
            }
          )
          mapList.sortBy(-_._2)
          var result = new StringBuilder
          result.append("[")
          var i = 0
          for (i <- 0 to 2){
            if (i != 2){
              result.append(mapList(i)._1 + ":" + mapList(i)._2 + ',')
            }else{
              result.append(result + mapList(i)._1 + ":" + mapList(i)._2)
            }
          }
          result.append("]")
          out.collect(result.toString())
        }
      })
      .addSink(new RedisSink[(String)](redisConf,new MyRedis()))
    env.execute()
  }
  class MyRedis extends RedisMapper[(String)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: String): String = "top3itemamount"

    override def getValueFromData(t: String): String = t
  }
}
