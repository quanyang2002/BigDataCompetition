object scala字符串定义 {

  def main(args: Array[String]): Unit = {

    val name:String = "hadoop"
    // 使用双引号定义字符串
    println(name,name.length)
    // 使用插值表达式创建字符串
    val name1 = "zhangsan"
    val age = 23
    val sex = '男'
    println(s"name=${name1},age=${age},sex=${sex}")
    // 使用三引号方式定义字符串
    val sql =
      """select * from dbstudent
        |where name = "zhangsan"
        |""".stripMargin
    print(sql)
  }

}
