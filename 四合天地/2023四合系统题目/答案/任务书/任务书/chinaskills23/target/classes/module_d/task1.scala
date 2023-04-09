package module_d

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.{OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.text.{DecimalFormat, SimpleDateFormat}
import java.time.Duration
import java.util.Properties

/**
 * 编写Scala代码，使用Flink消费Kafka中Topic为order的数据并进行相应的数据统计计算（订单信息对应表结构order_info,订单详细信息对应表结构order_detail（来源类型和来源编号这两个字段不考虑，所以在实时数据中不会出现），同时计算中使用order_info或order_detail表中create_time或operate_time取两者中值较大者作为EventTime，若operate_time为空值或无此属性，则使用create_time填充，允许数据延迟5S，订单状态分别为1001:创建订单、1002:支付订单、1003:取消订单、1004:完成订单、1005:申请退回、1006:退回完成。另外对于数据结果展示时，不要采用例如：1.9786518E7的科学计数法）。
 */
object task1 {
  /**
   * 一个流分成四个流
   */
  lazy val statusother: OutputTag[String] = new OutputTag[String]("other")
  lazy val status1003: OutputTag[String] = new OutputTag[String]("s1003")
  lazy val status1005: OutputTag[String] = new OutputTag[String]("s1005")
  lazy val status1006: OutputTag[String] = new OutputTag[String]("s1006")

  def main(args: Array[String]): Unit = {
    /**
     * 1、使用Flink消费Kafka中的数据，统计商城实时订单实收金额（需要考虑订单状态，若有取消订单、申请退回、退回完成则不计入订单实收金额，其他状态的则累加），将key设置成totalprice存入Redis中。使用redis cli以get key方式获取totalprice值，将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二次截图间隔1分钟以上，第一次截图放前面，第二次截图放后面；
     */
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1) //并行度


    //Kafka配置
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "101.35.193.165:9092") //集群地址
    properties.setProperty("group.id", "g1") //消费者组

    //原始流
    val stream = env.addSource(new FlinkKafkaConsumer[String]("order1", new SimpleStringSchema(), properties).setStartFromLatest())
      .assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness[String](Duration.ofSeconds(5))//允许数据延迟5S
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
    //设置自定义侧边流
    val streamProcess = stream.process(new MdSplitProcessFunction)
    /**
     * 1、使用Flink消费Kafka中的数据，统计商城实时订单实收金额（需要考虑订单状态，若有取消订单、申请退回、
     * 退回完成则不计入订单实收金额，其他状态的则累加），将key设置成totalprice存入Redis中。使用redis
     * cli以get key方式获取totalprice值，将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二次截图
     * 间隔1分钟以上，第一次截图放前面，第二次截图放后面；
     */
    val ds1 = streamProcess
      .getSideOutput(statusother)
      .map(line => line.split(",")(3).toDouble)
      .keyBy(_ => true) //聚合到一起
      .sum(0)
      .map(n=>new DecimalFormat("#.#").format(n))
    //redis配置
    val conf = new FlinkJedisPoolConfig.Builder()
      .setHost("101.35.193.165")
      .setPort(6378)
      .setPassword("123456")
      .build()
//    ds1.addSink(new RedisSink[String](conf, new MyRedisMapper("totalcount")))
    /**
     * 2、在任务1进行的同时，使用侧边流，监控若发现order_status字段为退回完成, 将key设置成totalrefundordercount存入Redis中，value存放用户退款消费额。使用redis cli以get key方式获取totalrefundordercount值，将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二次截图间隔1分钟以上，第一次截图放前面，第二次截图放后面；
     */
    val ds2 = streamProcess
      .getSideOutput(status1006)
      .map(line => line.split(",")(3).toDouble)
      .keyBy(_ => true) //聚合到一起
      .sum(0)
      .map(n=>new DecimalFormat("#.#").format(n))
    ds2.addSink(new RedisSink[String](conf, new MyRedisMapper("totalrefundordercount")))

    /**
     * 3、在任务1进行的同时，使用侧边流，监控若发现order_status字段为取消订单,将数据存入MySQL数据库shtd_result的order_info表中，然后在Linux的MySQL命令行中根据id降序排序，查询列id、consignee、consignee_tel、final_total_amount、feight_fee，查询出前5条，将SQL语句与执行结果截图粘贴至对应报告中。
     */
    val ds3 = streamProcess
      .getSideOutput(status1003)

    ds3.addSink(new RichSinkFunction[String] {
      var conn: Connection = _
      var insertStmt: PreparedStatement = _

      override def open(parameters: Configuration): Unit =  {
        conn = DriverManager.getConnection("jdbc:mysql://101.35.193.165:3307/shtd_result?useSSL=false", "root", "123456")
        insertStmt = conn.prepareStatement("insert into order_info (id,consignee,consignee_tel,final_total_amount,feight_fee) values (?,?,?,?,?)")
      }

      override def close(): Unit = {
        insertStmt.close()
        conn.close()
      }

      override def invoke(value: String, context: SinkFunction.Context): Unit = {
        val arr = value.split(",")
        insertStmt.setString(1, arr(0))
        insertStmt.setString(2, arr(1))
        insertStmt.setString(3, arr(2))
        insertStmt.setString(4, arr(3))
        insertStmt.setString(5, arr(19))
        insertStmt.execute()
      }
    })

    ds1.print()
//    ds2.print()
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

  /**
   * Redis key——value存储 也可用RichSinkFunction建立Redis
   */
  class MyRedisMapper(key: String) extends RedisMapper[String] {

    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getValueFromData(data: String): String = data

    override def getKeyFromData(data: String): String = key
  }


}
