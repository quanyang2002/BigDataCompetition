var option = {
	title:{
		text:'����ͼ'
	},
	legend:{
		data:['���1','���2',...]
	},
	xAxis:{},
	yAxis:{},
	series:[
		{
			name:'���1',
			type:'scatter',
			data:[
				[x1,y1,z1],
				[x2,y2,z2],
				...
			],
			symbolSize:function(data) {
				return data[2];
			}
		},
		{
			name:'���2',
			type:'scatter',
			data:[
				[x1,y1,z1],
				[x2,y2,z2],
				...
			],
			symbolSize:function(data) {
				return data[2];
			}
		},
			...
	]
}