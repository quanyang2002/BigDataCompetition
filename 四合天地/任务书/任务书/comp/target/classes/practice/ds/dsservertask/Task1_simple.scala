package practice.ds.dsservertask

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.util.Properties

object Task1_simple {

  def main(args: Array[String]): Unit = {

//    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    env.setParallelism(1)
//
//    val kafkaProp = new Properties()
//    kafkaProp.setProperty("bootstrap.servers", "192.168.3.55:9092")
//    kafkaProp.setProperty("auto.offset.reset", "earliest")
//
//    val kafkaStream = env.addSource(new FlinkKafkaConsumer[String]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
//    kafkaStream.filter(_.contains("order_master")).map(data => {
//      JSON.parseObject(data).getString("data")
//    }).addSink(new FlinkKafkaProducer[String]("fact_order_master", new SimpleStringSchema(), kafkaProp))
//    kafkaStream.filter(_.contains("order_detail"))
//      .map(data => {
//        JSON.parseObject(data).getString("data")
//      }).addSink(new FlinkKafkaProducer[String]("fact_order_detail", new SimpleStringSchema(), kafkaProp))
//    env.execute()
  }
}
