import scala.io.StdIn
import scala.util.control.Breaks._

object scala模拟登录 {

  def main(args: Array[String]): Unit = {

    var times = 3
    breakable{
      while(true){
        if (times == 0){
          println("账号已锁定，请联系管理员")
          break()
        }
        println("请输入账号：")
        val userName = StdIn.readLine()
        println("请输入密码：")
        val passwd = StdIn.readLine()
        if (userName == "itcast" && passwd == "heima") {
          println("登录成功！欢迎学习scala")
          break()
        }else{
          times -= 1
          println(s"账号或密码有误，您还有${times}次机会")
        }
      }
    }
  }

}
