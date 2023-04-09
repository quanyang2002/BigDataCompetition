package Test

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.util.Properties

object FlinkTestLocal {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.23.32:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")

    val kafkaStream = env
      .addSource(new FlinkKafkaConsumer[(String)]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
    kafkaStream.print()

    env.execute()
  }

}
