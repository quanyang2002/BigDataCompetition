package FlinkTest

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, tableConversions}

object TableAndSqlSimpleTest {

  def main(args: Array[String]): Unit = {

    // 获取流执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 设置流执行环境并行度为1
    env.setParallelism(1)
    // 获取流数据源
    val eventStream = env
      .fromElements(
        Event("Alice", "./home", 1000L),
        Event("Bob", "./cart", 1000L),
        Event("Alice", "./prod?id=1", 5 * 1000L),
        Event("Cary", "./home", 60 * 1000L),
        Event("Bob", "./prod?id=3", 90 * 1000L),
        Event("Alice", "./prod?id=7", 105 * 1000L)
      )
    // 创建表执行环境所需配置项
//    val settings = EnvironmentSettings
//      .newInstance()
//      .inStreamingMode()
//      .build()
    // 创建表执行环境
    val tableEnv = StreamTableEnvironment.create(env)
    // 将流转换为表
    val eventTable = tableEnv.fromDataStream(eventStream)
    // 将表名注册到catalog
    tableEnv.createTemporaryView("events",eventTable)
    // 业务需求
    // 统计Alice查询过的url列表
    val result1 = tableEnv.sqlQuery("select * from events")
    // 统计每个用户的访问次数
//    val result2 = tableEnv.sqlQuery("select user,count(*) from events group by user'")
    // 结果输出
    tableEnv.toChangelogStream(result1).print("Alice's urls")
//    tableEnv.toDataStream(result2).print("user's visit count")

    env.execute()
  }

}
