package practices.dsTask

import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

import java.util.Properties

object test {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    val stream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\tes")
    val mapStream = stream.map(data => {
      val nObject = JSON.parseObject(data)
      nObject.getString("data")
    })
    mapStream.print()
//    val kafkaProp = new Properties()
//    kafkaProp.setProperty("bootstrap.servers","101.35.193.165:9092")
//    kafkaProp.setProperty("auto.offset","earliest")

//    val kafkaStream = env.addSource(new FlinkKafkaConsumer[String]("order1", new SimpleStringSchema(), kafkaProp))
//    kafkaStream.print()

    env.execute()
  }

}
