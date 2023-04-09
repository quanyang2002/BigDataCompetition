package Test

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

import java.text.SimpleDateFormat
import java.time.Duration
import scala.collection.JavaConversions.iterableAsScalaIterable

object Task9Test {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val source = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\sources\\data_merge.txt")
    val masterSource = source.filter(_.contains("order_master"))
    val detailSource = source.filter(_.contains("order_detail"))
    val masterMap = masterSource
      .map(data => {
        val value = JSON.parseObject(data)
        val nObject = JSON.parseObject(value.getString("data"))
        val sn = nObject.getString("order_sn")
        val order_money = nObject.getString("order_money").toDouble
        val str = nObject.getString("create_time")
        (sn, order_money,str)
      })
      .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5))
      .withTimestampAssigner(new SerializableTimestampAssigner[(String,Double,String)] {
        override def extractTimestamp(t: (String, Double, String), l: Long): Long = {
          val simpleFormat = new SimpleDateFormat("yyyyMMddhhmmss")
          val date = simpleFormat.parse(t._3).getTime
          date
        }
      }))
      .keyBy(0)
      .sum(1)
    val detailMap = detailSource
      .map(data => {
        val nObject = JSON.parseObject(data)
        val nObject1 = JSON.parseObject(nObject.getString("data"))
        val sn = nObject1.getString("order_sn")
        val order_cnt = nObject1.getString("product_cnt").toInt
        val str = nObject1.getString("create_time")
        (sn, order_cnt,str)
      })
      .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5L))
      .withTimestampAssigner(new SerializableTimestampAssigner[(String,Int,String)] {
        override def extractTimestamp(t: (String, Int, String), l: Long): Long = {
          val simpleFormat = new SimpleDateFormat("yyyyMMddhhmmss")
          val time = simpleFormat.parse(t._3).getTime
          time
        }
      }))
      .keyBy(0)
      .sum(1)
    masterMap
      .join(detailMap)
      .where(_._1)
      .equalTo(_._1)
      .window(TumblingEventTimeWindows.of(Time.seconds(60)))
      .apply(new JoinFunction[(String,Double,String),(String,Int,String),(String,Double,Int)] {
        override def join(in1: (String, Double,String), in2: (String, Int,String)): (String, Double, Int) = {
          (in1._1,in1._2,in2._2)
        }
      }).print()

    env.execute()
  }

}
