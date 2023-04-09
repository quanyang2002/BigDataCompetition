object scala返回值类型推断 {

  def digui(n:Int):Int = {
    if (n == 1){
      return n
    }else{
      return n*digui(n-1)
    }
  }
  def main(args: Array[String]): Unit = {
    println(digui(3))

  }

}
