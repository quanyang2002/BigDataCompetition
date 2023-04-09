import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object WatermarkTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // 固定延迟水位线
    val source = env.socketTextStream("localhost", 9997)
//    source.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[String](Time.seconds(5)) {
//      override def extractTimestamp(t: String): Long ={
//
//      }
//    })
    // 单调递增水位线
//    source.assignAscendingTimestamps(某个字段)

    env.execute()
  }

}
