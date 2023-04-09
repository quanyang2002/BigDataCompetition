object scala九九乘法表 {

  def main(args: Array[String]): Unit = {

    for(i <- 1 until 10; j <- 1 to i) if (j==i) println(s"${j}*${i}=${i*j}") else print(s"${j}*${i}=${i*j}\t")
  }

}
