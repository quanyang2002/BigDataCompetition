package TableTest

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.state.StateTtlConfig.TtlTimeCharacteristic
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.{AnyWithOperations, EnvironmentSettings, FieldExpression}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

import java.time.Duration

object FlinkTable_TableTimeSetting {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    val streamSetting = EnvironmentSettings
      .newInstance()
      .useBlinkPlanner()
      .inStreamingMode()
      .build()
    val tableEnv = StreamTableEnvironment.create(env, streamSetting)
    val sourceStream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\实时计算学习\\competitions_env\\src\\main\\scala\\FlinkTest\\ds.txt")
    // 对于源数据流进行转换操作并添加水位线
    val mapStream = sourceStream.map(data => (data.split(" ")(0), data.split(" ")(1).toInt, data.split(" ")(3).toLong))
      .assignTimestampsAndWatermarks(WatermarkStrategy
        .forBoundedOutOfOrderness(Duration.ofSeconds(2))
      .withTimestampAssigner(new SerializableTimestampAssigner[(String,Int,Long)] {
        override def extractTimestamp(t: (String, Int, Long), l: Long): Long = t._3
      }))
    // 数据流转换成表
    // 注意这里把第三个字段作为时间语义且该字段在mapdataStream中存在
    // 对于在源数据流中不存在的字段，直接在转换时添加即可(处理时间也是类似的情况)
    // .rowtime 事件时间 .proctime 处理时间
    val dsTable = tableEnv.fromDataStream(mapStream, $"prod_name", $"produce_num", $"produce_time".rowtime)
    // 将表注册到catalog
    tableEnv.createTemporaryView("dsTable",dsTable)
    // 执行环境
    env.execute()
  }

}
