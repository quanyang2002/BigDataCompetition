var option = {
	title:[
		{
			text:"主标题",
			left:"center"
		},
		{
			subtext:"副标题1",
			// 标题位置：同一高度，x刻度不同 x按照等分原则
			left:"副标题1 的x方向上的位置",
			top:"副标题1 的y方向上的位置",
			// 确保标题位置在其等分区域内居中
			textAlign:"center"
		}
		...
	],
	series:[
		{
			type:"pie",
			radius:"指定饼图半径",
			center:["50%","50%"],
			data:用于展示的饼图数据,
			// 定义图表位置相关参数
			// 可以想象是在一个盒子中
			left:离盒子左面的距离,
			right:离盒子右面的距离,
			top:离盒子上面的距离,
			bottom:离盒子底面的距离
		}
	]
}