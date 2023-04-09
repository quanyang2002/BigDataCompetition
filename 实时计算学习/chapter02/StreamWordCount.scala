package chapter02

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._

object StreamWordCount {

  def main(args: Array[String]): Unit = {

    // 创建一个流式执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 读取socket文本流数据
    // 生产环境中的参数配置有两种方式：1 从配置文件中读取配置内容 2 通过运行程序时指定参数方式设定参数
//    val lineStreamData = env.socketTextStream("localhost",9999)
    // 运行时指定参数
    val tool = ParameterTool.fromArgs(args)
    val hostname = tool.get("host")
    val port = tool.getInt("port")
    val lineStreamData = env.socketTextStream(hostname, port)
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
