<template>
<div ref="main" style="width:600px;height:600px"></div>
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
      let tmp = {
        legend:{
          data:['本月播放量','上月播放量','总播放量'],
          orient:'vertical',
          left:'right',
          top:'middle'
        },
        title:{
          text:'歌曲本月上月总播放量对比'
        },
        xAxis:{
          type:'category',
          data:[],
          axisLabel:{
            rotate:90
          }
        },
        yAxis:{
          type:'value'
        },
        series:[
          {
            name:'本月播放量',
            type:'bar',
            stack:'播放量',
            data:[],
            label:{
              show:true,
              position:'insideTop'
            }
          },
          {
            name:'上月播放量',
            type:'bar',
            stack:'播放量',
            data:[],
            label:{
              show:true,
              position:'inside'
            }
          },
          {
            name:'总播放量',
            type:'bar',
            stack:'播放量',
            data:[],
            label:{
              show:true,
              position:'outside'
            }
          }
        ]
      }
      this.$axios.get('http://127.0.0.1:5000/task3/data').then(response=>{
        tmp.xAxis.data=response.data[0]
        tmp.series[0].data = response.data[1]
        tmp.series[1].data = response.data[2]
        tmp.series[2].data = response.data[3]
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
