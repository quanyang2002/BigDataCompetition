package module_d

import module_d.task1.MyRedisMapper
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.{SlidingEventTimeWindows, TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

import java.text.{DecimalFormat, SimpleDateFormat}
import java.time.Duration
import java.util.Properties

/**
 * 编写Scala代码，使用Flink消费Kafka中Topic为order的数据并进行相应的数据统计计算（订单信息对应表结构order_info,订单详细信息对应表结构order_detail（来源类型和来源编号这两个字段不考虑，所以在实时数据中不会出现），同时计算中使用order_info或order_detail表中create_time或operate_time取两者中值较大者作为EventTime，若operate_time为空值或无此属性，则使用create_time填充，允许数据延迟5S，订单状态分别为1001:创建订单、1002:支付订单、1003:取消订单、1004:完成订单、1005:申请退回、1006:退回完成。另外对于数据结果展示时，不要采用例如：1.9786518E7的科学计数法）。
 */
object task2 {
  lazy val statusother: OutputTag[String] = new OutputTag[String]("other")
  lazy val status1003: OutputTag[String] = new OutputTag[String]("s1003")
  lazy val status1005: OutputTag[String] = new OutputTag[String]("s1005")
  lazy val status1006: OutputTag[String] = new OutputTag[String]("s1006")

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1) //并行度

    val properties = new Properties() //Kafka配置文件
    properties.setProperty("bootstrap.servers", "101.35.193.165:9092") //集群地址
    properties.setProperty("group.id", "g1") //消费者组

    //原始流
    val stream = env.addSource(new FlinkKafkaConsumer[String]("order1", new SimpleStringSchema(), properties))
      .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[String](Duration.ofSeconds(5)) //允许数据延迟5S
        .withTimestampAssigner(
          new SerializableTimestampAssigner[String] {
            override def extractTimestamp(t: String, l: Long): Long = {
              val sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
              if (t.split(",")(11).equals("")) { //如果operate_time为空

                /**
                 * string to date to string
                 */
                  val string =t.split(",")(10)
                val sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                val sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date= sdf1.parse(string)
                val str = sdf2.format(date)

                sdf.parse(t.split(",")(10)).getTime//string to date to long
              } else {
                val create_time = sdf.parse(t.split(",")(10)).getTime
                val operate_time = sdf.parse(t.split(",")(11)).getTime
                math.max(create_time, operate_time)
              }
            }
          }
        ))
    //设置自定义侧边流
    val streamProcess = stream.process(new MdSplitProcessFunction)
    /**
     * 1、使用Flink消费Kafka中的数据，统计商城实时订单数量（需要考虑订单状态，若有取消订单、
     * 申请退回、退回完成则不计入订单数量，其他状态则累加），将key设置成totalcount存入Redis中
     * 。使用redis cli以get key方式获取totalcount值，将结果截图粘贴至对应报告中，需两次截图，
     * 第一次截图和第二次截图间隔1分钟以上，第一次截图放前面，第二次截图放后面；
     */
    val ds1 = streamProcess
      .getSideOutput(statusother)
      .map(_ => 1)
      .keyBy(_ => true) //聚合到一起
      .sum(0)
      .map(_.toString())

    //redis配置
    val conf = new FlinkJedisPoolConfig.Builder()
      .setHost("ngc")
      .setPort(6378)
      .setPassword("123456")
      .build()
    ds1.addSink(new RedisSink[String](conf, new MyRedisMapper("totalcount")))
    /**
     * 2、在任务1进行的同时，使用侧边流，统计每分钟申请退回订单的数量，将key设置成
     * refundcountminute存入Redis中。使用redis cli以get key方式获取refundcountminute值，
     * 将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二次截图间隔1分钟以上，第一次截图放前面
     * ，第二次截图放后面；
     */
    val ds2 = streamProcess
      .getSideOutput(status1005)
      .map(_ => ("key", 1))
      .windowAll(TumblingEventTimeWindows.of(Time.minutes(1))) // 基于处理时间的滚动窗口
      .reduce((a, b) => (a._1, a._2 + b._2))
      .map(_._2.toString())
//    ds2.addSink(new RedisSink[String](conf, new MyRedisMapper("refundcountminute")))
    /**
     * 3、在任务1进行的同时，使用侧边流，计算每分钟内状态为取消订单占所有订单的占比，
     * 将key设置成cancelrate存入Redis中，value存放取消订单的占比（为百分比，保留百
     * 分比后的一位小数，四舍五入，例如12.1%）。使用redis cli以get key方式获取cancelrate值
     * ，将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二次截图间隔1分钟以上，第一次截图
     * 放前面，第二次截图放后面。
     */
    val ds3 = stream
      .map(line => {
        val status = line.split(",")(4)
        if (status.equals("1003")) {
          ("1003", 1)
        } else {
          ("other", 1)
        }
      })
      .windowAll(TumblingEventTimeWindows.of(Time.minutes(1))) // 由于数据量少，Event时间结果为0%，换成processing就有了
      .aggregate(new AggregateFunction[(String, Int), (Int, Int), Double] {
        override def createAccumulator(): (Int, Int) = (0, 0)

        // 累加规则
        override def add(in: (String, Int), acc: (Int, Int)): (Int, Int) = {
          if (in._1.equals("1003")) {
            (acc._1 + 1, acc._2)
          } else {
            (acc._1, acc._2 + 1)
          }
        }

        // 获取窗口关闭时向下游发送的结果
        override def getResult(acc: (Int, Int)): Double = {
          acc._1.toDouble / (acc._1 + acc._2)
        }

        // merge 方法只有在事件时间的会话窗口时，才需要实现，这里无需实现。
        override def merge(acc: (Int, Int), acc1: (Int, Int)): (Int, Int) = ???
      })
      .map(n => new DecimalFormat("#.#%").format(n))

//    ds3.addSink(new RedisSink[String](conf, new MyRedisMapper("cancelrate")))
//    stream.print()
//        ds1.print()
//        ds2.print()
        ds3.print()

    env.execute("kafka sink test")
  }


  /**
   * 自定义侧边流配置
   */
  class MdSplitProcessFunction extends ProcessFunction[String, String] {
    override def processElement(value: String, ctx: ProcessFunction[String, String]#Context, out: Collector[String]): Unit = {
      val line = value.split(",")

      /**
       * 订单状态order_status分别为1001:创建订单、1002:支付订单、1003:取消订单、1004:完成订单、1005:申请退回、1006:退回完成。
       */
      if (line(4).equals("1003")) {
        ctx.output(status1003, value)
      } else if (line(4).equals("1005")) {
        ctx.output(status1005, value)
      } else if (line(4).equals("1006")) {
        ctx.output(status1006, value)
      } else {
        ctx.output(statusother, value)
      }

    }


  }


}
