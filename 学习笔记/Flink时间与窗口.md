### Flink时间与窗口

#### Flink window API

##### 窗口

窗口就是将无限流切割为有限流的一种方式，它会将流数据分发到有限大小的桶(bucket)中进行分析

##### 窗口类型

- 时间窗口(按照时间进行开窗)
- 计数窗口(按照桶中的数据个数进行开窗)

###### 滚动窗口

**decr：将数据依据固定的窗口长度对数据进行切分**

![image-20221129100001592](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\滚动窗口图片.png)

特点：时间对齐，窗口长度固定，没有重叠

###### 滑动窗口

**decr：滑动窗口是固定窗口的更广义的一种形式，滑动窗口由固定的窗口长度和滑动间隔组成**

![image-20221129100522203](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\滑动窗口.png)

特点：窗口长度固定，可以有重叠

###### 会话窗口

**decr：由一系列事件组合一个指定时间长度的timeout间隙组成，也就是一段时间没有接收到新数据就会生成新的窗口**

![image-20221129101221474](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\会话窗口图片.png)

特点：时间无对齐

##### 窗口分配器

**针对要进行开窗的数据流，必须先分组转换为keyedStream，然后才可以进行window操作**

````scala
    stream
      .flatMap(line => line.split(" "))
      .keyBy(0)
//      .window(TumblingEventTimeWindows.of(Time.seconds(15))) // 滚动窗口
//      .window(SlidingProcessingTimeWindows.of(Time.seconds(15),Time.seconds(5))) // 滑动窗口 窗口大小为15秒 滑动距离为5秒
//      .window(EventTimeSessionWindows.withGap(Time.seconds(10)))// 会话窗口 如果两条数据之间的时间差在10秒则属于同一个窗口。否则不属于一个窗口
      .timeWindow(Time.seconds(10))// 传一个参数相当于滚动窗口，传两个参数相当于滑动窗口
//      .countWindow(Time.seconds(1)) // 传参同timeWindow  用于计数
````

##### 窗口函数

window function 定义了要对窗口中收集的数据做的计算操作

###### 窗口函数分类

- 增量聚合函数 每条数据进来就进行计算，保持一个简单的状态
- 全窗口函数 先把窗口所有数据收集起来，等到计算的时候会遍历所有数据

###### 其他可选API

- trigger() 触发器 定义window什么时候关闭，触发计算并输出结果
- evictor() 移除器 定义移除某些数据的逻辑
- allowedLateness() 允许处理迟到的数据
- sideOutputLateData() 将迟到的数据放入测输出流
- getSideOutput() 获取侧输出流

#### Flink时间语义

##### 时间语义

![image-20221129143128424](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\Flink时间语义.png)

- Event Time：时间创建的时间
- Ingestion Time：数据进入Flink的时间
- Processing Time：执行操作算子的本地系统时间，与机器相关

##### 事件时间与处理时间的区别

![image-20221129143915899](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\事件时间与处理时间的区别.png)

##### 设置时间语义

````scala
val env = StreamExecutionEnvironment.getExecutionEnvironment
env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
````

**注意：如果不设置时间语义，则默认是处理时间，如果设置为处理时间，则需要指明数据流中哪个字段代表时间**

##### 乱序数据

![image-20221206113626435](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\Flink乱序数据.png)

当Flink以Event Time模式处理数据流时，它会根据数据里的时间戳来处理基于时间的算子

由于网络、分布式等原因，会导致乱序数据的产生
