package practice.ds.dstaskbook

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Properties

object Task9 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    val orderStream = env.addSource(new FlinkKafkaConsumer[(String)]("order1", new SimpleStringSchema(), kafkaProp))
    val detailStream = env.addSource(new FlinkKafkaConsumer[(String)]("detail",new SimpleStringSchema(), kafkaProp))

    val orderFirstStream = orderStream
      .map(data => {
        val strings = data.split(",")
        (strings(0), strings(3).toDouble, strings(10), strings(11))
      }).assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5))
      .withTimestampAssigner(new SerializableTimestampAssigner[(String, Double, String, String)] {
        override def extractTimestamp(t: (String, Double, String, String), l: Long): Long = {
          val simpleFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
          if (t._4 == "") {
            simpleFormat.parse(t._3).getTime
          } else {
            if (t._3 > t._4) {
              simpleFormat.parse(t._3).getTime
            } else {
              simpleFormat.parse(t._4).getTime
            }
          }
        }
      }))
    val detailFirstStream = detailStream
      .map(data => {
        val strings = data.split(",")
        (strings(1), strings(6).toInt, strings(7))
      })
      .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5))
        .withTimestampAssigner(new SerializableTimestampAssigner[(String, Int, String)] {
          override def extractTimestamp(t: (String, Int, String), l: Long): Long = {
            val simpleFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
            simpleFormat.parse(t._3).getTime
          }
        }))
    orderFirstStream.join(detailFirstStream)
      .where(_._1)
      .equalTo(_._1)
      .window(TumblingEventTimeWindows.of(Time.seconds(10)))
      .apply(new MyJoin())
      .addSink(new MysqlSink())

//    detailStream.print("detail")
    env.execute()
  }
  class MyJoin() extends JoinFunction[(String,Double,String,String),(String,Int,String),(Int,Double,Int)]{
    override def join(in1: (String, Double, String, String), in2: (String, Int, String)): (Int, Double, Int) = {
      (in1._1.toInt,in1._2,in2._2)
    }
  }
  class MysqlSink() extends RichSinkFunction[(Int,Double,Int)] {
    var connect:Connection = _
    var insertstmt:PreparedStatement = _

    override def open(parameters: Configuration): Unit = {
      connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useSSL=false","root","password")
      insertstmt = connect.prepareStatement("insert into orderpostiveaggr values(?,?,?)")
    }

    override def invoke(value: (Int, Double, Int)): Unit = {
      insertstmt.setInt(1,value._1)
      insertstmt.setDouble(2,value._2)
      insertstmt.setInt(3,value._3)
      insertstmt.execute()
    }

    override def close(): Unit = {
      connect.close()
      insertstmt.close()
    }
  }

}
