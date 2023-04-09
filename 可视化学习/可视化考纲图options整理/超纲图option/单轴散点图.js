var option = {
	title:{
		text:"单轴散点图标题",
		// top参数仅用于图表题排版 非必须
		top:((单轴散点图序号 + 0.5) * 100) / 单轴散点图数量 + “%”
	},
	singleAxis:{
		type:"category",
		data:轴标签数据
		// 该top参数用法同上述一样
		top:((单轴散点图序号 + 0.5) * 100) / 单轴散点图数量 + 5 + “%”
		height:100/ 单轴散点图个数 - 10 + “%”,
		axisLabel:{
			interval:表示轴标签间隔几个值
		}
	},
	series:[
			{
				type:"scatter",
				coordinateSystem:"singleAxis",
				data:用于展示的数据,
				symbolSize: function(形参名称) {
					return 对形参转换之后的数据
				}
			}
		]
}