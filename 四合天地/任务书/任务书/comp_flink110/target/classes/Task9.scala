import com.alibaba.fastjson.JSON
import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

object Task9 {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val source = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp_flink110\\src\\main\\scala\\sources\\data_merge.txt")
    val mastersource = source.filter(_.contains("order_master"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        val str = JSON.parseObject(nObject.getString("data"))
        val sn = str.getString("order_sn")
        val order_money = str.getString("order_money").toDouble
        (sn,order_money)
      }).keyBy(_._1)
      .sum(1)
    val detailsource = source.filter(_.contains("order_detail"))
      .map(data => {
        val nObject = JSON.parseObject(data)
        val nObject1 = JSON.parseObject(nObject.getString("data"))
        val order_sn = nObject1.getString("order_sn")
        val product_cnt = nObject1.getString("product_cnt").toInt
        (order_sn,product_cnt)
      }).keyBy(_._1)
      .sum(1)
      mastersource
        .join(detailsource)
        .where(_._1)
        .equalTo(_._1)
        .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
        .apply(new JoinFunction[(String,Double),(String,Int),(String,Double,Int)]{
          override def join(in1: (String, Double), in2: (String, Int)): (String, Double, Int) = {
            (in1._1,in1._2,in2._2)
          }
        }).print()
    env.execute()
  }

}
