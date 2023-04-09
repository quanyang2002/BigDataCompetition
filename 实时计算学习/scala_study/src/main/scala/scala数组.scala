object scala数组 {

  def main(args: Array[String]): Unit = {

    // 定义一个长度为10的整型数组，且设置第一个元素为11并打印第一个元素
    val arr0 = new Array[Int](10)
    arr0(0) = 11
    println(arr0(0))
    // 定义一个包含"java" "scala" "python"三个元素的数组
    val arr1 = Array("java","scala","python")
    println(arr1.length)
    println(arr1.size)

  }

}