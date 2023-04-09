object scala交换变量 {

  def main(args: Array[String]): Unit = {

    var a = 10;
    var b = 20;
    """
      |    a += b
      |    b = a-b
      |    a -= b
      |    println(a,b)
      |""".stripMargin
    var temp = a ^ b;
    a ^= temp;
    b ^= temp;
    println(a,b)
  }

}
