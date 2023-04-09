package FlinkTest

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, TableEnvironment}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object TableTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
//    val tableEnv = StreamTableEnvironment.create(env)
    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode()
      .build()
    val tableEnv = TableEnvironment.create(settings)
    env.execute()
  }

}
