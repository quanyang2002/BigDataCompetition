object scala方法参数 {

  // 参数带有默认值
  def getSum(a:Int=10,b:Int=20) = a+b

  // 定义带有变长参数的方法
  def getSum2(num:Int*) = num.sum

  def main(args: Array[String]): Unit = {

    // 调用方法不传参
    println(getSum())
    // 带名参数
    println(getSum(a=1))
    // 调用带有可变长参数的方法
    // scala中可变长参数在函数参数列表中只能有一个 且只能放在参数列表的最后边
    println(getSum2(1,2,3,4,5))
  }

}
