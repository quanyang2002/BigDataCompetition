package module_d

import com.ibm.icu.text.SimpleDateFormat
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._
import java.util.Date

/**
 * 编写Scala工程代码，使用Flink消费Kafka中的数据并进行相应的数据统计计算。
 */
object task8 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1) //并行度


    val source3 = KafkaSource.builder()
      .setBootstrapServers("101.35.193.165:9092")
      .setTopics("EnvironmentData")
      .setGroupId("my-group")
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .setStartingOffsets(OffsetsInitializer.latest())
      .build()
    //原始流
    val stream3 = env.fromSource(source3, WatermarkStrategy.noWatermarks(), "Kafka Source2")


    val conf = new FlinkJedisPoolConfig.Builder()
      .setHost("ngc")
      .setPort(6378)
      .setPassword("123456")
      .build()
    /**
     * 使用Flink消费Kafka中EnvironmentData主题的数据,监控各环境检测设备数据
     * ，当温度（Temperature字段）持续3分钟高于38度时记录为预警数据，将结果存
     * 入Redis中，key值为“env_temperature_monitor”，value值为“设备id-预
     * 警信息生成时间，预警信息”（预警信息生成时间格式：yyyy-MM-dd HH:mm:ss）。
     * 使用redis cli以HGETALL key方式获取env_temperature_monitor值，将结果
     * 截图粘贴至对应报告中，需要两次截图间隔3分钟以上，第一次截图放前面，第二次截
     * 图放后面
     */

    val ds1 = stream3
      .map(line => {
        val arr = line.split(",")
        (arr(1), arr(5).toDouble)
      })
      .keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.minutes(3)))
      .aggregate(new AggregateFunction[(String, Double), Double, Double] {
        override def createAccumulator(): Double = 100

        override def add(in: (String, Double), acc: Double): Double = if (in._2 < acc) in._2 else acc

        override def getResult(acc: Double): Double = acc

        override def merge(acc: Double, acc1: Double): Double = ???
      }
        , new ProcessWindowFunction[Double, (String, String), String, TimeWindow] {
          override def process(key: String, context: Context, elements: Iterable[Double], out: Collector[(String, String)]): Unit = {
            if (elements.head >= 37) {
              val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
              val k = key + "-" + sdf.format(new Date(context.window.getEnd))
              val v = s"设备${key}连续三分钟温度高于37度请及时处理！"
              out.collect(k, v)
            }

          }
        }
      )

    ds1.print()

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
