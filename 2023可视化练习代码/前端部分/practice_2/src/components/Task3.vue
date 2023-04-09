<template>
<div ref="main" style="width:600px;height:600px"></div>
</template>

<script>
export default {
  name: "Task3",
  data(){
    return {
      option : {

      }
    }
  },
  methods:{
    getPage(){
      let tmp = {
        title:{
          text:'各城市好评差评投诉平均数对比'
        },
        legend:{
          data:['好评平均数','差评平均数','投诉平均数']
        },
        xAxis:{
          type:'category',
          data:[]
        },
        yAxis:{
          type:'value',
        },
        series:[
          {
            type:'bar',
            name:'好评平均数',
            data:[]
          },
          {
            type:'bar',
            name:'差评平均数',
            data:[]
          },
          {
            type:'bar',
            name:'投诉平均数',
            data:[]
          }
        ]
      }
      this.$axios.get("http://127.0.0.1:5000/task3/data").then(response=>{
        tmp.xAxis.data = response.data[0]
        tmp.series[0].data = response.data[1]
        tmp.series[1].data = response.data[2]
        tmp.series[2].data = response.data[3]
        this.option = tmp
        this.chart = this.$echarts.init(this.$refs.main)
        this.chart.setOption(this.option)
      })
    }
  },
  mounted() {
    this.getPage()
  }
}
</script>

<style scoped>

</style>
