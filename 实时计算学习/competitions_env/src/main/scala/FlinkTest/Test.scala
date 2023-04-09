package FlinkTest

import org.apache.flink.streaming.api.scala._

object Test {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.execute()
  }

}
