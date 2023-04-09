import scala.io.StdIn

object scala打招呼 {

  def main(args: Array[String]): Unit = {

    println("请输入您的姓名：")
    val name = StdIn.readLine()
    println("请输入您的年龄：")
    val age = StdIn.readInt()
    print(s"您好！欢迎${age}岁的${name}")
  }

}
