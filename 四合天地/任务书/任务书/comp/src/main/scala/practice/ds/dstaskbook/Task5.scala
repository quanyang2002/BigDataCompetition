package practice.ds.dstaskbook

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Properties

object Task5 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    val config = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
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
      .filter(_._1 == "1005")
      .map(data => (data._1,1))
      .keyBy(_ => true)
      .window(TumblingEventTimeWindows.of(Time.minutes(1)))
      .aggregate(new MyAggregate())
//      .addSink(new RedisSink(config,new MyRedis()))
      .print()
    env.execute()
  }
  class MyAggregate() extends AggregateFunction[(String,Int),Int,Int]{
    override def createAccumulator(): Int = 0

    override def add(in: (String, Int), acc: Int): Int = acc + in._2

    override def getResult(acc: Int): Int = acc

    override def merge(acc: Int, acc1: Int): Int = ???
  }
  class MyRedis() extends RedisMapper[Int]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: Int): String = "refundcountminute"

    override def getValueFromData(t: Int): String = t + ""
  }
}
