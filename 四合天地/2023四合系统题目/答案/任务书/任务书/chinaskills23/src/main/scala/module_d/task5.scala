package module_d

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.api.scala._

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Properties

/**
 * 编写Scala代码，使用Flink消费Kafka中Topic为order的数据并进行相应的数据统计计算（订单信息对应表结构order_info,订单详细信息对应表结构order_detail（来源类型和来源编号这两个字段不考虑，所以在实时数据中不会出现），同时计算中使用order_info或order_detail表中create_time或operate_time取两者中值较大者作为EventTime，若operate_time为空值或无此属性，则使用create_time填充，允许数据延迟5S，订单状态分别为1001:创建订单、1002:支付订单、1003:取消订单、1004:完成订单、1005:申请退回、1006:退回完成。另外对于数据结果展示时，不要采用例如：1.9786518E7的科学计数法）。
 */
object task5 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1) //并行度

    val properties = new Properties() //Kafka配置文件
    properties.setProperty("bootstrap.servers", "ngc:9092") //集群地址
    properties.setProperty("group.id", "g1") //消费者组
    /**
     * 3、采用双流JOIN的方式（本系统稳定，无需担心数据迟到与丢失的问题,建议使用滚动窗口），结合订单信息和订单详细信息（需要考虑订单状态，若有取消订单、申请退回、退回完成则不进行统计），拼接成如下表所示格式，其中包含订单id、订单总金额、商品数，将数据存入MySQL数据库shtd_result的orderpostiveaggr表中（表结构如下），然后在Linux的MySQL命令行中根据id降序排序，查询出前5条，将SQL语句与执行结果截图粘贴至对应报告中。
     */
    //原始流
    val stream1 = env.addSource(new FlinkKafkaConsumer[String]("order1", new SimpleStringSchema(), properties))
      .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[String](Duration.ofSeconds(5)) //允许数据延迟5S
        .withTimestampAssigner(
          new SerializableTimestampAssigner[String] {
            override def extractTimestamp(t: String, l: Long): Long = {
              val sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
              if (t.split(",")(11).equals("")) { //如果operate_time为空
                sdf.parse(t.split(",")(10)).getTime
              } else {
                val create_time = sdf.parse(t.split(",")(10)).getTime
                val operate_time = sdf.parse(t.split(",")(11)).getTime
                math.max(create_time, operate_time)
              }
            }
          }
        ))
    val stream2 = env.addSource(new FlinkKafkaConsumer[String]("detail", new SimpleStringSchema(), properties))
      .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[String](Duration.ofSeconds(5)) //允许数据延迟5S
        .withTimestampAssigner(
          new SerializableTimestampAssigner[String] {
            override def extractTimestamp(t: String, l: Long): Long = {
              val sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
              sdf.parse(t.split(",")(10)).getTime
            }
          }
        ))
    //提取相应对列名
    val ds3_1 = stream1.map(line => {
      val arr = line.split(",")
      val id = arr(0)
      val final_total_amount = arr(3)
      (id, final_total_amount)
    })
    val ds3_2 = stream2.map(line => {
      val arr = line.split(",")
      val id = arr(1)
      val sku_num = arr(6)
      (id, sku_num)
    })
    val ds3_3 = ds3_1
      .join(ds3_2)
      .where(_._1) //指定第一条流中元素的 key
      .equalTo(_._1) //指定第二条流中元素的 key
      .window(TumblingEventTimeWindows.of(Time.seconds(30))) //滚动窗口，时间不能太短
      .apply(new JoinFunction[(String, String), (String, String), (String, String, String)] {
        override def join(in1: (String, String), in2: (String, String)): (String, String, String) = {
          (in1._1, in1._2, in2._2)
        }
      })
    ds3_3.addSink(new RichSinkFunction[(String, String, String)] {
      var conn: Connection = _
      var insertStmt: PreparedStatement = _

      override def open(parameters: Configuration): Unit = {
        conn = DriverManager.getConnection("jdbc:mysql://ngc:3307/shtd_result?useSSL=false", "root", "123456")
        insertStmt = conn.prepareStatement("insert into orderpostiveaggr values (?,?,?)")
      }

      override def close(): Unit = {
        insertStmt.close()
        conn.close()
      }

      override def invoke(value: (String, String, String), context: SinkFunction.Context): Unit = {
        insertStmt.setInt(1, value._1.toInt)
        insertStmt.setDouble(2, value._2.toDouble)
        insertStmt.setInt(3, value._3.toInt)
        insertStmt.execute()
      }
    })
    //    ds3_1.print()
    //    ds3_2.print()
    ds3_3.print()
    env.execute("kafka sink test")
  }


}
