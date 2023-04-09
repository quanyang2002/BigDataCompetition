package practice.ds.dsservertask

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.util.Properties

object Task7 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")

    val kafkaStream = env.addSource(new FlinkKafkaConsumer[String]("fact_order_master", new SimpleStringSchema(), kafkaProp))
    kafkaStream.filter(_.contains("已退款")).print("Task7")
    env.execute("Task7")
  }

}
