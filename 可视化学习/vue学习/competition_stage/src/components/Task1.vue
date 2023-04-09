<template>
  <div style="width:600px;height:600px" ref="chart"></div>
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
    getPage() {
      let tmp = {
        title:{
          text:'简单图'
        },
        xAxis:{
          type:'category',
          data:null
        },
        yAxis:{
          type:'value'
        },
        series:[
          {
            type:'line',
            data:null
          }
        ]
      }
      this.chart = this.$echarts.init(this.$refs.chart);
      this.$axios.get("http://127.0.0.1:5000/getTask1Data").then(response=>{
        tmp.xAxis.data=response.data[0]
        tmp.series[0].data=response.data[1]
        this.option=tmp
        console.log(this.option)
        this.chart.setOption(this.option);
      });

      // this.$axios.get("http://127.0.0.1:5000/getTask1Data").then(response=>(tmp.series[0].data=response.data[1]));

      // console.log(this.option.xAxis.data)
      // console.log(this.option.series[0].data)


    }
  },
  mounted() {
    this.getPage();
  }
}
</script>

<style scoped>

</style>
