object scala块表达式 {

  def main(args: Array[String]): Unit = {

    val a = {
      println("1+1")
      1 + 1
    }
    println(a)
  }

}
