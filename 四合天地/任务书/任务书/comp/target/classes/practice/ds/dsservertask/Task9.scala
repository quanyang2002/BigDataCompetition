package practice.ds.dsservertask

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Properties

object Task9 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    env.setParallelism(1)

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","earliest")

    val stream = env.addSource(new FlinkKafkaConsumer[(String)]("ods_mall_data", new SimpleStringSchema(), kafkaProp))

    // 获取两条数据流
    val masterStream = stream.filter(_.contains("order_master"))
    val detailStream = stream.filter(_.contains("order_detail"))

    // 对两条流进行相应信息的提取
    val masterMapStream = masterStream.map(data => {
      val nObject = JSON.parseObject(JSON.parseObject(data).getString("data"))
      (nObject.getString("order_sn"), nObject.getString("order_money").toDouble,{
        val format = new SimpleDateFormat("yyyymmddhhmmss")
        val time = format.parse(nObject.getString("create_time")).getTime
        time
      })
    }).assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(0))
    .withTimestampAssigner(new SerializableTimestampAssigner[(String,Double,Long)] {
      override def extractTimestamp(t: (String, Double, Long), l: Long): Long = t._3
    }))
      .keyBy(0)
//      .timeWindow(Time.seconds(5))
      .reduce((a,b) => (a._1,a._2+b._2,{
        if (a._3 < b._3){
          a._3
        }else{
          b._3
        }
      }))
    val detailMapStream = detailStream.map(data => {
      val nObject = JSON.parseObject(JSON.parseObject(data).getString("data"))
      (nObject.getString("order_sn"), nObject.getString("product_cnt").toInt,{
        val format = new SimpleDateFormat("yyyymmddhhmmss")
        val time = format.parse(nObject.getString("create_time")).getTime
        time
      })
    }).assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(0))
    .withTimestampAssigner(new SerializableTimestampAssigner[(String,Int,Long)] {
      override def extractTimestamp(t: (String, Int, Long), l: Long): Long = t._3
    }))
      .keyBy(0)
//      .timeWindow(Time.seconds(5))
      .reduce((a,b) => (a._1,a._2+b._2,{
        if (a._3 < b._3){
          a._3
        }else{
          b._3
        }
      }))
    // 两条流join
    masterMapStream
      .join(detailMapStream)
      .where(_._1)
      .equalTo(_._1)
      .window(TumblingEventTimeWindows.of(Time.seconds(10)))
      .apply(new JoinFunction[(String,Double,Long),(String,Int,Long),String]{
        override def join(in1: (String,Double,Long), in2: (String,Int,Long)): String = {
          var result = new StringBuilder()
          result.append(in1._1 + "," + in1._2 + "," + in2._2)
          result.toString()
        }
      }).print()
//      .addSink(new FlinkKafkaProducer[String]("Task9",new SimpleStringSchema(),kafkaProp))

    env.execute("双流join")
  }

}
