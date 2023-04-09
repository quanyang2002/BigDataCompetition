import Task4.MyRedis
import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.util.Collector

import scala.collection.JavaConversions.iterableAsScalaIterable

object Task8 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val redisconf = new FlinkJedisPoolConfig.Builder()
      .setHost("192.168.3.55")
      .setPort(6379)
      .build()
    val source = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp_flink110\\src\\main\\scala\\sources\\data_merge.txt")
    source.filter(_.contains("order_master"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        val nObject1 = JSON.parseObject(nObject.getString("data"))
        val user = nObject1.getString("shipping_user")
        val order_money = nObject1.getString("order_money").toDouble
        (user,order_money)
      }).keyBy(_._1)
      .sum(1)
      .keyBy(_ => true)
      .process(new KeyedProcessFunction[Boolean,(String,Double),String] {
        var liststate:ListState[(String,Double)] = _

        override def open(parameters: Configuration): Unit = {
          liststate = getRuntimeContext.getListState(new ListStateDescriptor[(String,Double)]("name",classOf[(String,Double)]))
        }
        override def processElement(i: (String, Double), context: KeyedProcessFunction[Boolean, (String, Double), String]#Context, collector: Collector[String]): Unit = {
          liststate.add(i)
          val list = liststate.get().toList
          if (list.size >= 3){
            val stringToInt = list.groupBy(_._1).mapValues(v => v.maxBy(_._2)._2)
            val list1 = stringToInt.toList.sortBy(-_._2)
            collector.collect(s"[${list1(0)._1}:${list1(0)._2},${list1(1)._1}:${list1(1)._2}]")
          }
        }
      }).map(data => ("top2userconsumption",data))
      //      .addSink(new RedisSink[(String,String)](redisconf,new MyRedis()))
      .print()

    env.execute()
  }

}
