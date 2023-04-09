object scalafor嵌套循环 {

  def main(args: Array[String]): Unit = {

//    for (i <- 1 to 3){
//      for (j <- 1 to 5){
//        print('*')
//      }
//      println()
//    }
    // scala独有
    for (i <- 1 to 3;j <- 1 to 5){
      if (j==5) println("*")
      else print("*")
    }
  }

}
