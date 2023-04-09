import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector

import java.util.Properties
import scala.collection.JavaConversions.iterableAsScalaIterable

object Task4 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    kafkaProp.setProperty("group.id","g1")
    val redisconf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()

//    val source = env.addSource(new FlinkKafkaConsumer[(String)]("fact_order_master", new SimpleStringSchema(), kafkaProp))
    val source = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp_flink110\\src\\main\\scala\\sources\\data_merge.txt")
    source
      .filter(_.contains("order_master"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        nObject.getString("data")
      })
      .map(data => {
        val value = JSON.parseObject(data)
        val order_id = value.getString("order_id")
        (order_id,1)
      })
      .keyBy(_._1)
      .sum(1)
      .keyBy(_ => true)
      .process(new KeyedProcessFunction[Boolean,(String,Int),String] {
        var liststate:ListState[(String,Int)] = _

        override def open(parameters: Configuration): Unit = {
          liststate = getRuntimeContext.getListState(new ListStateDescriptor[(String, Int)]("name",classOf[(String,Int)]))
        }
        override def processElement(i: (String, Int), context: KeyedProcessFunction[Boolean, (String, Int), String]#Context, collector: Collector[String]): Unit = {
          val list = liststate.get().toList
          if (list.indexOf(i) == -1){
            liststate.add(i)
          }
          val list1 = liststate.get().toList
          collector.collect(list1.size + "")
        }
      })
      .map(data => ("totalcount",data))
//      .addSink(new RedisSink[(String,String)](redisconf,new MyRedis()))
      .print()
    env.execute()
  }
  class MyRedis extends RedisMapper[(String,String)]{
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String, String)): String = t._1

    override def getValueFromData(t: (String, String)): String = t._2
  }

}
