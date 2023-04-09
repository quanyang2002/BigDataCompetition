package chapter02

import org.apache.flink.streaming.api.scala._

object BoundedStreamWordCount {

  def main(args: Array[String]): Unit = {

    // 创建一个流式执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 读取文本数据
    val lineStreamData = env.readTextFile("input/words.txt")
    // 对数据集进行转换处理
    val wordAndOne = lineStreamData.flatMap(_.split(" ")).map(word => (word, 1))
    // 按照单词进行分组
    val wordAndOneGroup = wordAndOne.keyBy(_._1)
    // 对分组数据进行sum聚合统计
    val result = wordAndOneGroup.sum(1)
    // 打印输出结果
    result.print()
    // 执行任务
    env.execute()
  }

}
