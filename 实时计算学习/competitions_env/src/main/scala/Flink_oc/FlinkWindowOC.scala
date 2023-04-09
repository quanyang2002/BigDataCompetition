package Flink_oc

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{EventTimeSessionWindows, ProcessingTimeSessionWindows, SlidingEventTimeWindows, SlidingProcessingTimeWindows, TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.table.functions.AggregateFunction

object FlinkWindowOC {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    // 设置时间语义
    // 将时间特性设置为 事件时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    // 将时间特性设置为 处理时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)

    // 获取测试流
    val stream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\实时计算学习\\competitions_env\\src\\main\\scala\\FlinkTest\\ds.txt")

    // 设置侧输出流 编号为late
    val lateData = new OutputTag[(String,String)]("late")
    // 对数据流进行处理
    val processStream = stream.map(data => (data.split(" ")(0),data.split(" ")(1)))
      .keyBy(_._1)
      // 窗口分配器
      // 1、设置基于事件时间语义的滚动窗口 窗口大小为5秒 调整为我国时区 默认时区为GMT
//      .window(TumblingEventTimeWindows.of(Time.seconds(5),Time.hours(-8))
      // 2、设置基于处理时间语义的滚动窗口
//      .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
      // 3、设置基于事件时间语义的滑动窗口 窗口大小为10秒 滑动步长为5秒
//      .window(SlidingEventTimeWindows.of(Time.seconds(10),Time.seconds(5)))
      // 4、设置基于处理时间的滑动窗口
//      .window(SlidingProcessingTimeWindows.of(Time.seconds(10),Time.seconds(5)))
      // 5、简单设置滚动窗口 窗口大小为10秒 这里的窗口时间语义根据环境设置应用
//      .timeWindow(Time.seconds(10))
      // 6、简单设置滑动窗口 窗口大小为10秒 滑动步长为5秒
      .timeWindow(Time.seconds(10),Time.seconds(5))
      // 7、设置基于事件时间语义的会话窗口 设置会话间隙为10秒
//      .window(EventTimeSessionWindows.withGap(Time.seconds(10)))
      // 8、设置基于处理时间语义的会话窗口，设置会话间隙为10秒
//      .window(ProcessingTimeSessionWindows.withGap(Time.seconds(10)))
    // 处理迟到数据
      // 设置允许处理迟到数据 最大迟到时间为2秒
      .allowedLateness(Time.seconds(2L))
      // 将迟到数据 抛出到 侧输出流
      .sideOutputLateData(lateData)
      // 窗口计算函数
      // reduceFunction:定义了对两个同类型元素的聚合操作 聚合结果与之前元素类型不变
//      .reduce((数据元素1:Tuple,数据元素2:Tuple) => (数据元素1._1,数据元素1._2 + 数据元素2._2))
      // aggregateFunction:定义带有中间状态的聚合计算 效率高于reduceFunction
//      .aggregate(new MyAggregateFunction)


    // 获取侧输出流
//    val lateStream = processStream.getSideOutput(lateData)

    // 执行流处理
    env.execute()
  }
// AggregateFunction
//  class MyAggregateFunction extends AggregateFunction[输出结果类型,中间状态类型] {
//    // 重写获取最后结果方法
//    override def getValue(acc: 中间状态类型): 输出结果类型 = ???
//    // 重写创建累加器方法
//    override def createAccumulator(): 中间状态类型 = ???
//  }

}
