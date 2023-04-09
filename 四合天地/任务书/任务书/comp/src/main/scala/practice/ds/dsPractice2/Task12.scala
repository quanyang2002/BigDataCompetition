package practice.ds.dsPractice2

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.util.Properties

object Task12 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    kafkaProp.setProperty("group.id","g1")

    val sourceStream = env.addSource(new FlinkKafkaConsumer[(String)]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
    val logStream = env.addSource(new FlinkKafkaConsumer[(String)]("ods_mall_log", new SimpleStringSchema(), kafkaProp))
    val masterStream = sourceStream.filter(_.contains("order_master"))
    val detailStream = sourceStream.filter(_.contains("order_detail"))
    val customerLogin = logStream.filter(_.contains("customer_login_log"))
    masterStream.map(
      data => {
        val nObject = JSON.parseObject(data)
        nObject.getString("data")
      }
    ).addSink(new FlinkKafkaProducer[String]("fact_order_master",new SimpleStringSchema(),kafkaProp))
    detailStream
      .map(data => {
        val nObject = JSON.parseObject(data)
        nObject.getString("data")
      }).addSink(new FlinkKafkaProducer[String]("fact_order_detail",new SimpleStringSchema(),kafkaProp))
    customerLogin.addSink(new FlinkKafkaProducer[String]("dim_customer_login_log",new SimpleStringSchema(),kafkaProp))

    env.execute()
  }

}
