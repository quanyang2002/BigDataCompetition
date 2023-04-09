package Test

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.collection.JavaConversions.mapAsJavaMap
import scala.collection.mutable.ListBuffer

object FlinkTopN {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val sourceStream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\Test\\test_words")
    val mapStream = sourceStream
      .map(data => {
        (data.split(",")(1), data.split(",")(0).toInt, data.split(",")(2).toLong)
      })
      .assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps()
        .withTimestampAssigner(new SerializableTimestampAssigner[(String, Int, Long)] {
          override def extractTimestamp(t: (String, Int, Long), l: Long): Long = t._3
        }))
    mapStream
      .windowAll(TumblingEventTimeWindows.of(Time.seconds(10)))
      .process(new ProcessAllWindowFunction[(String,Int,Long),String,TimeWindow] {
        override def process(context: Context, elements: Iterable[(String, Int, Long)], out: Collector[String]): Unit = {
          var mapList = new ListBuffer[(String,Int)]
          var productCountMap = Map[String,Int]()
          elements.foreach(
            el => {
              productCountMap.put(el._1,el._2)
            }
          )
          productCountMap.keys.foreach(
            k => productCountMap.get(k) match {
              case Some(count) => mapList += ((k,count))
              case None => mapList
            }
          )
          mapList.sortBy(-_._2)
          var result = new StringBuilder
          var i = 0
          for (i <- 0 to result.length){
            result.append(mapList(i)._1 + ":" +mapList(i)._2)
          }
          out.collect(result.toString())
        }
      }).print()
    env.execute()
  }
}
