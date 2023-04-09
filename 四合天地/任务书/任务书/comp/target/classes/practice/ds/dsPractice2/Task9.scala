package practice.ds.dsPractice2

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Properties

object Task9 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    kafkaProp.setProperty("group.id","g1")

    val masterSource = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_master", new SimpleStringSchema(), kafkaProp))
    val detailSource = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_detail", new SimpleStringSchema(), kafkaProp))

    val masterMap = masterSource
      .map(data => {
        val nObject = JSON.parseObject(data)
        val sn = nObject.getString("order_sn")
        val money = nObject.getString("order_money").toDouble
        (sn, money)
      }).keyBy(_._1)
      .sum(1)
    val detailMap = detailSource
      .map(data => {
        val nObject = JSON.parseObject(data)
        val sn = nObject.getString("order_sn")
        val cnt = nObject.getString("product_cnt").toInt
        (sn, cnt)
      }).keyBy(_._1)
      .sum(1)

    masterMap
      .join(detailMap)
      .where(_._1)
      .equalTo(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .apply(new JoinFunction[(String,Double),(String,Int),(String,Double,Int)] {
        override def join(in1: (String, Double), in2: (String, Int)): (String, Double, Int) = {

          (in1._1,in1._2,in2._2)
        }
      }).addSink(new MySink())
    env.execute()
  }
  class MySink() extends RichSinkFunction[(String,Double,Int)]{
    var connect:Connection = _
    var insertstmt:PreparedStatement = _
    override def open(parameters: Configuration): Unit = {
      connect = DriverManager.getConnection("jdbc:mysql://192.168.3.55:3306/test?useSSL=false","root","123456")
      insertstmt = connect.prepareStatement("insert into task9 values(?,?,?)")
    }

    override def invoke(value: (String, Double, Int)): Unit ={
      insertstmt.setString(1,value._1)
      insertstmt.setDouble(2,value._2)
      insertstmt.setInt(3,value._3)
      insertstmt.execute()
    }

    override def close(): Unit = {
      insertstmt.close()
      connect.close()
    }

  }

}
