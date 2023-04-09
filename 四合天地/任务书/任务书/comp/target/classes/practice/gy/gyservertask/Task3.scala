package practice.gy.gyservertask

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.util.Collector

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.text.SimpleDateFormat
import java.util.{Date, Properties}

object Task3 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("ChangeRecord", new SimpleStringSchema(), kafkaProp))
    kafkaStream
      .map(data => {
        val strings = data.split(",")
        (strings(1),strings(2),strings(3))
      })
      .filter(_._2 == "0")
      .filter(_._3 == "待机")
      .map(data => (data._1,1))
      .keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.minutes(3)))
      .aggregate(new MyAggregate(),new ProcessWindowFunction[(String,Int),(String,Int,String),String,TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[(String, Int)], out: Collector[(String, Int, String)]): Unit = {
          val simpleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
          val end = context.window.getEnd
          val str = simpleFormat.format(end)
          val list = elements.toList
          var i = 0
          for (i <- 0 to list.length-1){
            out.collect((list(i)._1,list(i)._2,str))
          }
        }
      })
      .map(data => (data._1.toInt,data._2,data._3))
      .addSink(new MySqlSink())

    env.execute()
  }
  class MyAggregate() extends AggregateFunction[(String,Int),(String,Int),(String,Int)]{
    override def createAccumulator(): (String, Int) = ("",0)

    override def add(in: (String, Int), acc: (String, Int)): (String, Int) = (in._1,in._2+acc._2)

    override def getResult(acc: (String, Int)): (String, Int) = acc

    override def merge(acc: (String, Int), acc1: (String, Int)): (String, Int) = (acc._1,acc._2+acc1._2)
  }
  class MySqlSink() extends RichSinkFunction[(Int,Int,String)] {
    var connection:Connection = _
    var insertstmt:PreparedStatement = _

    override def open(parameters: Configuration): Unit = {
      connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useSSL=false","root","password")
      insertstmt = connection.prepareStatement("insert into threemin_warning_state_agg values(?,?,?)")
    }

    override def invoke(value: (Int, Int, String)): Unit = {
      insertstmt.setInt(1,value._1)
      insertstmt.setInt(2,value._2)
      insertstmt.setString(3,value._3)
      insertstmt.execute()
    }

    override def close(): Unit = {
      insertstmt.close()
      connection.close()
    }
    }
}
