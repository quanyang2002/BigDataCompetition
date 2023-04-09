package module_c

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession


import java.util.Properties

object task1 {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    //配置hive连接地址
    val spark = SparkSession
      .builder()
      .appName("ml example")
      .master("local")
      .getOrCreate()

    //配置MySQL地址
    val properties = new Properties()
    val url = "jdbc:mysql://ngc:3307/gy_pub?useSSL=false"
    properties.setProperty("user", "root")
    properties.setProperty("password", "123456")

    val df_order_info = spark.read.jdbc(url, "order_info", properties)
    val df_order_detail = spark.read.jdbc(url, "order_detail", properties)
    df_order_info.createOrReplaceTempView("order_info")
    df_order_detail.createOrReplaceTempView("order_detail")

    /**
     * 1、据Hive的dwd库中相关表或MySQL数据库shtd_store中订单相关表（order_detail、order_info、sku_info）
     * ，对用户购买过的商品进行去重，将其转换为以下表：第一列为用户id mapping，第二列为用户购买过的商品id
     * mapping，按照user_id与sku_id进行升序排序，输出前5行，将结果截图粘贴到对应报告中。
     */
    val df1 = spark.sql(
      """
        |SELECT
        |		DENSE_RANK() OVER ( ORDER BY user_id )- 1 user_id,
        |		DENSE_RANK() OVER ( ORDER BY sku_id )- 1 sku_id
        |FROM
        |	order_info
        |	JOIN order_detail ON order_info.id = order_detail.order_id
        |ORDER BY
        |	user_id,
        |	sku_id
        |""".stripMargin)

    /**
     * 2、根据第1小题的结果，对其进行聚合，其中对sku_id进行one-hot转换，将其转换为以下格式矩阵：
     * 第一列为用户id，其余列名为商品id，按照用户id进行升序排序，展示矩阵第一行前5列数据，将结果
     * 截图粘贴至对应报告中。
     */

    val df2 = spark.sql(
      """
        |SELECT
        |	user_id,
        |	concat_ws(',',collect_set(sku_id)) array_sku_id
        |FROM
        |	(
        |	SELECT
        |		DENSE_RANK() OVER ( ORDER BY user_id )- 1 user_id,
        |		DENSE_RANK() OVER ( ORDER BY sku_id )- 1 sku_id
        |	FROM
        |		order_info
        |		JOIN order_detail ON order_info.id = order_detail.order_id
        |	ORDER BY
        |		user_id,
        |		sku_id
        |	) t1
        |GROUP BY
        |	user_id
        |""".stripMargin)
    val count = spark.sql(
      """
        |select count(DISTINCT(sku_id)) from order_detail
        |""".stripMargin).take(1)(0).getLong(0)

    val rdd = df2.rdd
      .map(row => {
        (row.getInt(0), row.getString(1))
      })
      .map(kv => {
        val userId = kv._1.toDouble
        val arr = new Array[Double](count.toInt)
        for (i <- kv._2.split(",")) {
          arr.update(i.toInt, 1)
        }
        (userId,arr.mkString(","))
      }).map(kv=>kv._1+","+kv._2)
    df1.show()
    df2.show(100)
    rdd.foreach(println)


    spark.stop()
  }

}
