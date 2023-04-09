package Test

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

object TopNTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val sourceStream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\Test\\test_words")
    sourceStream.map(data => (data,1))
      .keyBy(_._1).sum(1)
      .map(data => (1,data._1,data._2))
      .keyBy(_._1)
      .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(4)))



    env.execute()
  }

}
