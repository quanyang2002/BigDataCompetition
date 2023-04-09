package module_b

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

import java.text.SimpleDateFormat
import java.util.{Date, Properties}

object task10_1 {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    //配置hive连接地址
    val spark = SparkSession
      .builder()
      .appName("hive example")
      .master("local")
      .config("hive.metastore.uris", "thrift://124.70.183.40:9083")
      .config("spark.sql.warehouse.dir", "hdfs://124.70.183.40:8020/user/hive/warehouse")
      .config("dfs.client.use.datanode.hostname", "true")
      .enableHiveSupport()
      .getOrCreate()

    //配置MySQL地址
    val properties = new Properties()
    val url = "jdbc:mysql://ngc:3307/ds_pub"
    properties.setProperty("user", "root")
    properties.setProperty("password", "123456")

    val df = spark.read.jdbc(url, "environmentdata", properties)
    df.createOrReplaceTempView("environmentdata")
    //模块B的结果
    val df1 = spark.sql(
      """
        |select *,'20221111' as etldate
        |from environmentdata
        |""".stripMargin)

    df1.write.mode("overwrite").partitionBy("etldate").saveAsTable("environmentdata")

    df1.show()
    spark.stop()
  }

}
