package FlinkTest

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

object FlinkRedisTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    // 练习写入redis数据
    val redisConf = new FlinkJedisPoolConfig.Builder().setHost("192.168.3.55").setPort(6379).build()
    val stream = env.fromElements(("quanyang1", 123))
    stream.addSink(new RedisSink[(String, Int)](redisConf,new MyRedis()))
    env.execute()
  }
  class MyRedis() extends RedisMapper[(String,Int)] {
    override def getCommandDescription: RedisCommandDescription = new RedisCommandDescription(RedisCommand.SET)

    override def getKeyFromData(t: (String, Int)): String = t._1

    override def getValueFromData(t: (String, Int)): String = t._2 + ""
  }

}
