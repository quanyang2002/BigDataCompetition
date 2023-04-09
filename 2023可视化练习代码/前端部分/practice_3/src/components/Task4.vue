<template>
  <div ref="main" style="width:600px;height:600px"></div>
</template>

<script>
export default {
  name: "Task4",
  data(){
    return {
      option:{}
    }
  },
  methods:{
    getPage(){
      let tmp = {
        title:{
          text:'各区域房屋数量占比情况'
        },
        legend:{
          data:[]
        },
        xAxis:{
        },
        yAxis:{},
        series:[
          {
            type:'pie',
            data:[],
            label:{
              show:true
            }
          }
        ]
      }
      this.$axios.get("http://127.0.0.1:5000/task4/data").then(response => {
        tmp.legend.data = response.data[0]
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
