package module_b

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

import java.util.Properties

object task2_3 {
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
    df01.createOrReplaceTempView("base_province")
    df02.createOrReplaceTempView("order_info")
    df03.createOrReplaceTempView("base_region")
    df04.createOrReplaceTempView("user_info")

    /**
     * 3、请根据dws层表计算出每个省份2020年4月的平均订单金额和该省所在地区平均订单金额相比较结果（“高/低/相同”
     * ）,存入MySQL数据库shtd_result的provinceavgcmpregion表中（表结构如下），然后在Linux的MySQL命令行
     * 中根据省份表主键、省平均订单金额、地区平均订单金额均为降序排序，查询出前5条，将SQL语句与执行结果截图粘
     * 贴至对应报告中。
     */
    val df3 = spark.sql(
      """
        |SELECT
        |	t1.*,
        |	t2.regionavgconsumption,
        |CASE
        |		WHEN t1.provinceavgconsumption < t2.regionavgconsumption THEN '高' WHEN t1.provinceavgconsumption > t2.regionavgconsumption THEN
        |		'低' ELSE '相同'
        |	END comparison
        |FROM
        |	(
        |	SELECT
        |		base_province.id provinceid,
        |		base_province.NAME provincename,
        |		base_region.id regionid,
        |		base_region.region_name regionname,
        |		avg( final_total_amount ) provinceavgconsumption
        |	FROM
        |		order_info
        |		JOIN base_province ON base_province.id = order_info.province_id
        |		JOIN base_region ON base_region.id = base_province.region_id
        |	WHERE
        |		YEAR ( create_time ) = '2020'
        |		AND MONTH ( create_time )= '4'
        |	GROUP BY
        |		provinceid,
        |		provincename,
        |		regionid,
        |		regionname
        |	) t1
        |	JOIN (
        |	SELECT
        |		base_region.id AS regionid,
        |		avg( final_total_amount ) regionavgconsumption
        |	FROM
        |		order_info
        |		JOIN base_province ON base_province.id = order_info.province_id
        |		JOIN base_region ON base_region.id = base_province.region_id
        |	GROUP BY
        |	regionid
        |	) t2 ON t1.regionid = t2.regionid
        |""".stripMargin)

    df3.show()


  }
}
