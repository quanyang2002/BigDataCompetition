var mychart = echarts.init(document.getElementById('main'));
var option = {
    xAxis:{
        data:['A','B','C']
    },
    yAxis:{
        type:'value'
    },
    series:[
    {
        type:'line',
        data:[100,340,200]
    }]
};
mychart.setOption(option);