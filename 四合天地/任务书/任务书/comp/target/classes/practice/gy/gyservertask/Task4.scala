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
import java.util.Properties

object Task4 {

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
        (strings(1).toInt,strings(3),strings(4),strings(5),1)
      }).keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
      .aggregate(new MyAggregate()
      ,new ProcessWindowFunction[(Int,String,Int),(Int,String,Int,String),Int,TimeWindow] {
          override def process(key: Int, context: Context, elements: Iterable[(Int, String, Int)], out: Collector[(Int, String, Int, String)]): Unit = {
            val list = elements.toList
            val simpleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val str = simpleFormat.format(context.window.getEnd)
            var i = 0
            for (i <- 0 to list.length-1){
              out.collect(list(i)._1,list(i)._2,list(i)._3,str)
            }
          }
        }).addSink(new MysqlSink())
//      .print()
    env.execute()
  }
  class MyAggregate extends AggregateFunction[(Int,String,String,String,Int),(Int,Int,String),(Int,String,Int)] {
    override def createAccumulator(): (Int,Int, String) = (0,0,"待机")

    override def add(in: (Int, String, String, String, Int), acc: (Int,Int, String)): (Int,Int, String) = {
      if (in._2 == "运行"){
        (in._1,acc._2+1,acc._3)
      }else {
        (in._1,acc._2,in._2)
      }
    }
    override def getResult(acc: (Int,Int, String)): (Int, String, Int) = (acc._1,acc._3,acc._2)

    // 累加器为什么要聚合？
    override def merge(acc: (Int,Int, String), acc1: (Int,Int, String)): (Int,Int, String) = (acc._1,acc._2 + acc1._2,acc._3)
  }
  class MysqlSink() extends RichSinkFunction[(Int,String,Int,String)] {
    var connection:Connection = _
    var insertstmt:PreparedStatement = _

    override def open(parameters: Configuration): Unit = {
      connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useSSL=false","root","password")
      insertstmt = connection.prepareStatement("insert into change_state_other_to_run_agg values(?,?,?,?)")
    }

    override def invoke(value: (Int, String, Int, String)): Unit = {
      insertstmt.setInt(1,value._1)
      insertstmt.setString(2,value._2)
      insertstmt.setInt(3,value._3)
      insertstmt.setString(4,value._4)
      insertstmt.execute()
    }

    override def close(): Unit = {
      insertstmt.close()
      connection.close()
    }
  }
}
