<template>
<div ref="main" style="width:600px;height:600px"></div>
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
          text:'各个城市推单数占比情况'
        },
        legend:{
          data:[]
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
      this.$axios.get("http://127.0.0.1:5000/task5/data").then(response=>{
        tmp.series[0].data = response.data[1]
        tmp.legend.data = response.data[0]
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
