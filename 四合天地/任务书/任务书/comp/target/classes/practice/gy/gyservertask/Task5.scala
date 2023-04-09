package practice.gy.gyservertask

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.{ProcessAllWindowFunction, ProcessWindowFunction}
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.lang
import java.text.SimpleDateFormat
import java.util.Properties

object Task5 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")
    val RedisConf = new FlinkJedisPoolConfig
    .Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()

    val kafkaStream = env.addSource(new FlinkKafkaConsumer[(String)]("ChangeRecord", new SimpleStringSchema(), kafkaProp))
    kafkaStream
      .map(data => {
        val strings = data.split(",")
        (strings(1),strings(3),1)
      })
      .filter(_._2 == "待机")
      .map(data => (data._1,data._3))
      .keyBy(_._1)
      .windowAll(SlidingProcessingTimeWindows.of(Time.minutes(3),Time.minutes(1)))
      .aggregate(new MyAggregate(), new ProcessAllWindowFunction[(String,Int),String,TimeWindow] {
        override def process(context: Context, elements: Iterable[(String, Int)], out: Collector[String]): Unit = {
          val list = elements.toList
          list.sortBy(-_._2)
          val end = context.window.getEnd
          val simpleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
          val str = simpleFormat.format(end)
          out.collect(str + "," +list(0)._1)
        }
      }).print()
//      .addSink(new RedisSink[String](RedisConf,new MyRedis()))
//      .window(SlidingProcessingTimeWindows.of(Time.seconds(3),Time.seconds(3)))
//      .aggregate(new MyAggregate(),
//        new ProcessWindowFunction[(String,Int),String,String,TimeWindow] {
//          override def process(key: String, context: Context, elements: Iterable[(String, Int)], out: Collector[String]): Unit = {
//            val list = elements.toList
//            val end = context.window.getEnd
//            val simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//            val str = simpleFormat.format(end)
//            list.sortBy(-_._2)
//            out.collect(str + "," +list(0)._1 + "," + list(0)._2)
//          }
//        })
//      .map(data => (1,data.split(",")(0),data.split(",")(1),data.split(",")(2).toInt))
//
//      .keyBy(0)
//      .max(3)
////      .map(data => (data._2 + "," + data._3 + "," + data._4))
//      .print()

    env.execute()
  }
  class MyAggregate() extends AggregateFunction[(String,Int),(String,Int),(String,Int)]{
    override def createAccumulator(): (String, Int) = ("",0)

    override def add(in: (String, Int), acc: (String, Int)): (String, Int) = (in._1,acc._2+in._2)

    override def getResult(acc: (String, Int)): (String, Int) = acc

    override def merge(acc: (String, Int), acc1: (String, Int)): (String, Int) = ("",0)
  }
  class MyRedis() extends RedisMapper[(String)] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.HSET,"warning_last3min_everymin_out")

    override def getKeyFromData(t: String): String = t.split(",")(0)

    override def getValueFromData(t: String): String = t.split(",")(1)
  }

}
