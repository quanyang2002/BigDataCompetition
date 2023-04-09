package module_d

import com.ibm.icu.text.SimpleDateFormat
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

import java.util.Date
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`

/**
 * 编写Scala工程代码，使用Flink消费Kafka中的数据并进行相应的数据统计计算。
 */
object task7 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1) //并行度


    val source2 = KafkaSource.builder()
      .setBootstrapServers("101.35.193.165:9092")
      .setTopics("ChangeRecord")
      .setGroupId("my-group")
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .setStartingOffsets(OffsetsInitializer.latest())
      .build()
    //原始流
    val stream2 = env.fromSource(source2, WatermarkStrategy.noWatermarks(), "Kafka Source2")

    val conf = new FlinkJedisPoolConfig.Builder()
      .setHost("ngc")
      .setPort(6378)
      .setPassword("123456")
      .build()
    /**
     * 2、使用Flink消费Kafka中ChangeRecord主题的数据，实时统计每个设备从其他状态转变为“运行”状态的总次数，
     * 将结果存入MySQL数据库shtd_industry的change_state_other_to_run_agg表中（表结构如下）。请将任务
     * 启动命令截图，启动1分钟后根据change_machine_id降序查询change_state_other_to_run_agg表并截图，
     * 启动两分钟后根据change_machine_id降序查询change_state_other_to_run_agg表并再次截图；
     * 注：时间语义使用Processing Time。
     */

    val ds2 = stream2
      .map(line => {
        val arr = line.split(",")
        (arr(1), arr(3))
      })
      .keyBy(_._1)
      .process(new KeyedProcessFunction[String, (String, String), (String, String, Int, String)] {
        // 声明列表状态_.1上一次的状态，_.2状态为运行的次数
        val valueState: ValueState[(String, Int)] = getRuntimeContext.getState(new ValueStateDescriptor[(String, Int)]("value-count", classOf[(String, Int)]))

        override def processElement(i: (String, String), context: KeyedProcessFunction[String, (String, String), (String, String, Int, String)]#Context, collector: Collector[(String, String, Int, String)]): Unit = {

          var value = valueState.value()
          if (value == null || value._1 == null) value = ("待机", 0)
          if (i._2.equals("运行")) {
            valueState.update(value._1, value._2 + 1)
            val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentTime = context.timerService().currentProcessingTime()
            collector.collect((i._1, value._1, value._2, sdf.format(new Date(currentTime))))
          } else {
            valueState.update(i._2, value._2)
          }
        }

      })

    /**
     * 3、使用Flink消费Kafka中ChangeRecord主题的数据，每隔1分钟输出最近3分钟的预警次数最多的设备，
     * 将结果存入Redis中，key值为“warning_last3min_everymin_out”，value值为“窗口结束时间，设
     * 备id”（窗口结束时间格式：yyyy-MM-dd HH:mm:ss）。使用redis cli以HGETALL key方式获取
     * warning_last3min_everymin_out值，将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二
     * 次截图间隔1分钟以上，第一次截图放前面，第二次截图放后面。
     */
    val ds3 = stream2
      .filter(line => line.split(",")(6).equals("0")) //取ChangeHandleStatue这列，实际以预警字段为准
      .map(line => {
        val arr = line.split(",")
        (arr(1), 1)
      }).keyBy(_._1)
      .window(SlidingProcessingTimeWindows.of(Time.minutes(3), Time.minutes(1)))
      .aggregate(new AggregateFunction[(String, Int), Int, Int] {
        override def createAccumulator(): Int = 0

        override def add(in: (String, Int), acc: Int): Int = acc + in._2

        override def getResult(acc: Int): Int = acc

        override def merge(acc: Int, acc1: Int): Int = ???
      }
        , new ProcessWindowFunction[Int, (String, Int, String), String, TimeWindow] {
          override def process(key: String, context: Context, elements: Iterable[Int], out: Collector[(String, Int, String)]): Unit = {
            val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            out.collect((key, elements.toList.head, sdf.format(new Date(context.window.getEnd))))
          }
        }
      )
      //按照窗口信息进行分组提取，排序输出
      .keyBy(_._3)
      //如果只是提取最大值，可以用以下的方法，其中reduce可用maxBy代替
      //      .reduce((a,b)=>{
      //        if(a._2>b._2) a else  b
      //      }).map(t3=>(t3._3,t3._1))
      //此方法适用于TopN问题
      .process(new KeyedProcessFunction[String, (String, Int, String), (String, String)] {
        // 声明列表状态
        var listState: ListState[(String, Int, String)] = _

        override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, (String, Int, String), (String, String)]#OnTimerContext, out: Collector[(String, String)]): Unit = {
          val head = listState.get().toList.maxBy(_._2)
          out.collect((head._3, head._1))
        }

        override def open(parameters: Configuration): Unit = {
          listState = getRuntimeContext.getListState(new ListStateDescriptor[(String, Int, String)]("listState", classOf[(String, Int, String)]))
        }

        override def processElement(i: (String, Int, String), context: KeyedProcessFunction[String, (String, Int, String), (String, String)]#Context, collector: Collector[(String, String)]): Unit = {
          // 每来一个数据，就直接放入ListState中
          listState.add(i)
          // 注册一个窗口结束时间1ms之后的定时器
          val currentTime = context.timerService().currentProcessingTime()
          context.timerService().registerProcessingTimeTimer(currentTime + 5 * 1000)
        }
      })

    ds3.addSink(new RedisSink[(String, String)](conf, new MyRedisMapper("warning_last3min_everymin_out")))

    ds2.print()
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
