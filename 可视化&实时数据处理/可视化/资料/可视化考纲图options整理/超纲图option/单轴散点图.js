var option = {
	title:{
		text:"����ɢ��ͼ����",
		// top����������ͼ�����Ű� �Ǳ���
		top:((����ɢ��ͼ��� + 0.5) * 100) / ����ɢ��ͼ���� + ��%��
	},
	singleAxis:{
		type:"category",
		data:���ǩ����
		// ��top�����÷�ͬ����һ��
		top:((����ɢ��ͼ��� + 0.5) * 100) / ����ɢ��ͼ���� + 5 + ��%��
		height:100/ ����ɢ��ͼ���� - 10 + ��%��,
		axisLabel:{
			interval:��ʾ���ǩ�������ֵ
		}
	},
	series:[
			{
				type:"scatter",
				coordinateSystem:"singleAxis",
				data:����չʾ������,
				symbolSize: function(�β�����) {
					return ���β�ת��֮�������
				}
			}
		]
}