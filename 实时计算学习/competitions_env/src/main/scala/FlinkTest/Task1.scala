package FlinkTest

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.util.Properties

object Task1 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[String]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
    val orderDetailStream = kafkaStream.filter(_.contains("order_detail"))
    val orderMasterStream = kafkaStream.filter(_.contains("order_master"))
    val orderDetailResult = orderDetailStream.map(data => "{" + data.split('{')(2).split('}')(0) + "}")
    val orderMasterResult = orderMasterStream.map(data => "{" + data.split('{')(2).split('}')(0) + "}")
    orderDetailResult.print("order_detail")
    orderMasterResult.print("order_master")
    orderMasterResult.addSink(new FlinkKafkaProducer[String]("fact_order_master",new SimpleStringSchema(),kafkaProp))
    orderDetailResult.addSink(new FlinkKafkaProducer[String]("fact_order_detail",new SimpleStringSchema(),kafkaProp))
    env.execute()
  }
}
