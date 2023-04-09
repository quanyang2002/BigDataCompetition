package SparkStudy

import org.apache.spark.{SparkConf, SparkContext}

object FirstSpark {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local").setAppName("my First")
    val sc = new SparkContext(conf)
    val testFile = "D:\\学习\\大数据\\技能大赛\\实时计算学习\\competitions_env\\src\\main\\scala\\SparkStudy\\testwords"
    val sourceData = sc.textFile(testFile).cache()
    val a = sourceData.filter(_.contains("el")).count()
    println(a)
  }

}
