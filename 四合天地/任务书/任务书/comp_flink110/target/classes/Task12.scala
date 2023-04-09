import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.util.Properties

object Task12 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers","192.168.3.55:9092")
    kafkaProp.setProperty("auto.offset.reset","latest")
    kafkaProp.setProperty("group.id","g1")

//    val source = env.addSource(new FlinkKafkaConsumer[(String)]("ods_mall_data", new SimpleStringSchema(), kafkaProp))
//    val loginSource = env.addSource(new FlinkKafkaConsumer[(String)]("ods_mall_log", new SimpleStringSchema(), kafkaProp))
    // Task1
    val source = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp_flink110\\src\\main\\scala\\sources\\data_merge.txt")
    val masterStream = source.filter(_.contains("order_master"))
    val detailStream = source.filter(_.contains("order_detail"))
    masterStream
      .map(data => {
        val nObject = JSON.parseObject(data)
        nObject.getString("data")
      }).print("order_master")
    detailStream
      .map(data => {
        val nObject = JSON.parseObject(data)
        val str = nObject.getString("data")
        str
      }).print("order_detail")
    // Task2
//    loginSource.filter(_.contains("customer_login_log"))
//      .addSink(new FlinkKafkaProducer[(String)]("dim_customer_login_log",new SimpleStringSchema(),kafkaProp))

    env.execute()
  }

}
