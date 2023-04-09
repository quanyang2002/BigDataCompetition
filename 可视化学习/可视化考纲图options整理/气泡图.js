var option = {
	title:{
		text:'气泡图'
	},
	legend:{
		data:['类别1','类别2',...]
	},
	xAxis:{},
	yAxis:{},
	series:[
		{
			name:'类别1',
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
			name:'类别2',
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