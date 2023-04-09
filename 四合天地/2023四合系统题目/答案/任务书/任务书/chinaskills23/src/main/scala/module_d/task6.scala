package module_d

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

import java.text.SimpleDateFormat
import java.util.Date

/**
 * 编写Scala工程代码，使用Flink消费Kafka中的数据并进行相应的数据统计计算。
 */
object task6 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1) //并行度

    val source1 = KafkaSource.builder()
      .setBootstrapServers("101.35.193.165:9092")
      .setTopics("ProduceRecord")
      .setGroupId("my-group")
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .setStartingOffsets(OffsetsInitializer.latest()) //从最新的开始读
      .build()
    val source2 = KafkaSource.builder()
      .setBootstrapServers("101.35.193.165:9092")
      .setTopics("ChangeRecord")
      .setGroupId("my-group")
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .setStartingOffsets(OffsetsInitializer.latest())
      .build()
    //原始流
    val stream1 = env.fromSource(source1, WatermarkStrategy.noWatermarks(), "Kafka Source1")

    val stream2 = env.fromSource(source2, WatermarkStrategy.noWatermarks(), "Kafka Source2")

    //stream.print()
    /**
     * 1、使用Flink消费Kafka中ProduceRecord主题的数据，统计在已经检验的产品中，各设备每5分钟生产产品总数，将结果存入Redis中，key值为“totalproduce”，value值为“设备id，最近5分钟生产总数”。使用redis cli以HGETALL key方式获取totalproduce值，将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二次截图间隔5分钟以上，第一次截图放前面，第二次截图放后面；
     */
    //提取相应对列名
    val ds1 = stream1.map(line => {
      val arr = line.split(",")
      val id = arr(1)
      (id, 1)
    }).keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(5))) //时间语义使用Processing Time
      .reduce((a, b) => (a._1, a._2 + b._2))
      .map(tuple => (tuple._1, tuple._2.toString))
    val conf = new FlinkJedisPoolConfig.Builder()
      .setHost("ngc")
      .setPort(6378)
      .setPassword("123456")
      .build()
//    ds1.addSink(new RedisSink[(String, String)](conf, new MyRedisMapper("totalproduce")))
    /**
     * 2、使用Flink消费Kafka中ChangeRecord主题的数据，当某设备30秒状态连续为“预警”，输出预警信息。当前预警信息输出后，
     * 最近30秒不再重复预警（即如果连续1分钟状态都为“预警”只输出两次预警信息），将结果存入Redis中，key值为“warning30sMachine”，
     * value值为“设备id，预警信息”。使用redis cli以HGETALL key方式获取warning30sMachine值，将结果截图粘贴至对应报告中，
     * 需两次截图，第一次截图和第二次截图间隔1分钟以上，第一次截图放前面，第二次截图放后面；
     * 注：时间使用change_start_time字段，忽略数据中的change_end_time不参与任何计算。忽略数据迟到问题。
     * Redis的value示例：115,2022-01-01 09:53:10:设备115 连续30秒为预警状态请尽快处理！
     * 1。事件时间（change_start_time）语义下30s的滚动窗口keyBy设备ID
     */
    val ds2 = stream2
      .assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps[String]()
        .withTimestampAssigner(
          new SerializableTimestampAssigner[String] {
            override def extractTimestamp(t: String, l: Long): Long = {
              val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
              sdf.parse(t.split(",")(4)).getTime
            }
          }
        ))
      .map(line => {
        val arr = line.split(",")
        (arr(1), arr(3))
      })
      .keyBy(_._1)
      .window(TumblingEventTimeWindows.of(Time.seconds(30)))
      .aggregate(new AggregateFunction[(String, String), Set[String], Set[String]] {
        override def createAccumulator(): Set[String] = Set[String]()

        override def add(in: (String, String), acc: Set[String]): Set[String] = acc + in._2

        override def getResult(acc: Set[String]): Set[String] = acc

        override def merge(acc: Set[String], acc1: Set[String]): Set[String] = ???
      }
        , new ProcessWindowFunction[Set[String], (String, String), String, TimeWindow] {
          override def process(key: String, context: Context, elements: Iterable[Set[String]], out: Collector[(String, String)]): Unit = {
            val list = elements.toList
            for (set <- list) {
              //这里设置待机为提醒
              if (set.size == 1 && set.head.equals("待机")) {
                val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                //Redis的value示例：115,2022-01-01 09:53:10:设备115 连续30秒为预警状态请尽快处理！
                //(2022-01-01 09:53:10 为change_start_time字段值，中文内容及格式必须为示例所示内容。)
                val value = s"${key},${sdf.format(new Date(context.window.getEnd))}:设备${key} 连续30秒为预警状态请尽快处理！"
                out.collect(key, value)
              }
            }
          }
        }
      )
//    ds2.addSink(new RedisSink[(String, String)](conf, new MyRedisMapper("warning30sMachine")))

    /**
     * 3、使用Flink消费Kafka中ChangeRecord主题的数据，统计每3分钟各设备状态为“预警”且未处
     * 理的数据总数，将结果存入MySQL数据库shtd_industry的threemin_warning_state_agg表中
     * （追加写入，表结构如下）。请将任务启动命令截图，启动且数据进入后按照设备id降序排序查询
     * threemin_warning_state_agg表进行截图，第一次截图后等待3分钟再次查询并截图,将结果截图粘贴至对应报告中。
     */
    val ds3 = stream2
      //      .filter(line => line.split(",")(6).equals("0"))
      .filter(line => line.split(",")(6).equals("0")) //过滤状态为“预警”且未处理（预警不知道是那个字段，根据实际情况在加个filter）
      .map(line => {
        val arr = line.split(",")
        (arr(1), 1)
      }).keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.minutes(3)))

      .aggregate(new AggregateFunction[(String, Int), Int, Int] {
        override def createAccumulator(): Int = 0

        override def add(in: (String, Int), acc: Int): Int = acc + 1

        override def getResult(acc: Int): Int = acc

        override def merge(acc: Int, acc1: Int): Int = ???
      }
        , new ProcessWindowFunction[Int, (String, Int, String), String, TimeWindow] {
          override def process(key: String, context: Context, elements: Iterable[Int], out: Collector[(String, Int, String)]): Unit = {
            val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            out.collect((key, elements.head, sdf.format(new Date(context.window.getEnd))))
          }
        }
      )
    //    存储到mysql看前面的任务书
    ds1.print()
//    ds2.print()
//    ds3.print()
    env.execute("kafka sink test")
  }

  /**
   * Redis HSet存储 也可用RichSinkFunction建立Redis
   */
  class MyRedisMapper(key: String) extends RedisMapper[(String, String)] {

    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.HSET, key)

    override def getKeyFromData(t: (String, String)): String = t._1

    override def getValueFromData(t: (String, String)): String = t._2
  }

}
