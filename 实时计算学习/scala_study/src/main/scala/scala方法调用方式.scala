object scala方法调用方式 {

  // 定义无参方法

  def main(args: Array[String]): Unit = {

    // 后缀调用法:对象.方法(参数)
    println(Math.abs(-10))
    // 中缀调用法: 对象名 方法名 参数
    //注意：如果涉及到多个参数，只需要将多个参数用小括号括起来
    println(Math abs -10)
    // 花括号调用法
    val res = Math.abs{
      println("求绝对值")
      -29
    }
    println(res)

  }
}
