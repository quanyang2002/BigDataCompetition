import scala.io.StdIn

object scala键盘录入 {

  def main(args: Array[String]): Unit = {

    println("请输入一个字符串：")
    val str = StdIn.readLine()
    println("您录入的字符串内容为：" + str)
    println("请输入一个整数：")
    val num = StdIn.readInt()
    println("您录入的整数为：" + num)
  }

}
