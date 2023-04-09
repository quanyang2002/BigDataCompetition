package TableTest

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object FlinkTable_CreateTableEnv {

  def main(args: Array[String]): Unit = {

    // 获取流执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 设置执行并行度1
    env.setParallelism(1)
    // 设置环境相关配置(流处理)
    val streamSetting = EnvironmentSettings
      .newInstance()
      .useBlinkPlanner()
      .inStreamingMode()
      .build()
    // 设置环境相关配置(批处理)
    val batchSetting = EnvironmentSettings
      .newInstance()
      .useBlinkPlanner()
      .inBatchMode()
      .build()
    // 根据配置和流环境 创建表环境
    val tableEnv = StreamTableEnvironment.create(env, streamSetting)
    // 执行流计算任务
    env.execute()
  }

}
