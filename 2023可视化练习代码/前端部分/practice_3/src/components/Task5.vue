<template>
  <div ref="main" style="width:600px;height:600px;"></div>
</template>

<script>
export default {
  name: "Task5",
  data(){
    return {
      option:{}
    }
  },
  methods:{
    getPage(){
      let tmp = {
        title:{
          text:'各区域低中高层房屋数量对比'
        },
        legend:{
          data:['低层房屋数量','中层房屋数量','高层房屋数量']
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
            name:'低层房屋数量',
            type:'bar',
            stack:'房屋总数',
            color:'green',
            data:[]
          },
          {
            name:'中层房屋数量',
            type:'bar',
            stack:'房屋总数',
            color:'red',
            data:[]
          },
          {
            name:'高层房屋数量',
            type:'bar',
            stack:'房屋总数',
            color:'yellow',
            data:[]
          }
        ]
      }
      this.$axios.get("http://127.0.0.1:5000/task5/data").then(response => {
        tmp.xAxis.data = response.data[0]
        tmp.series[0].data = response.data[1][0]
        tmp.series[1].data = response.data[1][1]
        tmp.series[2].data = response.data[1][2]
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
