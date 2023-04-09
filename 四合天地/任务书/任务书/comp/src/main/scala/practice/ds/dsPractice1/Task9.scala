package practice.ds.dsPractice1

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig

import java.util.Properties

object Task9 {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    kafkaProp.setProperty("group.id","g1")
    val config = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val masterStream = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_master", new SimpleStringSchema(), kafkaProp))
    val detailStream = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_detail", new SimpleStringSchema(), kafkaProp))

    val masterMap = masterStream
      .map(data => {
        val nObject = JSON.parseObject(data)
        (nObject.getString("order_sn"), nObject.getString("order_money").toDouble)
      }).keyBy(_._1)
      .sum(1)
    val detailMap = detailStream
      .map(data => {
        val nObject = JSON.parseObject(data)
        val sn = nObject.getString("order_sn")
        val cnt = nObject.getString("product_cnt").toInt
        (sn, cnt)
      }).keyBy(_._1)
      .sum(1)
    masterMap.join(detailMap)
      .where(_._1)
      .equalTo(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
      .apply(new MyJoin())
      .print()

    env.execute()
  }
  class MyJoin() extends JoinFunction[(String,Double),(String,Int),(String,Double,Int)]{
    override def join(in1: (String, Double), in2: (String, Int)): (String, Double, Int) = {
        (in1._1,in1._2,in2._2)
    }
  }
}
