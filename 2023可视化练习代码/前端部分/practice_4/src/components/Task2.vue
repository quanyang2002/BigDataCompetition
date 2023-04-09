<template>
  <div ref="main" style="width:600px;height:600px;"></div>
</template>

<script>
export default {
  name: "Task2",
  data(){
    return {
      option:{}
    }
  },
  methods:{
    getPage(){
      let tmp = {
        title: {
          text: '每月停车总次数占总次数展示'
        },
        xAxis:{},
        yAxis:{},
        series:[
          {
            type:'pie',
            data:[]
          }
        ]
      }
      this.$axios.get("http://127.0.0.1:5000/task2/data").then(response => {
        tmp.series[0].data = response.data.data
        this.option = tmp
        this.chart = this.$echarts.init(this.$refs.main)
        this.chart.setOption(this.option)
      })
    }
  },
  mounted(){
    this.getPage()
  }
}
</script>

<style scoped>

</style>
