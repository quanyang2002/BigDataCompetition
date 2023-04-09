package FlinkTest

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, TableEnvironment}

object TableEnvTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    // 创建表环境配置项
    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode() // 使用流差不离模式
      .build()
    val tableEnv = TableEnvironment.create(settings)
    // 自定义catalog 和 Database
    tableEnv.useCatalog("default_catalog")
    tableEnv.useDatabase("default_database")
    env.execute()
  }

}
