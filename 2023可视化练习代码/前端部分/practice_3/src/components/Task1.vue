<template>
<div ref="main" style="width:12000px;height:600px"></div>
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
        title:{
          text:'各小区二手房均价展示',
          left:'center'
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
      this.$axios.get("http://127.0.0.1:5000/task1/data").then(response => {
        tmp.xAxis.data = response.data[0]
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
