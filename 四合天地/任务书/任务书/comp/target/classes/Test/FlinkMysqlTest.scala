package Test

import org.apache.flink.configuration.{ConfigUtils, Configuration}
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.scala._

import java.sql.{Connection, DriverManager, PreparedStatement}

object FlinkMysqlTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val insert_sql = "insert into fam_info(id,name) values(?,?)"
    val sourceStream = env.readTextFile("D:\\学习\\大数据\\技能大赛\\四合天地\\任务书\\任务书\\comp\\src\\main\\scala\\Test\\test_words")
    val mapStream = sourceStream.map(data => {
      (data.split(",")(0).toInt, data.split(",")(1))
    })
    mapStream.addSink(new MysqlSink())
    env.execute()
  }
  class MysqlSink extends RichSinkFunction[(Int,String)] {
    // 预设公共配置项 便于在各个方法内调用
    var connection:Connection = _
    var insertstmt:PreparedStatement = _
    // 定义对于数据库的操作
    override def invoke(value: (Int, String)): Unit = {
      insertstmt.setInt(1,value._1)
      insertstmt.setString(2,value._2)
      insertstmt.execute()
    }
    // 获取连接 初始化预执行平台
    override def open(parameters: Configuration): Unit = {
      connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test?useSSL=false","root","password")
      insertstmt = connection.prepareStatement("insert into fam_info(id,name) values(?,?)")
    }

    // 关闭连接
    override def close(): Unit = {
      insertstmt.close()
      connection.close()
    }
  }

}
