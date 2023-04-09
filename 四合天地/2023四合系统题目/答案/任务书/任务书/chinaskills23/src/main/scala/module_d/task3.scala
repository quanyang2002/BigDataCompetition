package module_d

import module_d.task1.MyRedisMapper
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.{KeyedProcessFunction, ProcessFunction}
import org.apache.flink.streaming.api.scala.{OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

import java.text.{DecimalFormat, SimpleDateFormat}
import java.time.Duration
import java.util.Properties
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`
import scala.collection.mutable.ListBuffer

/**
 * 编写Scala代码，使用Flink消费Kafka中Topic为order的数据并进行相应的数据统计计算（订单信息对应表结构order_info,订单详细信息对应表结构order_detail（来源类型和来源编号这两个字段不考虑，所以在实时数据中不会出现），同时计算中使用order_info或order_detail表中create_time或operate_time取两者中值较大者作为EventTime，若operate_time为空值或无此属性，则使用create_time填充，允许数据延迟5S，订单状态分别为1001:创建订单、1002:支付订单、1003:取消订单、1004:完成订单、1005:申请退回、1006:退回完成。另外对于数据结果展示时，不要采用例如：1.9786518E7的科学计数法）。
 */
object task3 {
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
     * 1、使用Flink消费Kafka中的数据，实时统计商城中消费额前2的用户
     * （需要考虑订单状态，若有取消订单、申请退回、退回完成则不计入订单消费额，其他的相加）
     * ，将key设置成top2userconsumption存入Redis中（value使用String数据格式，
     * value为前2的用户信息并且外层用[]包裹，其中按排序依次存放为该用户
     * id:用户名称:消费总额，用逗号分割，其中用户名称为user_info表中的name字段，
     * 可从MySQL中获取）。使用redis cli以get key方式获取top2userconsumption值，
     * 将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二次截图间隔1分钟以上，
     * 第一次截图放前面，第二次截图放后面（如有中文，需在redis-cli中展示中文）；
     * 示例如下：
     * top2userconsumption：[1:张三:10020,42:李四:4540]
     */
    val ds1 = streamProcess
      .getSideOutput(statusother)
      //      .map(line => (line.split(",")(0), line))
      .map(line => {
        val arr = line.split(",")
        (arr(0), arr(1), arr(3).toDouble)
      })
      .keyBy(_._1)
      //      .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .reduce((a, b) => (a._1, a._2, a._3 + b._3))
      //      .process(new ProcessFunction[(String,String,Double),String] {
      //        var listState: ListState[(String,String,Double)] = _
      //        override def open(parameters: Configuration): Unit = {
      //          listState = getRuntimeContext.getListState(new ListStateDescriptor[(String,String,Double)]("list-state", classOf[(String,String,Double)]))
      //        }
      //        override def processElement(i: (String, String, Double), context: ProcessFunction[(String, String, Double), String]#Context, collector: Collector[String]): Unit = {
      //          listState.add(i)
      //          val tuples = listState.get().toList.sortBy(-_._3).take(2)
      //          println(tuples)
      ////          context.timerService().registerProcessingTimeTimer(1000)
      //        }
      //        override def onTimer(timestamp: Long, ctx: ProcessFunction[(String, String, Double), String]#OnTimerContext, out: Collector[String]): Unit = {
      //        }
      //      })
      .keyBy(_ => "key")
//            .keyBy(_=>true)
      .process(new KeyedProcessFunction[String, (String, String, Double), String] {
        // 声明列表状态
        var listState: ListState[(String, String, Double)] = _

        override def open(parameters: Configuration): Unit = {
          listState = getRuntimeContext.getListState(new ListStateDescriptor[(String, String, Double)]("list-state", classOf[(String, String, Double)]))
        }

        override def processElement(i: (String, String, Double), context: KeyedProcessFunction[String, (String, String, Double), String]#Context, collector: Collector[String]): Unit = {
          listState.add(i)
          val currentTime = context.timerService().currentProcessingTime()
          context.timerService().registerProcessingTimeTimer(currentTime + 5 * 1000)
        }


        override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, (String, String, Double), String]#OnTimerContext, out: Collector[String]): Unit = {
          val map = listState.get()
            .toList
            .groupBy(n => (n._1, n._2))
            .mapValues(v => v.maxBy(_._2)._3)
          // 将 Map 中的 KV 键值对转换成列表数据结构
          // 列表中的元素是(K,V)元组
          var mapList = new ListBuffer[(String, String, Double)]()
          map.keys.foreach(
            k => map.get(k) match {
              case Some(count) => mapList += ((k._1, k._2, count))
              case None => mapList
            })
          val list = mapList.sortBy(-_._3)
          //top2
          val key1 = list.head._2
          val key2 = list(1)._2
          val value1 = new DecimalFormat("#.#").format(list.head._3)
          val value2 = new DecimalFormat("#.#").format(list(1)._3)
          out.collect(s"[1:${key1}:${value1},2:${key2}:${value2}]")
        }

      })


    //      .aggregate(new AggregateFunction[(String, String), (String, String, Double), (String, String, Double)] {
    //        override def createAccumulator(): (String, String, Double) = ("", "", 0)
    //
    //        override def add(in: (String, String), acc: (String, String, Double)): (String, String, Double) = {
    //          val arr = in._2.split(",")
    //          (in._1, arr(1), arr(3).toDouble + acc._3)
    //        }
    //
    //        override def getResult(acc: (String, String, Double)): (String, String, Double) = acc
    //
    //        override def merge(acc: (String, String, Double), acc1: (String, String, Double)): (String, String, Double) = ???
    //      }
    //        , new ProcessWindowFunction[(String, String, Double),  String, String, TimeWindow] {
    //          override def process(key: String, context: Context, elements: Iterable[(String, String, Double)], out: Collector[String]): Unit = {
    //          val list = elements.toList.sortBy(-_._3)
    //            println(list)
    ////            out.collect(s"[1:${list.head._2}:${list.head._3},42:${list(1)._2}:${list(1)._3}]")
    //            out.collect(s"[1:${list.head._2}:${list.head._3}]")
    //          }
    //        }
    //      )
    //redis配置
    val conf = new FlinkJedisPoolConfig.Builder()
      .setHost("ngc")
      .setPort(6378)
      .setPassword("123456")
      .build()
//    ds1.addSink(new RedisSink[String](conf, new MyRedisMapper("top2userconsumption")))
    /**
     * 2、在任务1进行的同时，使用侧边流，计算每分钟内状态为取消订单占所有订单的占比，
     * 将key设置成cancelrate存入Redis中，value存放取消订单的占比（为百分比，保留
     * 百分比后的一位小数，四舍五入，例如12.1%）。使用redis cli以get key方式获取c
     * ancelrate值，将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二次截图间隔
     * 1分钟以上，第一次截图放前面，第二次截图放后面；
     */

    val ds2 = stream
      .map(line => {
        val status = line.split(",")(4)
        if (status.equals("1003")) {
          ("1003", 1)
        } else {
          ("other", 1)
        }
      })
      .windowAll(TumblingEventTimeWindows.of(Time.minutes(1))) // 基于事件时间的滚动窗口
      .aggregate(new AggregateFunction[(String, Int), (Int, Int), Double] {
        override def createAccumulator(): (Int, Int) = (0, 0)

        // 累加规则
        override def add(in: (String, Int), acc: (Int, Int)): (Int, Int) = {
          if (in._1.equals("1003")) {
            (acc._1 + in._2, acc._2)
          } else {
            (acc._1, acc._2 + in._2)
          }
        }

        // 获取窗口关闭时向下游发送的结果
        override def getResult(acc: (Int, Int)): Double = {
          acc._1.toDouble / (acc._1 + acc._2)
        }

        // merge 方法只有在事件时间的会话窗口时，才需要实现，这里无需实现。
        override def merge(acc: (Int, Int), acc1: (Int, Int)): (Int, Int) = ???
      })
      .map(res => (res * 100).toString.format("%.1f")) //格式转换
//    ds2.addSink(new RedisSink[String](conf, new MyRedisMapper("cancelrate")))
    /**
     * 3、在任务1进行的同时，使用侧边流，使用Flink消费Kafka中的订单详细信息的数据
     * ，实时统计商城中销售额前3的商品（不考虑订单状态，不考虑打折，销售额为order_price*sku_num）
     * ，将key设置成top3itemconsumption存入Redis中（value使用String数据格式，value为前3的商
     * 品信息并且外层用[]包裹，其中按排序依次存放商品id:销售额，并用逗号分割）。使用redis cli以get
     * key方式获取top3itemconsumption值，将结果截图粘贴至对应报告中，需两次截图，第一次截图和第二
     * 次截图间隔1分钟以上，第一次截图放前面，第二次截图放后面。
     */
    val stream3 = env.addSource(new FlinkKafkaConsumer[String]("detail", new SimpleStringSchema(), properties))
    val ds3 = stream3.map(line => {
      val arr = line.split(",")
      (arr(2), arr(5).toDouble * arr(6).toInt)
    }).keyBy(_._1)
      .reduce((a, b) => (a._1, a._2 + b._2))
      .keyBy(_ => true)
      .process(new KeyedProcessFunction[Boolean, (String, Double), String] {
        var listState: ListState[(String, Double)] = _

        override def processElement(i: (String, Double), context: KeyedProcessFunction[Boolean, (String, Double), String]#Context, collector: Collector[String]): Unit = {
          listState.add(i)

          val currentTime = context.timerService().currentProcessingTime()
          context.timerService().registerProcessingTimeTimer(currentTime + 5 * 1000)
        }

        override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Boolean, (String, Double), String]#OnTimerContext, out: Collector[String]): Unit = {
          val map = listState.get()
            .toList
            .groupBy(_._1)
            .mapValues(v => v.maxBy(_._2)._2)
          // 将 Map 中的 KV 键值对转换成列表数据结构
          // 列表中的元素是(K,V)元组
          var mapList = new ListBuffer[(String, Double)]()
          map.keys.foreach(
            k => map.get(k) match {
              case Some(count) => mapList += ((k, count))
              case None => mapList
            })
          val list = mapList.sortBy(-_._2)
          val key1 = list.head._1
          val key2 = list(1)._1
          val key3 = list(2)._1
          val value1 = new DecimalFormat("#.#").format(list.head._2)
          val value2 = new DecimalFormat("#.#").format(list(1)._2)
          val value3 = new DecimalFormat("#.#").format(list(2)._2)
          out.collect(s"[${key1}:${value1},${key2},${value2},${key3}:${value3}]")
        }


        override def open(parameters: Configuration): Unit = {
          listState = getRuntimeContext.getListState(new ListStateDescriptor[(String, Double)]("list-state3", classOf[(String, Double)]))
        }
      })
//    ds3.addSink(new RedisSink[String](conf, new MyRedisMapper("top3itemconsumption")))
    ds1.print()
    //    ds2.print()
//    ds3.print()
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
