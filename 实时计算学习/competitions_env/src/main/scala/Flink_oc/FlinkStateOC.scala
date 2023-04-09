package Flink_oc

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.streaming.api.scala._

object FlinkStateOC {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
//    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    val stream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\实时计算学习\\competitions_env\\src\\main\\scala\\FlinkTest\\ds.txt")
    stream.map(data => (1,data.split(" ")(0),data.split(" ")(1).toInt*data.split(" ")(2).toInt))
      .keyBy(_._1).map(new MyMapper()).print()
    env.execute()
  }
  class MyMapper() extends RichMapFunction[(Int,String,Int),String] {
    lazy val firstProductName = getRuntimeContext.getState(new ValueStateDescriptor[String]("firstProduct",classOf[String]))
    lazy val secondProductName = getRuntimeContext.getState(new ValueStateDescriptor[String]("secondProduct",classOf[String]))
    lazy val firstProdcutValue = getRuntimeContext.getState(new ValueStateDescriptor[Int]("firstValue",classOf[Int]))
    lazy val secondProdcutValue = getRuntimeContext.getState(new ValueStateDescriptor[Int]("secondValue",classOf[Int]))
    override def map(in: (Int,String,Int)): String = {

      if (in._3 > firstProdcutValue.value()){
        secondProdcutValue.update(firstProdcutValue.value())
        firstProdcutValue.update(in._3)
        secondProductName.update(firstProductName.value())
        firstProductName.update(in._2)
      }
      else if(in._3 > secondProdcutValue.value()){
        secondProdcutValue.update(in._3)
        secondProductName.update(in._2)
      }
      "Top1:"+firstProductName.value()+" Top2:"+secondProductName.value()
    }
  }
}
