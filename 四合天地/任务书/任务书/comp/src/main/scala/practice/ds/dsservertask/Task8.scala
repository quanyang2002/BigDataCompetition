package practice.ds.dsservertask

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.util.Properties
import scala.collection.JavaConversions.{bufferAsJavaList, mapAsJavaMap}
import scala.collection.mutable.ListBuffer

object Task8 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val redisConf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_master", new SimpleStringSchema(), kafkaProp))
    kafkaStream.map(
      data => {
        val nObject = JSON.parseObject(data)
        (nObject.getString("shipping_user"),nObject.getString("order_money").toDouble)
      }
    )
      .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(5)))
      .process(new ProcessAllWindowFunction[(String,Double),String,TimeWindow] {
        override def process(context: Context, elements: Iterable[(String, Double)], out: Collector[String]): Unit = {
          var mapList = new ListBuffer[(String,Double)]()
          var userSaleCount = Map[String,Double]()
          elements.foreach(
            el => userSaleCount.get(el._1) match {
              case Some(count) => userSaleCount.put(el._1,count+el._2)
              case None => userSaleCount.put(el._1,el._2)
            }
          )
          userSaleCount.keys.foreach(
            k => userSaleCount.get(k) match {
              case Some(count) => mapList += ((k,count))
              case None => mapList
            }
          )
          mapList.sortBy(-_._2)
          var result = ""
          for (i <- 0 to 1) {
            var tmp = mapList(i)
            if (i != 1){
              result = result + tmp._1 + ":" + tmp._2 + ","
            }else {
              result = result + tmp._1 + ":" + tmp._2
            }
          }
          out.collect("[" + result + "]")
        }
      }).addSink(new RedisSink[String](redisConf,new MyRedis()))
    env.execute()
  }
  class MyRedis() extends RedisMapper[String] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: String): String = "top2userconsumption"

    override def getValueFromData(t: String): String = t
  }
}
