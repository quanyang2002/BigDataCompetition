package module_c

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.linalg.{DenseVector, Vectors}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{broadcast, col}
import org.apache.spark.sql.types.StringType

import java.text.DecimalFormat
import java.util.Properties

object task2 {
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
    val df_sku_info = spark.read.jdbc(url, "sku_info", properties)
    df_order_info.createOrReplaceTempView("order_info")
    df_order_detail.createOrReplaceTempView("order_detail")
    df_sku_info.createOrReplaceTempView("sku_info")

    /**
     * 1、根据Hive的dwd库中相关表或MySQL中shtd_store中相关表（order_detail、sku_info），
     * 计算出与用户id为6708的用户所购买相同商品种类最多的前10位用户（只考虑他俩购买过多少个相同
     * 的商品，不考虑相同的商品买了多少次），将10位用户id进行输出，输出格式如下，将结果截图粘贴至报告中：
     */
    val df_7031 = spark.sql(
      """
        |SELECT
        |	collect_set(sku_id) array_sku_7031
        |FROM
        |	(
        |	SELECT
        |		user_id,
        |		sku_id
        |	FROM
        |		order_info
        |		JOIN order_detail ON order_info.id = order_detail.order_id
        |	ORDER BY
        |		user_id,
        |		sku_id
        |	) t1
        |GROUP BY
        |	user_id
        |	HAVING user_id=7031
        |""".stripMargin)
    df_7031.show()
    val df1 = spark.sql(
      """
        |SELECT
        | user_id,
        |	collect_set(sku_id) array_sku
        |FROM
        |	(
        |	SELECT
        |		user_id,
        |		sku_id
        |	FROM
        |		order_info
        |		JOIN order_detail ON order_info.id = order_detail.order_id
        |	ORDER BY
        |		user_id,
        |		sku_id
        |	) t1
        |GROUP BY
        |	user_id
        | HAVING user_id !=7031
        |""".stripMargin)
    df1.show()
    val df1_1 = df1.crossJoin(broadcast(df_7031))
    df1_1.createOrReplaceTempView("t1")
    val df1_2 = spark.sql(
      """
        |select user_id,size(array_intersect(array_sku,array_sku_7031)) size
        | from t1
        | order by size desc
        |""".stripMargin)
    df1_2.show(10)

    /**
     * 2、根据Hive的dwd库中相关表或MySQL中shtd_store中相关商品表（sku_info），
     * 获取id、spu_id、price、weight、tm_id、category3_id 这六个字段并进行数据预处理
     * ，对price、weight进行规范化(StandardScaler)处理，对spu_id、tm_id、category3_id
     * 进行one-hot编码处理（若该商品属于该品牌则置为1，否则置为0）,并按照id进行升序排序，
     * 在集群中输出第一条数据前10列（无需展示字段名），将结果截图粘贴至报告中。
     */
    import spark.implicits._
    val df2 = spark.sql(
      """
        |	SELECT id,price,weight,spu_id,tm_id,category3_id from sku_info
        |""".stripMargin)
      .rdd.map(_.mkString(","))
      .map(line => {
        val arr = line.split(",")
        (arr(0).toInt, Vectors.dense(arr(1).toDouble, arr(2).toDouble), arr(3), arr(4), arr(5))
      }).toDF("id", "features", "spu_id", "tm_id", "category3_id")

    val scaler = new StandardScaler()
      .setInputCol("features")
      .setOutputCol("scaledFeatures")
    val scalerModel = scaler.fit(df2)
    val scaledData = scalerModel.transform(df2)

    val count_spu = spark.sql(
      """
        |select count(DISTINCT(spu_id)) from sku_info
        |""".stripMargin).take(1)(0).getLong(0)
    val count_tm = spark.sql(
      """
        |select count(DISTINCT(tm_id)) from sku_info
        |""".stripMargin).take(1)(0).getLong(0)
    val count_cate = spark.sql(
      """
        |select count(DISTINCT(category3_id)) from sku_info
        |""".stripMargin).take(1)(0).getLong(0)

    val rdd2 = scaledData.rdd
      .map(row => {
        val vector = row.getAs[DenseVector]("scaledFeatures")
        val strings = vector.values.map(x => new DecimalFormat("#.##").format(x))
        //        new Array[Double](count_spu.toInt), new Array[Double](count_tm.toInt), new Array[Double](count_cate.toInt)
        (row.getInt(0), strings(0), strings(1))
      })
    val df2_1 = rdd2.toDF("f_id", "f1", "f2")
    val df2_2 = spark.sql(
      """
        |	SELECT
        |	id,
        |		DENSE_RANK() OVER ( ORDER BY spu_id )-1 AS spu_id,
        |		DENSE_RANK() OVER ( ORDER BY tm_id )-1 AS tm_id,
        |		DENSE_RANK() OVER ( ORDER BY category3_id )-1 AS category3_id
        |	FROM
        |		sku_info
        |	ORDER BY
        |		id
        |""".stripMargin)
    val df2_3 = df2_1
      .join(df2_2, df2_1.col("f_id") === df2_2.col("id"))
      .drop("f_id")
    df2_2.show()
    df2_3.show()
    val rdd2_1 = df2_3.rdd
      .map(_.mkString(","))
      .map(line => {
        val arr = line.split(",")
        val arr1 = new Array[Double](count_spu.toInt)
        arr1.update(arr(3).toInt, 1)
        val arr2 = new Array[Double](count_tm.toInt)
        arr2.update(arr(4).toInt, 1)
        val arr3 = new Array[Double](count_cate.toInt)
        arr3.update(arr(5).toInt, 1)
        arr(2).toDouble + "," + arr(0) + "," + arr(1) + "," + arr1.mkString(",") + "," + arr2.mkString(",") + "," + arr3.mkString(",")
      }).sortBy(line => line.substring(0, line.indexOf(",")).toDouble)
    rdd2_1.foreach(println)

    //另一种实现方法
    //Spark ML 特征工程之 One-Hot Encoding
    //https://www.jianshu.com/p/bcbd686d08df


    spark.stop()

  }

}
