object scala标识符 {

  def main(args: Array[String]): Unit = {

    // 命名规则
    // 1、必须由大小写英文字母，数字，下划线_ 美元符号 这四部分任意组合且数字不能开头
    // 2、不能和scala关键字重名
    // 3、最好做到见名知意

    // 命名规范
    // 1、变量或方法，从第二个单词开始，每个单词的首字母都大写，其他字母都小写
    val name = "zhangsan"
    val zhangSanAge = 18
    // 2、类或特质，每个单词首字母都大写，其他字母都小写
    class Person{}
    // 3、包，全部小写，一般是公司域名反写
  }

}
