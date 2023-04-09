package Test

import com.alibaba.fastjson.JSON
import org.apache.flink.streaming.api.scala._

object Task4Test {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val source = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\sources\\data_merge.txt")
    source.filter(_.contains("order_master"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        val nObject1 = JSON.parseObject(nObject.getString("data"))
        val id = nObject1.getString("order_id")
        (id,1)
      })
      .keyBy(_=>true)
      .sum(1)
      .map(data => ("totalcount",data._2))
      .print()


    env.execute()
  }

}
