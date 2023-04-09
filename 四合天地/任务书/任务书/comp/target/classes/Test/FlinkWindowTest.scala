package Test

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

object FlinkWindowTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val inputStream = env.socketTextStream("localhost", 9994)
    val mapStream = inputStream.map(data => (data.split(" ")(0), data.split(" ")(1).toLong))
      .assignAscendingTimestamps(_._2)
      .keyBy(_._1)
    mapStream.window(TumblingEventTimeWindows.of(Time.seconds(5)))
      .max(1).print()
    env.execute()
  }

}
