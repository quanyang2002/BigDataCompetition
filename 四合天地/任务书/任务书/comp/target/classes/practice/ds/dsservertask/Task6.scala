package practice.ds.dsservertask

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaConsumerBase}
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.table.runtime.operators.window.assigners.TumblingWindowAssigner
import org.apache.flink.util.Collector

import java.time.Duration
import java.util.Properties
import scala.collection.JavaConversions.mapAsJavaMap
import scala.collection.mutable.ListBuffer

object Task6 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val redisConf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379).build()
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_detail", new SimpleStringSchema(), kafkaProp))
    kafkaStream.map(data => {
      val nObject = JSON.parseObject(data)
      (nObject.getString("product_id"),nObject.getString("product_cnt").toInt*nObject.getString("product_price").toDouble)
    }).keyBy(0)
      .sum(1)
      .map(data => (1,data._1,data._2))
      .keyBy(0)
      .timeWindowAll(Time.seconds(5))
      .process(new ProcessAllWindowFunction[(Int,String,Double),String,TimeWindow] {
        override def process(context: Context, elements: Iterable[(Int, String, Double)], out: Collector[String]): Unit = {
          var mapList = new ListBuffer[(String,Double)]
          var productSaleMap = Map[String,Double]()
          elements.foreach(
            el => productSaleMap.get(el._2) match {
              case Some(count) => productSaleMap.put(el._2,count+el._3)
              case None => productSaleMap.put(el._2,el._3)
            }
          )
          productSaleMap.keys.foreach(
            k => productSaleMap.get(k) match {
              case Some(count) => mapList += ((k,count))
              case None => mapList
            }
          )
          mapList.sortBy(-_._2)
          var result = ""
          for (i <- 0 to 2){
            var tmp = mapList(i)
            if (i!=2){
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

    override def getKeyFromData(t: String): String = "top3itemconsumption"

    override def getValueFromData(t: String): String = t
  }
}
