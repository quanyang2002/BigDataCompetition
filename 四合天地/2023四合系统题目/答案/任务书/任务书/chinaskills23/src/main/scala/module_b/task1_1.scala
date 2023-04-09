package module_b

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Properties

object task1_1 {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    //配置hive连接地址
    val spark = SparkSession
      .builder()
      .appName("hive example")
      .master("local")
      //            .config("hive.metastore.uris", "thrift://ngc:9083")
      //            .config("spark.sql.warehouse.dir", "hdfs://ngc:8020/user/hive/warehouse")
      //            .config("dfs.client.use.datanode.hostname", "true")
      //            .enableHiveSupport()
      .getOrCreate()

    //配置MySQL地址
    val properties = new Properties()
    val url = "jdbc:mysql://ngc:3307/gy_pub"
    properties.setProperty("user", "root")
    properties.setProperty("password", "123456")

    val df = spark.read.jdbc(url, "user_info", properties)
    df.createOrReplaceTempView("origin_table")
    spark.sql("desc origin_table").show()

    /**
     * 方法一：SparkSQL自带函数greatest
     * greatest(exprs: Column*):
     * 返回多列中的最大值，跳过Null
     */
    spark.sql(
      """
        |select *,'20221111' as partition_field
        |from origin_table
        |where greatest(operate_time,create_time) >'2020-04-26 23:02:22'
        |""".stripMargin).show()

    /**
     * 方法二：CASE WHEN
     */
    spark.sql(
      """
        |select *,'20221111' as partition_field
        |from origin_table
        |where case when operate_time>create_time then operate_time else create_time end >'2020-04-26 23:02:22'
        |""".stripMargin).show()


    /**
     * 方法三：自定义udf函数
     */
    spark.udf.register("choose_row", (operate_time: Timestamp, create_time: Timestamp) => {
      var res = ""
      val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      if (operate_time == null) {
        res = sdf.format(create_time.getTime)
      } else {
        if (operate_time.getTime > create_time.getTime) {
          res = sdf.format(operate_time.getTime)
        } else {
          res = sdf.format(create_time.getTime)
        }
      }
      res
    })
    val df1 = spark.sql(
      """
        |select *,'20221111' as partition_field
        |from origin_table
        | where  choose_row(operate_time,create_time)>'2020-04-26 23:02:22'
        |""".stripMargin)
    df1.show(true)



    //追加append 覆盖overwrite
    //    df1.write.mode("append").partitionBy("partition_field").saveAsTable("user_info")
    //添加动态分区
    df.write.mode("overwrite").partitionBy("etldate").saveAsTable("accommodationdata1")

    //spark sql函数
    //https://blog.csdn.net/liam08/article/details/79663018

    //spark sql 数组操作
    //https://sparkbyexamples.com/spark/spark-sql-array-functions/

    //spark 转rdd/自定义udf函数 数据类型转化
    //https://blog.csdn.net/An1090239782/article/details/102541024
    spark.stop()
  }

}
