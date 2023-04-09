<template>
  <!--模板内容-->
  <!--定义一个有大小的div盒子(相当于画布) ref相当于id-->
  <div style="width:600px;height:600px;" ref="chart"></div>
</template>

<script>
export default {
  name: "Task3",
  data(){
    return {
      option:{
        // echarts图表相关配置
      }
    }
  },
  methods:{
    // 设置画图方法
    getPage(){
      // 实例化echarts对象
      this.chart = this.$echarts.init(this.$refs.chart);
      // 将配置好的图表配置应用到创建好的echarts对象
      this.chart.setOption(this.option);
    }
  },
  mounted(){// 钩子函数，在vue组件实例创建时会自动调用该方法
    this.getPage();
  }
}
</script>

<style scoped>

</style>
