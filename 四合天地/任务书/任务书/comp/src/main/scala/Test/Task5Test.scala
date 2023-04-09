package Test

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import scala.collection.JavaConversions.iterableAsScalaIterable

object Task5Test {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val source = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\sources\\data_merge.txt")
    source.filter(_.contains("order_detail"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        val nObject1 = JSON.parseObject(nObject.getString("data"))
        val id = nObject1.getString("product_id")
        val cnt = nObject1.getString("product_cnt").toInt
        (id,cnt)
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
          liststate.add(i)
          val list = liststate.get().toList
          if (list.size >= 3){
            val stringToInt = list.groupBy(_._1).mapValues(v => v.maxBy(_._2)._2)
            val list1 = stringToInt.toList.sortBy(-_._2)
            collector.collect(s"[${list1(0)._1}:${list1(0)._2},${list1(1)._1}:${list1(1)._2},${list1(2)._1}:${list1(2)._2}]")
          }
        }
      }).print()

    env.execute()
  }

}
