package Test

object ScalaFloatTest {

  def main(args: Array[String]): Unit = {

    var a = 1
    var b = 3
    println((a.toDouble/b.toDouble).formatted("%.1f").toFloat)
  }

}
