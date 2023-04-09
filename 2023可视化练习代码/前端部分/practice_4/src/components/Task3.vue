<template>
<div ref="main" style="width:1200px;height:600px"></div>
</template>

<script>
export default {
  name: "Task3",
  data(){
    return {
      option:{}
    }
  },
  methods:{
    getPage(){
      let tmp={
        title:{
          text:'1-10每天车辆平均停留时间'
        },
        xAxis:{
          type:'category',
          data:[]
        },
        yAxis:{
          type:'value'
        },
        series:[
          {
            type:'bar',
            data:[]
          }
        ]
      }
      this.$axios.get("http://127.0.0.1:5000/task3/data").then(response => {
        tmp.xAxis.data = response.data.data[0]
        tmp.series[0].data = response.data.data[1]
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
