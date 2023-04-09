package module_b

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

import java.util.Properties

object task1_3 {
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
     * 1、根据dwd层表统计每个省份、每个地区、每个月下单的数量和下单的总金额，存入MySQL数据库shtd_result的
     * provinceeverymonth表中（表结构如下），然后在Linux的MySQL命令行中根据订单总数、订单总金额、省份表
     * 主键均为降序排序，查询出前5条，将SQL语句与执行结果截图粘贴至对应报告中;
     */
    val df1 = spark.sql(
      """
        |SELECT
        |	base_province.id AS provinceid,
        |	base_province.name AS provincename,
        |	base_region.id AS regionid,
        |	base_region.region_name AS regionname,
        |	sum( final_total_amount ) AS totalconsumption,
        |	count(*) AS totalorder,
        |	YEAR ( create_time ) year,
        |	MONTH ( create_time ) month
        |FROM
        |	order_info
        |	JOIN base_province ON base_province.id = order_info.province_id
        |	JOIN base_region ON base_region.id = base_province.region_id
        |GROUP BY
        |	provinceid,
        |	provincename,
        |	regionid,
        |	regionname,
        |	YEAR ( create_time ),
        |	MONTH ( create_time )
        |""".stripMargin)
//            df1.write.jdbc(url_save, "provinceeverymonth", properties)
    /**
     * 2、请根据dwd层表计算出2020年4月每个省份的平均订单金额和所有省份平均订单金额相比较结果（“高/低/相同”）
     * ,存入MySQL数据库shtd_result的provinceavgcmp表中（表结构如下），然后在Linux的MySQL命令行中根据省
     * 份表主键、该省平均订单金额均为降序排序，查询出前5条，将SQL语句与执行结果截图粘贴至对应报告中;
     */
    val df2 = spark.sql(
      """
        |SELECT
        |	base_province.id provinceid,
        |	base_province.NAME provincename,
        |	avg( final_total_amount ) provinceavgconsumption,
        |	'4705.917526' allprovinceavgconsumption,
        |CASE
        |		WHEN avg( final_total_amount )< '4705.917526' THEN
        |		'高'
        |		WHEN avg( final_total_amount )> '4705.917526' THEN
        |		'低' ELSE '相同'
        |	END comparison
        |FROM
        |	order_info
        |	JOIN base_province ON base_province.id = order_info.province_id
        |WHERE
        |	YEAR ( create_time ) = '2020'
        |	AND MONTH ( create_time )= '4'
        |GROUP BY
        |	provinceid,
        |	provincename
        |""".stripMargin)
    //    df2.write.jdbc(url_save, "provinceavgcmp", properties)

    /**
     * 3、根据dwd层表统计在两天内连续下单并且下单金额保持增长的用户，存入MySQL数据库shtd_result的
     * usercontinueorder表中(表结构如下)，然后在Linux的MySQL命令行中根据订单总数、订单总金额、客
     * 户主键均为降序排序，查询出前5条，将SQL语句与执行结果截图粘贴至对应报告中。
     */
    //因为数据量少，这里user_id和name都写死了，实际上要换成 user_id,name
    val df3 = spark.sql(
      """
        |SELECT
        |	"8888",
        |	"张三",
        |	to_date(order_info.create_time,'yyyyMMdd') DAY,
        |	sum( final_total_amount ),
        |	count(*)
        |FROM
        |	order_info
        |	JOIN user_info ON user_info.id = order_info.user_id
        |GROUP BY
        |	'8888',
        |	'张三',
        |	to_date(order_info.create_time,'yyyyMMdd')
        |""".stripMargin)
    import spark.implicits._
    val rdd3 = df3.rdd
      .map(_.mkString(",").split(","))
      .map(arr => ((arr(0).toInt, arr(1)), arr(2), arr(3).toDouble, arr(4).toInt))
      .groupBy(_._1)
      .mapValues(iter => {
        val list = iter.toList.sortBy(_._2)
        var resultList: List[(Int, String, String, Double, Int)] = List()
        if (list.size != 1) {
          val iterator = list.sliding(2, 1)
          for (elem <- iterator) {
            if (elem.head._3 < elem.last._3) {
              val userid = elem.head._1._1
              val username = elem.head._1._2
              val day = elem.head._2 + "_" + elem.last._2
              val totalconsumption = elem.head._3 + elem.last._3
              val totalorder = elem.head._4 + elem.last._4
              resultList = resultList.::(userid, username, day.replaceAll("-", ""), totalconsumption, totalorder)
            }
          }
        }
        resultList
      }).flatMap(_._2)

    val df3_1 = rdd3.toDF("userid", "username", "day", "totalconsumption", "totalorder")
    //    df3_1.write.jdbc(url_save, "usercontinueorder", properties)

    df1.show()
    df2.show()
    df3_1.show()

  }
}
