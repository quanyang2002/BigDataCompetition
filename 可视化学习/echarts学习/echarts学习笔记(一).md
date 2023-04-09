#### echarts学习笔记(一)

##### echarts开发步骤

1. 创建一个新的html文件
2. 在html文件head头部信息中导入echarts
3. 声明一个容器(可以理解为画布)，用于存放echarts
4. 实例化echarts绑定先前创建的容器
5. 编写可视化图标相关配置
6. 将配置应用于我们实例化好的echarts对象
7. 使用浏览器打开即可看到我们绘制的图表

##### 快速入门

###### 使用editplus(可以使用其他的编辑器)新建html文件[快捷键:ctrl+shift+n]

````html
<!DOCTYPE html>
<html lang="en">
 <head>
  <meta charset="UTF-8">
  <meta name="Generator" content="EditPlus®">
  <meta name="Author" content="">
  <meta name="Keywords" content="">
  <meta name="Description" content="">
  <title>Document</title>
 </head>
 <body>
  
 </body>
</html>
````

###### 在html头部信息中导入echarts

````html
<!DOCTYPE html>
<html lang="en">
 <head>
  <meta charset="UTF-8">
  <meta name="Generator" content="EditPlus®">
  <meta name="Author" content="">
  <meta name="Keywords" content="">
  <meta name="Description" content="">
  <script src="放置echarts.min.js文件全路径(也可以是相对路径)"></script>
  <title>Document</title>
 </head>
 <body>
  
 </body>
</html>

````

###### 声明一个容器(可以理解为画布)，用于存放echarts

````html
<!DOCTYPE html>
<html lang="en">
 <head>
  <meta charset="UTF-8">
  <meta name="Generator" content="EditPlus®">
  <meta name="Author" content="">
  <meta name="Keywords" content="">
  <meta name="Description" content="">
  <script src="放置echarts.min.js文件全路径(也可以是相对路径)"></script>
  <title>Document</title>
 </head>
 <body>
  <div id="main" style="height:500px;width:500px"></div>
 </body>
</html>
````

###### 实例化echarts对象绑定之前创建的容器(画布)

````html
<!DOCTYPE html>
<html lang="en">
 <head>
  <meta charset="UTF-8">
  <meta name="Generator" content="EditPlus®">
  <meta name="Author" content="">
  <meta name="Keywords" content="">
  <meta name="Description" content="">
  <script src="放置echarts.min.js文件全路径(也可以是相对路径)"></script>
  <title>Document</title>
 </head>
 <body>
  <div id="main" style="height:500px;width:500px"></div>
  <script type="text/javascript">
	var mychart=echarts.init(document.getElementById("main"));

  </script>
 </body>
</html>

````

###### 编写图表相关配置

````html
<!DOCTYPE html>
<html lang="en">
 <head>
  <meta charset="UTF-8">
  <meta name="Generator" content="EditPlus®">
  <meta name="Author" content="">
  <meta name="Keywords" content="">
  <meta name="Description" content="">
  <script src="放置echarts.min.js文件全路径(也可以是相对路径)"></script>
  <title>Document</title>
 </head>
 <body>
  <div id="main" style="height:500px;width:500px"></div>
  <script type="text/javascript">
	var mychart=echarts.init(document.getElementById("main"));
	var option={
		xAxis:{
			type:"category",
			data:["语文","数学","英语"]
		},
		yAxis:{
			type:"value"
		},
		series:[
		{
			type:"bar",
			data:[100,85,96]
		}
		]
	};
  </script>
 </body>
</html>
````

###### 将编写好的配置应用于我们实例化好的echarts对象

````html
<!DOCTYPE html>
<html lang="en">
 <head>
  <meta charset="UTF-8">
  <meta name="Generator" content="EditPlus®">
  <meta name="Author" content="">
  <meta name="Keywords" content="">
  <meta name="Description" content="">
  <script src="放置echarts.min.js文件全路径(也可以是相对路径)"></script>
  <title>Document</title>
 </head>
 <body>
  <div id="main" style="height:500px;width:500px"></div>
  <script type="text/javascript">
	var mychart=echarts.init(document.getElementById("main"));
	var option={
		xAxis:{
			type:"category",
			data:["语文","数学","英语"]
		},
		yAxis:{
			type:"value"
		},
		series:[
		{
			type:"bar",
			data:[100,85,96]
		}
		]
	};
	mychart.setOption(option);
  </script>
 </body>
</html>
````

###### 使用浏览器打开我们编写好的html文件查看绘制的图像吧！！

![image-20220831222207522](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\echarts.png)

##### 进一步探索

###### 尝试将柱状图改成折线图

如果细心观察，我们会发现，控制图表类型的关键位置就在我们的series中的type属性。

![image-20220831222636920](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\echarts图表类型关键因素.png)

将其修改为"line"试试

![image-20220831222813021](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\echarts图像修改为折线图.png)

now！是不是就变成上面的这样子啦！！

如果我们希望图表的折线可以有一定的圆滑的感觉，可以加上smooth属性

![image-20220831223116608](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\echarts加上smooth属性.png)

图像就会变成这样：

![image-20220831223209100](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\echart入门折线圆滑.png)

现在看起来是不是有些圆滑了呢？只不过图像看起来还是比画布小很多啊！可以使用boundaryGap属性将图像两边贴合画布外围哦！

![image-20220831223617491](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\boundaryGap属性.png)

![image-20220831223708302](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\图像两边贴合.png)

如果我想要让这条线有区域性呢？可以将它修改为面积图

![image-20220831223920109](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\echarts基础面积图属性.png)

![image-20220831223949925](D:\学习\大数据\技能大赛\echarts学习\echarts博客学习截图\echarts基础面积图.png)

##### 总结

通过学习，我们可以了解到使用echarts绘图的基本步骤以及控制图表样式的相关属性，同样还有其他很多属性等待大家一起挖掘哦！