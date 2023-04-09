object scala方法 {

//  def getMax(a:Int,b:Int):Int = {
//    return if(a >= b) a else b
//  }
  def getMax(a:Int,b:Int)=if (a>b) a else b

  def main(args: Array[String]): Unit = {

    val max = getMax(10,20)
    println(max)

  }

}
