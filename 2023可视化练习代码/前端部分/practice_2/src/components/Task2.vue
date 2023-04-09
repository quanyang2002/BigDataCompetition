<template>
<div ref="main" style="height:600px;width:600px;"></div>
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
          text:"餐厅聚集地理位置展示"
        },
        legend:{
          data:[]
        },
        xAxis:{},
        yAxis:{},
        series:[
          {
            type:'scatter',
            name:'上海',
            data:[[]],
            symbolSize: function (data) {
              return data[2];
            }
          },
          {
            type:'scatter',
            name:'北京',
            data:[[]],
            symbolSize: function (data) {
              return data[2];
            }
          },
          {
            type:'scatter',
            name:'南京',
            data:[[]],
            symbolSize: function (data) {
              return data[2];
            }
          }
        ]
      };
      this.$axios.get("http://127.0.0.1:5000/task2/data").then(response=>{
        tmp.legend.data = response.data[0]
        tmp.series[0].data[0] = response.data[1][0]
        tmp.series[1].data[0] = response.data[1][1]
        tmp.series[2].data[0] = response.data[1][2]
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
