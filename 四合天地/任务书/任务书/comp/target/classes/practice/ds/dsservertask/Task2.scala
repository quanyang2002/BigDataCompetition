package practice.ds.dsservertask

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.util.Properties

object Task2 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val sourceStream = env.addSource(new FlinkKafkaConsumer[String]("ods_mall_log", new SimpleStringSchema(), kafkaProp))
    sourceStream.filter(_.contains("customer_login_log"))
      .addSink(new FlinkKafkaProducer[String]("dim_customer_login_log",new SimpleStringSchema(),kafkaProp))
    env.execute()
  }

}
