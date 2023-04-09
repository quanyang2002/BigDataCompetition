object scala循环守卫 {

  def main(args: Array[String]): Unit = {

//    for (i <- 1 to 10){
//      if (i%3==0){
//        println(i)
//      }
//    }
    // 添加守卫
    for (i <- 1 to 10 if i%3==0) println(i)
  }

}
