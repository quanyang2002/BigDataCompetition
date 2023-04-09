package module_b

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.types.LongType

import java.util.Properties

object task5_3 {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val spark = SparkSession
      .builder()
      .appName("hive example")
      .master("local")
      .getOrCreate()
    //配置MySQL地址
    val properties = new Properties()
    val url = "jdbc:mysql://ngc:3307/gy_pub?useSSL=false"
    val url_save = "jdbc:mysql://ngc:3307/shtd_result?useSSL=false"
    properties.setProperty("user", "root")
    properties.setProperty("password", "123456")

    val df01 = spark.read.jdbc(url, "base_province", properties)
    val df02 = spark.read.jdbc(url, "order_info", properties)
    val df03 = spark.read.jdbc(url, "base_region", properties)
    val df04 = spark.read.jdbc(url, "user_info", properties)
    val df05 = spark.read.jdbc(url, "order_detail", properties)
    val df06 = spark.read.jdbc(url, "sku_info", properties)
    df01.createOrReplaceTempView("base_province")
    df02.createOrReplaceTempView("order_info")
    df03.createOrReplaceTempView("base_region")
    df04.createOrReplaceTempView("user_info")
    df05.createOrReplaceTempView("order_detail")
    df06.createOrReplaceTempView("sku_info")
    //    val df2 = spark.sql(
    //      """
    //        |
    //        |""".stripMargin)
    /**
     * 3、根据dwd层的数据，请计算每个省份累计订单量（订单信息表一条算一个记录），然后根据每个省份订单量从高到低排列，
     * 将结果打印到控制台（使用spark中的show算子，同时需要显示列名），将执行结果截图粘贴至对应报告中。
     * select str_to_map(concat_ws(',', collect_set(concat(no, '-',score))),',','-')
     * from test_tmp_sy
     * group by no
     */
    val df3_1 = spark.sql(
      """
        |SELECT
        |	base_province.name province_name,
        |	COUNT(*) amount
        |FROM
        |	order_info
        |	JOIN base_province ON order_info.province_id = base_province.id
        |GROUP BY
        |	base_province.name
        |ORDER BY
        |	amount DESC
        |""".stripMargin)
    df3_1.createOrReplaceTempView("pn_amount")
    //    val df3_2 = spark.sql(
    //      """
    //        |select str_to_map(concat_ws(',', collect_set(concat(province_name, '-',amount))),',','-') stm
    //        |from pn_amount
    //        |""".stripMargin)
    df3_1.show()
    val df3_2 = df3_1
      .withColumn("key", lit("key"))
      .groupBy("key")
      .pivot(col("province_name")).agg(functions.sum("amount"))
      .drop("key")
    df3_2.show()

  }
}
