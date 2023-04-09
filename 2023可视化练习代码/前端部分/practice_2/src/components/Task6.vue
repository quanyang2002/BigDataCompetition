<template>
<div ref="main" style="width:600px;height:600px"></div>
</template>

<script>
export default {
  name: "Task6",
  data(){
    return {
      option:{}
    }
  },
  methods:{
    getPage(){
      let tmp = {
        title:{
          text:'各个城市餐厅好差评对比'
        },
        legend:{
          data:['好评数','差评数']
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
            name:'好评数',
            stack:'评论总数',
            color:'green',
            data:[]
          },
          {
            type:'bar',
            name:'差评数',
            stack:'评论总数',
            color:'red',
            data:[]
          }
        ]
      }
      this.$axios.get("http://127.0.0.1:5000/task6/data").then(response => {
        tmp.xAxis.data = response.data[0]
        tmp.series[0].data = response.data[1][0]
        tmp.series[1].data = response.data[1][1]
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
