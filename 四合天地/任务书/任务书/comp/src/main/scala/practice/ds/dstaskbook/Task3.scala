package practice.ds.dstaskbook

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import practice.gy.gyservertask.Task3.MySqlSink

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Properties

object Task3 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("order1", new SimpleStringSchema(), kafkaProp))
    kafkaStream
      .map(data => {
        val strings = data.split(",")
        (strings(0).toInt,strings(4).toInt,strings(3).toInt)
      }).filter(_._2 == 1003)
      .addSink(new MySqlSink())
    env.execute()
  }
  class MySqlSink() extends RichSinkFunction[(Int,Int,Int)]{
    var conn:Connection = _
    var insertstmt:PreparedStatement = _

    override def open(parameters: Configuration): Unit = {
      conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useSSL=false","root","password")
      insertstmt = conn.prepareStatement("insert into order_info values(?,?,?)")
    }

    override def invoke(value: (Int, Int, Int)): Unit = {
      insertstmt.setInt(1,value._1)
      insertstmt.setInt(2,value._2)
      insertstmt.setInt(3,value._3)
      insertstmt.execute()
    }

    override def close(): Unit = {
      insertstmt.close()
      conn.close()
    }
  }

}
