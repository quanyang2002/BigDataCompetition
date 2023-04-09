package practice.ds.dstaskbook

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Properties

object Task6 {

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
        (strings(4),strings(3).toDouble,strings(10),strings(11))
      }).assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5))
      .withTimestampAssigner(new SerializableTimestampAssigner[(String,Double,String,String)] {
        override def extractTimestamp(t: (String, Double, String, String), l: Long): Long = {
          val simpleFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
          if (t._4 == ""){
            val time = simpleFormat.parse(t._3).getTime
            time
          }else if (t._3 >= t._4){
            simpleFormat.parse(t._3).getTime
          }else{
            simpleFormat.parse(t._4).getTime
          }
        }
      }))
      .keyBy(_ => true)
      .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
      .aggregate(new MyAggregate())
      .map(data => data+"%")
      .print()
    env.execute()
  }
  class MyAggregate() extends AggregateFunction[(String,Double,String,String),(Int,Int),Float]{
    override def createAccumulator(): (Int, Int) = (0,0)

    override def add(in: (String, Double, String, String), acc: (Int, Int)): (Int, Int) = {
      if (in._1 == "1003"){
        (acc._1+1,acc._2+1)
      }else{
        (acc._1,acc._2+1)
      }
    }
    override def getResult(acc: (Int, Int)): Float = (acc._1.toDouble / acc._2.toDouble).formatted("%.1f").toFloat

    override def merge(acc: (Int, Int), acc1: (Int, Int)): (Int, Int) = ???
  }

}
