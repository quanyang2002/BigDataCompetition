package example

import com.google.gson.{GsonBuilder, JsonParser}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.{Configuration, RestOptions}
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.functions.ProcessFunction

import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.util.Collector

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Random

case class FlinkTestCaseClass(new_id: Int, ss: String, sa: String)

// 本地调试Flink的时候需要，检查集群kafka的server.propertise中的advertised.listeners=PLAINTEXT://${ip}:9092 ,
// 将${ip}改为直连ip（使用23.2设备的学校，有两层ip，使用最外层ip也就是直连ip）或者本节点的ip（使用45.3设备的学校，使用容器的内部ip，在容器内使用ip addr查看ip，172开头），
// kafka为集群模式，三个节点都需要改动
// 建议本地配置好hosts
// kafka-console-producer.sh  --broker-list bigdata1:9092  --topic ods_topic
// flink run -m yarn-cluster -p 2 -yjm 1G -ytm 1G -c ********** /***********.jar onyarn
object FlinkDemo {

  def main(args: Array[String]): Unit = {

    var env = StreamExecutionEnvironment.getExecutionEnvironment
    var tableEnv = StreamTableEnvironment.create(env)

    //为了本地调试方便，开启了本地ui,集群调试请注意多加个参数
    if ((args.length > 0 && args(0).equals("local")) || args.length == 0) {
      val configuration: Configuration = new Configuration()
      configuration.setString(RestOptions.BIND_PORT, "8081-8089")
      env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration)
      tableEnv = StreamTableEnvironment.create(env)
    }

    //kafka
    val source = KafkaSource.builder()
      .setBootstrapServers("bigdata1:9092,bigdata2:9092,bigdata3:9092")
      .setTopics("ods_topic")
      .setGroupId("test111111")
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val kafka_sink = KafkaSink.builder[String]()
      .setBootstrapServers("bigdata1:9092,bigdata2:9092,bigdata3:9092")
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("dwd_topic")
        .setValueSerializationSchema(new SimpleStringSchema())
        .build()
      ).build()


    val kafkaDS = env.fromSource(source, WatermarkStrategy.noWatermarks(), "kafka_fact_order_master")

    val originDS = kafkaDS.map(data => {
      //字符串转为jsonobject，y也可以用注释掉的这种方式
      //      val gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
      //      gson.fromJson(data,classOf[FlinkTestCaseClass])

      val data2 = "{ 'id' :1}"
      val jsonObject = JsonParser.parseString(data2).getAsJsonObject
      val aaaxxxx = jsonObject.get("id").getAsString
      FlinkTestCaseClass(new Random().nextInt(100000), aaaxxxx, data)
    })


    val mainstream = originDS.process(new ProcessFunction[FlinkTestCaseClass, FlinkTestCaseClass] {
      override def processElement(value: FlinkTestCaseClass, ctx: ProcessFunction[FlinkTestCaseClass, FlinkTestCaseClass]#Context, out: Collector[FlinkTestCaseClass]): Unit = {

        lazy val hbase_sink_tag = new OutputTag[FlinkTestCaseClass]("hbase_sink")
        lazy val ck_sink_tag = new OutputTag[FlinkTestCaseClass]("ck_sink")

        ctx.output(hbase_sink_tag, value)

        ctx.output(ck_sink_tag, value)

        out.collect(value)

      }

    })
    //获取边流
    val sidehbasestream = mainstream.getSideOutput(new OutputTag[FlinkTestCaseClass]("hbase_sink"))
    val sideckstream = mainstream.getSideOutput(new OutputTag[FlinkTestCaseClass]("ck_sink"))


    /**
     * sink kafka
     * */
    mainstream.map(fcc => {
      //字符串转为jsonobject
      val gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
      gson.toJson(fcc)
    }).sinkTo(kafka_sink)





    /*
    Sink CK
    *
    *  只要是我们的训练服务器，我们已经配置好了跑job的三个服务器，里面的配置不需要改（如果需要改，我们会特意说明）
    *  如果连不上，检查ck的配置文件看看这一行 <listen_host>0.0.0.0</listen_host>
     */
    //集群连接的命令，避开了9000端口，使用9001
    //clickhouse-client -m --host bigdata1 --port=9001 --password 123456
    //建表语句 create database ads;
    //create table IF NOT EXISTS ads.ads_ck ( ss    String, a int ) ENGINE = TinyLog;
    sideckstream.addSink(new RichSinkFunction[FlinkTestCaseClass] {

      var conn: Connection = _
      var insertStmt: PreparedStatement = _

      val insertClickhouse: String =
        """
          |INSERT INTO ads_ck(
          |ss,
          |a
          |)VALUES
          | (?,?)
          |""".stripMargin

      override def open(parameters: Configuration): Unit = {

        conn = DriverManager.getConnection(
          "jdbc:clickhouse://bigdata1:8123/ads",
          "default"
          , "123456"
        )
        insertStmt = conn.prepareStatement(insertClickhouse)
      }


      override def invoke(value: FlinkTestCaseClass, context: SinkFunction.Context): Unit = {


        insertStmt.setString(1, value.sa)
        insertStmt.setInt(2, 1)
        insertStmt.execute()
      }

      override def close(): Unit = {
        if (insertStmt != null)
          insertStmt.close()
        if (conn != null)
          conn.close()
      }
    })


    /**
     * sink hbase
     * */
    //建表语句：create_namespace 'ods'
    // create 'ods:demo_hbase','info'
    tableEnv.executeSql(
      s"""
         |
         |CREATE TABLE if not exists  Hbase_Test (
         | rowkey string,
         | info ROW<
         |new_id	int
         |
         | >
         |) WITH (
         | 'connector' = 'hbase-2.2',
         | 'table-name' = 'ods:demo_hbase',
         | 'zookeeper.quorum' = 'bigdata1:2181,bigdata2:2181,bigdata3:2181'
         |)
         |
         |""".stripMargin)


    val stat = tableEnv.createStatementSet()


    tableEnv.createTemporaryView("temp_table", sidehbasestream)
    stat.addInsertSql(
      """
        |insert into Hbase_Test
        |select rowkey,row(new_id ) from (
        |select cast(new_id as string) as rowkey,new_id from temp_table
        |) a
        |""".stripMargin)


    stat.execute()
    env.execute("FlinkDemo")
  }


}


