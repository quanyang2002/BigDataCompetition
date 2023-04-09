package rec

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.util.Properties

object fuxi1 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")

    val sourceStream = env.addSource(new FlinkKafkaConsumer[(String)]("detail", new SimpleStringSchema(), kafkaProp))

    sourceStream.print()

    env.execute()
  }

}
