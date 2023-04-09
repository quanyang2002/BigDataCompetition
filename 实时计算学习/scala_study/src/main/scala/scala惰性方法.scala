object scala惰性方法 {

  def getSum(a:Int,b:Int) = a+b

  def main(args: Array[String]): Unit = {

    // 正常情况下调用即执行
    val res1 = getSum(10,20)
    // 懒加载方式 看样子是调用了getSum方法，但是getSum方法并没有真正执行
    // 注意：lazy不能修饰var类型的变量
    lazy val res2 = getSum(10,20)
    // getSum方法执行
    println(res2)
  }

}
