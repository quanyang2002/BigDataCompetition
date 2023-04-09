package FlinkTest

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.bridge.scala._

case class Event(name:String,visit:String,timeStamp:Long)
object tb {

  def main(args: Array[String]): Unit = {

    // 获取流执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 设置流执行环境并行度为1
    env.setParallelism(1)
    // 读取数据源
    val eventStream = env
      .fromElements(
        Event("Alice", "./home", 1000L),
        Event("Bob", "./cart", 1000L),
        Event("Alice", "./prod?id=1", 5 * 1000L),
        Event("Cary", "./home", 60 * 1000L),
        Event("Bob", "./prod?id=3", 90 * 1000L),
        Event("Alice", "./prod?id=7", 105 * 1000L)
      )
    // 获取表执行环境
    val tableEnv = StreamTableEnvironment.create(env)
//    tableEnv.executeSql("create" with ("connector" = "kafka",))
    // 将数据流转换为表
    val table = tableEnv.fromDataStream(eventStream)
    // 以执行sql的方式提取数据
    val result = tableEnv.sqlQuery("select name,timestamp from " + table)
    // 将表转换为流 打印输出
    result.toAppendStream[(String,Long)].print()

    env.execute()
  }

}
