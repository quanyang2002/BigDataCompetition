<template>
  <div style="width:600px;height:600px" ref="chart"></div>
</template>

<script>
export default {
  name: "Task2",
  data(){
    return {
      option:{
        title:{
          text:'简单图2'
        },
        xAxis:{
          type:'category',
          data:['A','B','C']
        },
        yAxis:{
          type:'value'
        },
        series:[
          {
            type:'bar',
            data:[1,5,2]
          }
        ]
      }
    }
  },
  methods:{
    getPage() {
      this.chart = this.$echarts.init(this.$refs.chart);
      this.chart.setOption(this.option);
    }
  },
  mounted() {
    this.getPage();
  }
}
</script>

<style scoped>

</style>
