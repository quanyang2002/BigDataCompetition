object scala惰性赋值 {

  def main(args: Array[String]): Unit = {

    // 惰性赋值与普通赋值的区别：惰性赋值不会立刻将变量值加载到内存中，只有当使用到改变量时，才会进行加载
    // 赋值不会进行加载
    lazy val name = "quanyang"
    // 输出使用时 进行加载
    print(name)

  }

}
