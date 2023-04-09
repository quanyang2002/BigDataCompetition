### Kafka快速入门

#### Kafka概述

Kafka是一个分布式的基于**发布/订阅**模式的消息队列，主要应用于大数据实时处理领域。

#### 消息队列

![image-20221111202720060](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\Kafka快速入门消息队列同步异步处理区别.png)

#### 使用消息队列的好处

- 解耦
- 可恢复性
- 缓冲
- 灵活性和峰值处理能力
- 异步通信

#### 消息队列的两种模式

点对点模式（一对一，消费者主动拉取数据，消息收到(消费)后消息清除）

![image-20221111203711408](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\kafka快速入门消息队列的点对点模式.png)

**注意：Queue中允许存在多个消费者，但对于一个消息而言，只能有一个消费者可以消费**

发布/订阅模式（一对多，消费者消费数据之后不会清除消息）

![image-20221111204127706](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\kafka快速入门消息队列的发布订阅模式.png)

发布/订阅模式的两种子模式

- 消费者主动拉取数据(需要消费者各自维护一个长轮询)
- 队列推送消息给消费者(各消费者接收数据速度不一致)

#### Kafka基础架构

![image-20221111211312655](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\Kafka快速入门Kafka基础架构.png)

#### Kafka命令行操作

查看当前服务器中的所有topic

````shell
kafka-topics.sh --zookeeper master:2181 --list
````

创建topic

````shell
kafka-topics.sh --zookeeper master:2181 --create --replication-factor 副本数量 --partitions 分区数量 --topic 主题名称
````

删除topic

````shell
kafka-topics.sh --zookeeper master:2181 --delete --topic 要删除的主题名称
````

查看topic详细信息

````shell
kafka-topics.sh --zookeeper master:2181 --describe --topic 主题名称
````

#### Kafka启动生产者消费者测试

启动生产者

````shell
kafka-console-producer.sh --topic 要向哪个主题生产数据 --broker-list master:9092
````

(旧方法)启动消费者

````shell
kafka-console-consumer.sh --topic 要消费哪个主题的数据 --zookeeper master:2181
````

(旧方法)启动消费者（显示旧消息）

````shell
kafka-console-consumer.sh --topic 要消费哪个主题的数据 --zookeeper master:2181 --from-beginning
````

(新方法)启动消费者

````shell
kafka-console-consumer.sh --topic 要消费哪个主题的数据 --bootstrap-server master:9092
````