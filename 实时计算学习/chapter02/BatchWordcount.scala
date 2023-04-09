package chapter02

import org.apache.flink.api.scala._

object BatchWordcount {

  def main(args: Array[String]): Unit = {

    // 创建一个执行环境
    val env = ExecutionEnvironment.getExecutionEnvironment
    // 读取文本文件数据
    val lineDataSet = env.readTextFile("input/words.txt")
    // 对数据集进行转换处理
    val wordAndOne = lineDataSet.flatMap(_.split(" ")).map(word => (word, 1))
    // 按照单词进行分组
    val wordAndOneGroup = wordAndOne.groupBy(0)
    // 对分组数据进行sum聚合统计
    val result = wordAndOneGroup.sum(1)
    // 打印输出结果
    result.print()
  }

}
