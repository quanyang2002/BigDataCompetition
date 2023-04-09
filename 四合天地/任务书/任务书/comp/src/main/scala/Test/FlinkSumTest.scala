package Test

import org.apache.flink.streaming.api.scala._

object FlinkSumTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val value = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\Test\\test_words")
    value.map(data => {
      val strings = data.split(" ")
      (strings(0),strings(1).toInt)
    }).keyBy(_._1)
      .reduce((a,b) => (a._1,a._2+b._2))
    .print()

    env.execute()
  }

}
