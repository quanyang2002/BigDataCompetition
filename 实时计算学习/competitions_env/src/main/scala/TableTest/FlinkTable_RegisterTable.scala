package TableTest

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.{EnvironmentSettings, FieldExpression}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object FlinkTable_RegisterTable {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableSetting = EnvironmentSettings
      .newInstance()
      .useBlinkPlanner()
      .inStreamingMode()
      .build()
    val tableEnv = StreamTableEnvironment
      .create(env, tableSetting)
    val sourceStream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\实时计算学习\\competitions_env\\src\\main\\scala\\FlinkTest\\ds.txt")
    val mapStream = sourceStream.map(data => (data.split(" ")(0), data.split(" ")(1).toInt))
    // 将流转换为表
    val dsTable = tableEnv.fromDataStream(mapStream, $"prod_id", $"prod_count")
    // 将表注册到catalog 便于在sql语句中直接使用表名
    tableEnv.createTemporaryView("dsTable",dsTable)
    env.setParallelism(1)
    env.execute()
  }

}
