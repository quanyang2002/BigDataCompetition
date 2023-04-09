<template>
<div ref="main" style="width:600px;height:600px"></div>
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
        title:{
          text:'前五热门户型房屋均价展示'
        },
        xAxis:{
          type:'value'
        },
        yAxis:{
          type:'category',
          data:[]
        },
        series:[
          {
            type:'bar',
            data:[]
          }
        ]
      }
      this.$axios.get("http://127.0.0.1:5000/task2/data").then(response => {
        tmp.yAxis.data = response.data[0]
        tmp.series[0].data = response.data[1]
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
