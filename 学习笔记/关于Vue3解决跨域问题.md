### 关于 Vue3 解决跨域问题

#### 跨域问题

当我们编写的前端页面通过axios等网络请求库向后端接口请求数据时，这通常涉及到跨端口访问数据，因此涉及到跨域问题。

#### 解决方法

- 通过在后端配置允许跨域访问资源
- 在前端配置跨域代理

#### Vue3项目如何解决跨域问题

使用编辑器打开创建好的vue3项目根目录下的vue.config.js,将文件中的内容修改如下：

````javascript
const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave:false,
  devServer:{
      // 配置前端宿主机ip
      host:'127.0.0.1',
      // 配置前端项目端口
      port:'8080',
      // 开启
      open:true,
      proxy:{
          '/api':{
              // 请求的后端接口 站点地址
              target: 'http://127.0.0.1:5000',
              // 配置可以切换域
              changeOrigin:true,
              // 配置域名重写
              pathRewrite:{
                  "^/api":"",
              }
          }
      }
  }
})
````

**注意：这里是通过在前端配置代理方式进行跨域访问的！！！**

此时在编写具体的组件时，详细代码如下：

````javascript
<template>
  <div id="main" style="width:600px;height:600px;"></div>
</template>

<script>
export default {
  // eslint-disable-next-line vue/multi-word-component-names
  name: "Task1",
  mounted() {
    this.$axios.get("/api/getTask1Data").then(response => {
      var data_x = response.data[0]
      var data_y = response.data[1]
      var option = {
        title:{
          text:'简单图测试'
        },
        xAxis:{
          type:'category',
          data:data_x
        },
        yAxis:{
          type:'value'
        },
        series:[
          {
            type:'bar',
            data:data_y
          }
        ]
      }
      var mychart = this.$echarts.init(document.getElementById("main"))
      mychart.setOption(option)
    })
  }
}
</script>

<style scoped>

</style>
````

**注意：这里使用axios请求的url中/api会自动转换为我们刚才在vue.config.js中配置的目标路径，所以这里请求的完整地址为：http://127.0.0.1:5000/getTask1Data**

