<template>
<div ref="main" style="width:600px;height:600px;"></div>
</template>

<script>
export default {
  name: "Task1",
  data(){
    return {
      option:{}
    }
  },
  methods:{
    getPage(){
      let tmp = {
        legend:{
          show:true,
          left:"center",
          top:"bottom"
        },
        title:{
          text:''
        },
        xAxis:{},
        yAxis:{},
        series:[{
          type:'pie',
          data:[]
        }]
      }
      this.$axios.get("http://127.0.0.1:5000/task1/data").then(response=>{
        let result = response.data
        tmp.series[0].data = result
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
