import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.extensions._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.{SlidingProcessingTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.util.Random

object Chapter03 extends App {

  // 使用createLocalEnvironmentWithWebUI可以在本地查看WebUI，在集群提交任务无需此方法
  val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI()
  // Flink的输入为Source，这里我们构建一个定义Source：C03Source
  val sourceDataStream = env.addSource(new C03Source())
  // 接下来以品类做为key，计算每个品类的总价格
  // 同样keyingBy来自org.apache.flink.streaming.api.scala.extensions._包，这里使用keyBy也可以
  // keyBy操作后会返回一个KeyedStream，保存了key信息
  sourceDataStream
    .keyingBy(_._1)
    // 与Chapter 02不同，这里我们调用window来设置窗口
    // 以下代码说明参见README
    .window(SlidingProcessingTimeWindows.of(Time.seconds(60L), Time.seconds(10L)))
    // 计算交易额的总和
    .sum(1)
    .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(10L)))
    .process(new ProcessAllWindowFunction[(String, Long), String, TimeWindow] {
      override def process(context: Context, elements: Iterable[(String, Long)], out: Collector[String]): Unit = {
        val top3 = elements.toSeq
          .sortBy(-_._2)
          .take(3)
          .zipWithIndex
          .map { case ((item, price), idx) => s"   ${idx + 1}. $item: $price" }
          .mkString("\n")
        out.collect(("-" * 16) + "\n" + top3)
      }
    })
    .print()
  env.execute("Chapter 03")

  /**
   * 每100ms产生一条"交易"数据，最终输出品类+价格（随机产生）
   */
  class C03Source extends SourceFunction[(String, Long)] {
    private val items = Array(
      // 男装
      "卫衣",
      "T恤",
      "牛仔裤",
      "西服",
      "风衣",
      // 女装
      "连衣裙",
      "卫衣",
      "衬衫",
      "针织衫",
      "休闲裤",
      // 手机数码
      "手机",
      "手机配件",
      "摄影摄像",
      "影音娱乐",
      "数码配件",
      "智能设备",
      "电子教育",
      // 电脑办公
      "电脑整机",
      "电脑组件",
      "外设",
      "网络产品",
      "办公设备",
      "文具耗材",
      // 家用电器
      "电视",
      "空调",
      "洗衣机",
      "冰箱",
      "厨卫",
      "生活电器",
      // 户外运动
      "运动鞋包",
      "运行服饰",
      "户外鞋服",
      "户外装备",
      "骑行",
      "健身",
      // 家具家装
      "厨房卫浴",
      "灯饰照明",
      "五金工具",
      "客厅家具",
      "餐厅家具",
      // 图书文娱
      "少儿读物",
      "文学",
      "动漫",
      "专业"
    )
    var running = true

    /**
     * Flink会调用run来收集数据
     */
    override def run(sourceContext: SourceFunction.SourceContext[(String, Long)]): Unit = {
      val random = new Random()
      do {
        val item = items(random.nextInt(items.length))
        val price = random.nextInt(3333) + 33
        // context.collect通知Flink新元素进入系统
        sourceContext.collect(item -> price.toLong)
        Thread.sleep(1000)
      } while (running)
    }

    override def cancel(): Unit = running = false

  }

}
