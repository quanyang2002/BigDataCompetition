package module_b

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.LongType

import java.util.Properties

object task4_3 {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val spark = SparkSession
      .builder()
      .appName("hive example")
      .master("local")
      .getOrCreate()
    import spark.implicits._
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

    /**
     * 1、根据dwd层表统计每个省每月下单的数量和下单的总金额，并按照year，month，region_id进行分组,
     * 按照total_amount降序排序，形成sequence值，将计算结果存入Hive的dws数据库province_consumption_day_aggr
     * 表中（表结构如下），然后使用hive cli根据订单总数、订单总金额、省份表主键均为降序排序，查询出前5条，
     * 在查询时对于订单总金额字段将其转为bigint类型（避免用科学计数法展示），将SQL语句与执行结果截图粘贴
     * 至对应报告中;
     */
    //provinceid和regionid和表里重复了不能直接命名
    val df1 = spark.sql(
      """
        |SELECT
        |	base_province.id AS provinceid,
        |	base_province.name AS province_name,
        |	base_region.id AS regionid,
        |	base_region.region_name AS region_name,
        |	sum( final_total_amount ) AS total_amount,
        |	count(*) AS total_count,
        | RANK() OVER ( ORDER BY sum( final_total_amount ) DESC) sequence,
        |	YEAR ( create_time ) year,
        |	MONTH ( create_time ) month
        |FROM
        |	order_info
        |	JOIN base_province ON base_province.id = order_info.province_id
        |	JOIN base_region ON base_region.id = base_province.region_id
        |GROUP BY
        |	provinceid,
        |	province_name,
        |	regionid,
        |	region_name,
        |	YEAR ( create_time ),
        |	MONTH ( create_time )
        |""".stripMargin)
      .withColumnRenamed("provinceid", "province_id")
      .withColumnRenamed("regionid", "region_id")
      .withColumn("total_amount", col("total_amount").cast(LongType))
    //    df1.write.jdbc(url_save, "province_consumption_day_aggr", properties)

    /**
     * 2、请根据dwd层的相关表，计算2020年销售量前10的商品，销售额前10的商品，存入MySQL数据库shtd_result的
     * topten表中（表结构如下），然后在Linux的MySQL命令行中根据排名升序排序，查询出前5条，将SQL语句与执行结
     * 果截图粘贴至对应报告中;
     */
    val df2 = spark.sql(
      """
        |SELECT
        |	topquantityid,
        |	topquantityname,
        |	topquantity,
        |	toppriceid,
        |	toppricename,
        |	topprice,
        |	t1.sequence
        |FROM
        |	(
        |	SELECT
        |		sku_info.id topquantityid,
        |		sku_info.sku_name topquantityname,
        |		sum( sku_num ) topquantity,
        |		RANK() OVER ( ORDER BY sum( sku_num ) DESC) sequence
        |	FROM
        |		order_info
        |		JOIN order_detail ON order_info.id = order_detail.order_id
        |		JOIN sku_info ON order_detail.sku_id = sku_info.id
        |	GROUP BY
        |		topquantityid,
        |		topquantityname
        |		ORDER BY sequence
        |		LIMIT 10
        |	) t1
        |	JOIN (
        |	SELECT
        |		sku_info.id toppriceid,
        |		sku_info.sku_name toppricename,
        |		sum( final_total_amount ) topprice,
        |		RANK() OVER ( ORDER BY sum( final_total_amount ) DESC) sequence
        |	FROM
        |		order_info
        |		JOIN order_detail ON order_info.id = order_detail.order_id
        |		JOIN sku_info ON order_detail.sku_id = sku_info.id
        |	GROUP BY
        |		toppriceid,
        |		toppricename
        |		ORDER BY sequence
        |		LIMIT 10
        |	) t2 ON t1.sequence = t2.sequence
        |""".stripMargin)


    /**
     * 3、请根据dwd层的相关表，计算出2020年每个省份所在地区的订单金额的中位数,存入MySQL数据库
     * shtd_result的nationmedian表中（表结构如下），然后在Linux的MySQL命令行中根据地区表主
     * 键，省份表主键均为升序排序，查询出前5条，将SQL语句与执行结果截图粘贴至对应报告中。
     */
    //    spark.sql("select percentile(final_total_amount,0.5,100) from order_info").show()
    //    spark.sql("select percentile(final_total_amount,0.5,1) from order_info").show()
    val df3 = spark.sql(
      """
        |SELECT
        |	t1.*,
        |	t2.regionmedian
        |FROM
        |	(
        |	SELECT
        |		base_province.id AS provinceid,
        |		base_province.NAME AS provincename,
        |		base_region.id AS regionid,
        |		base_region.region_name AS regionname,
        |		percentile_approx( final_total_amount,0.5,100 ) provincemedian
        |	FROM
        |		order_info
        |		JOIN base_province ON base_province.id = order_info.province_id
        |		JOIN base_region ON base_region.id = base_province.region_id
        |	GROUP BY
        |		provinceid,
        |		provincename,
        |		regionid,
        |		regionname
        |	) t1
        |	JOIN (
        |	SELECT
        |		base_region.id AS regionid,
        |		percentile_approx( final_total_amount,0.5,100 )  regionmedian
        |	FROM
        |		order_info
        |		JOIN base_province ON base_province.id = order_info.province_id
        |		JOIN base_region ON base_region.id = base_province.region_id
        |	GROUP BY
        |		regionid,
        |	region_name
        |	) t2 ON t1.regionid = t2.regionid
        | """.stripMargin)
    df1.show()
    df2.show()
    df3.show()
  }
}
