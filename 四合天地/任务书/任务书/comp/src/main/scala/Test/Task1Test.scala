package Test

import com.alibaba.fastjson.JSON
import org.apache.flink.streaming.api.scala._

object Task1Test {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val source = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\sources\\data_merge.txt")
    val masterStream = source.filter(_.contains("order_master"))
    val detailStream = source.filter(_.contains("order_detail"))

    masterStream
      .map(data => {
        val nObject = JSON.parseObject(data)
        val str = nObject.getString("data")
        str
      }).print("order_master")
    detailStream
      .map(data => {
        val nObject = JSON.parseObject(data)
        nObject.getString("data")
      }).print("order_detail")

    env.execute()
  }

}
