package example

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.util.Properties

//建议本地配置好hosts
//spark-submit --master yarn --driver-memory 1G --driver-cores 1 --num-executors 2 --executor-memory 1g --executor-cores 1  --class XXXXXX /XXXXX.jar yarn
object sparkDemo {
  def main(args: Array[String]): Unit = {
    val sparkBuilder = SparkSession.builder()
    if ((args.length > 0 && args(0).equals("local")) || args.length == 0) {
      sparkBuilder.master("local[*]")
    }

    val spark = sparkBuilder.appName("sparkDemo")
      .config("spark.network.timeout", 800)
      //本地调试必须要有hive-site.xml文件，这个文件从集群中拉出来放到resources下
      .enableHiveSupport()
      .getOrCreate()


    /**
     * 连接mysql
     * */
    spark.read.format("jdbc")
      .option("url", "jdbc:mysql://bigdata1:3306/ds_db01?characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("user", "root")
      .option("password", "123456")
      .option("dbtable", "order_master").load().createTempView("mysql_order_master")

    //    spark.sql(
    //      """
    //        |
    //        |insert overwrite table ods.demo partition (etl_date="19990909")
    //        |select * from mysql_order_master limit 10
    //        |""".stripMargin)

    spark.sql(
      """
        |select * from mysql_order_master limit 10
        |""".stripMargin).show()


    /**
     * 链接hive 写入ck  ck的表为flink的存入表
     * 只要是我们的训练服务器，我们已经配置好了跑job的服务器，里面的配置不需要改（如果需要改，我们会特意说明）
     * 如果连不上，检查ck的配置文件看看这一行 <listen_host>0.0.0.0</listen_host>
     * */
    //ck建表语句 create database ads;
    //create table IF NOT EXISTS ads.ads_ck ( ss    String, a int ) ENGINE = TinyLog;
    val properties = new Properties()
    properties.setProperty("user", "default")
    properties.setProperty("password", "123456")
    properties.setProperty("driver", "ru.yandex.clickhouse.ClickHouseDriver")


    val resultckdata = spark.sql(
      """
        |
        |select level_name as ss,customer_level as a  from ods.customer_level_inf
        |""".stripMargin)

    resultckdata.write.mode(SaveMode.Append).jdbc("jdbc:clickhouse://bigdata1:8123/ads", "ads_ck", properties)


    /**
     * spark 操作hbase  hbase为flink的存入表
     * */

    val conf = HBaseConfiguration.create()
    conf.set("hbase.zookeeper.quorum", "bigdata1:2181,bigdata2:2181,bigdata3:2181")
    conf.set(TableInputFormat.INPUT_TABLE, "ods:demo_hbase")

    //查询表
    val hbaseRDD = spark.sparkContext.newAPIHadoopRDD(conf, classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result]
    )
    hbaseRDD.foreach {
      case (rowkey, result) => {
        println(Bytes.toString(rowkey.get()))
        println(Bytes.toInt(result.getValue("info".getBytes, "new_id".getBytes)))

      }
    }


    //插入表
    val dataRDD = spark.sparkContext.makeRDD(List(("1002", 1), ("1003", 2),
      ("1004", 3)))

    val putRDD = dataRDD.map {
      case (rowkey, newId) => {
        val put = new Put(Bytes.toBytes(rowkey))
        put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("new_id"),
          Bytes.toBytes(newId))
        (new ImmutableBytesWritable(Bytes.toBytes(rowkey)), put)
      }
    }
    val jobConf = new JobConf(conf)
    jobConf.setOutputFormat(classOf[TableOutputFormat])
    jobConf.set(TableOutputFormat.OUTPUT_TABLE, "ods:demo_hbase")

    putRDD.saveAsHadoopDataset(jobConf)


  }
}
