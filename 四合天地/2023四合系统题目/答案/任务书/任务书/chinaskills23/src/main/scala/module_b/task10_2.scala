package module_b

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

import java.text.SimpleDateFormat
import java.util.{Date, Properties}

object task10_2 {
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

    val df = spark.sql("select * from environmentdata")

    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val df1 = df
      .withColumn("dwd_insert_user", lit("user1")) //添加新字段
      .withColumn("dwd_insert_time", lit(sdf.format(new Date()))) //添加新字段
      .withColumn("dwd_modify_user", lit("user1")) //添加新字段
      .withColumn("dwd_modify_time", lit(sdf.format(new Date()))) //添加新字段
    df1.write.mode("overwrite").partitionBy("etldate").saveAsTable("fact_environment_data")

    df1.show()
    spark.stop()
  }

}
