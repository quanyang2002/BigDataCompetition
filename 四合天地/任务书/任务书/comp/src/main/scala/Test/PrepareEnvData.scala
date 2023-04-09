package Test

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.util.Properties

object PrepareEnvData {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    kafkaProp.setProperty("group.id","g1")

    val source = env.addSource(new FlinkKafkaConsumer[(String)]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
    source.writeAsText("/opt/data/data.txt",WriteMode.OVERWRITE)
    env.execute()
  }

}
