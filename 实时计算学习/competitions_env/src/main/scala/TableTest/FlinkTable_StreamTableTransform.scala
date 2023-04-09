package TableTest

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.{EnvironmentSettings, FieldExpression}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object FlinkTable_StreamTableTransform {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val streamSettings = EnvironmentSettings
      .newInstance()
      .useBlinkPlanner()
      .inStreamingMode()
      .build()
    val tableEnv = StreamTableEnvironment.create(env, streamSettings)
    env.setParallelism(1)
    val sourceStream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\实时计算学习\\competitions_env\\src\\main\\scala\\FlinkTest\\ds.txt")
    val mapSourceStream = sourceStream.map(data => (data.split(" ")(0), data.split(" ")(1).toInt, data.split(" ")(2).toInt))
    // 数据流转换成表
    // 1、将datastream转换为Table对象
    val dsTable1 = tableEnv.fromDataStream(mapSourceStream)
    // 2、直接将datastream注册成为表
    val dsTable2 = tableEnv.fromDataStream(mapSourceStream, $"prod_id", $"prod_count", $"prod_price")

    // 表转换为数据流
    // 1、通过使用todataStream方法直接进行转换
    val dsStream1 = tableEnv.toDataStream(dsTable1)
    // 2、使用toappendstream方法进行转换
    val dsStream2 = tableEnv.toAppendStream[(String, Int, Int)](dsTable2)

    env.execute()
  }

}
