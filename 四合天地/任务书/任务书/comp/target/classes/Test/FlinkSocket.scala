package Test

import org.apache.flink.streaming.api.scala._

object FlinkSocket {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val value = env.socketTextStream("47.101.188.234", 9999)
    value.print()

    env.execute()
  }

}
