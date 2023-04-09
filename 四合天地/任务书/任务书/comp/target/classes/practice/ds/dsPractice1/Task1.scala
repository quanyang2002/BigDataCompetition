package practice.ds.dsPractice1

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.util.Properties

object Task1 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")

    val sourcestream = env.addSource(new FlinkKafkaConsumer[(String)]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
    val masterSource = sourcestream.filter(_.contains("order_master"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        nObject.getString("data")
      }).addSink(new FlinkKafkaProducer[String]("fact_order_master",new SimpleStringSchema(),kafkaProp))
    val detailSource = sourcestream.filter(_.contains("order_detail"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        nObject.getString("data")
      }).addSink(new FlinkKafkaProducer[String]("fact_order_detail",new SimpleStringSchema(),kafkaProp))

    env.execute()
  }

}
