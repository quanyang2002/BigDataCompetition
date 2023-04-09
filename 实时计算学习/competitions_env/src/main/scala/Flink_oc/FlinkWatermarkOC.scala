package Flink_oc

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.state.StateTtlConfig.TtlTimeCharacteristic
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import java.time.Duration

object FlinkWatermarkOC {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 设置当前环境时间语义为事件时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)
    // 获取测试流
    val stream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\实时计算学习\\competitions_env\\src\\main\\scala\\FlinkTest\\ds.txt")
    // 给数据流添加水位线和时间戳
//    stream.assignTimestampsAndWatermarks(WatermarkStrategy
      // 设置生成固定延迟水位线 需要对数据整体最大延迟时间有一个大概的预估
//      .forBoundedOutOfOrderness[String](Duration.ofSeconds(10L))
      // 设置生成单调递增水位线
//      .forMonotonousTimestamps()
      // 提取数据中的时间属性作为当前环境的事件时间语义
//    .withTimestampAssigner(new SerializableTimestampAssigner[String] {
//      override def extractTimestamp(t: String, l: Long): Long = t.split(" ")(1)
//    }))
    env.execute()
  }

}
