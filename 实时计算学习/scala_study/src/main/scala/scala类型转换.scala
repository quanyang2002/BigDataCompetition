object scala类型转换 {

  def main(args: Array[String]): Unit = {

    // 自动类型转换：范围小的数据类型会自动转换为范围大的数据类型
    val a:Int = 3
    val b:Double = a+2.21
    // 强制类型转换
    val c:Int = (a+2.21).toInt
    print(c)
    // 值类型与String类型的转换
    // 值类型转换为String类型
    val d = 10
    println(d+"")
    println(d.toString)
    // String类型转换为值类型
    val age = "12"
    println(age.toInt)
    println(age.toCharArray)
  }

}
