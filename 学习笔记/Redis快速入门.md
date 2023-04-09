### Redis快速入门

#### 什么是Redis

远程字典服务器，**Re**mote **di**ctionary **s**erver

一个开源的基于**内存存储**的数据库，常用作**键值存储、缓存和消息队列**等

Redis它通常将全部数据存储在内存中，也可以不时（默认两秒）的将数据写入硬盘实现持久化，这里的持久化仅用于重新启动Redis后将数据加载回内存

#### Redis基本操作

**redis默认有16个数据库，默认选择0号数据库**

数据库操作

##### 选择数据库

````shell
select 数据库编号[0,15]
````

##### 添加数据

````shell
set 键名 值名
````

##### 查看数据库中的键值对数量

````shell
dbsize
````

##### 清空当前数据库

````shell
flushdb
````

##### 清空所有数据库

````shell
flushall
````

##### 将数据保存至磁盘

````shell
save
````

##### 将数据异步保存至磁盘

````shell
bgsave
````

##### 获取最后一次成功保存的unix时间

````shell
lastsave
````

数据操作

##### 查看符合指定格式的key，*为通配符

````shell
keys 格式
````

##### 查看是否存在指定的key

````shell
exists 键名...
````

**注意：...表示可以有多个！！！**

##### 查看指定key对应的value的类型

````shell
type 键名
````

##### 删除键值对

````shell
del 键名...
````

##### 重命名键

````shell
rename k1 k2
````

**注意：如果k2存在，则会用原先k1对应的value覆盖k2的value！！！**

##### 不覆盖原值重命名

````shell
renamenx k1 k2
````

##### 按照key将一个键值对移动到指定数据库

````shell
move 键名 数据库编号
````

##### 按照键名称拷贝value

````shell
copy 键名1 键名2
````

##### 获取key对应的value

````shell
get 键名
````

#### Redis字符串

##### 添加/修改一个键值对

````shell
set 键名 值名
````

##### 按照key获取value

````shell
get 键名
````

##### 添加/修改一个或多个键值对

````shell
mset 键名1 值1 键名2 值2...
````

##### 按照key获取一个或多个value

````shell
mget 键1 键2...
````

##### 按照key在原有value的基础上追加数据

````shell
append 键 追加的值
````

##### 按照key查看value的长度

````shell
strlen 键
````

##### 按照起始索引和key获取对应value的子串

````shell
getrange 键 开始索引 结束索引
````

**注意：在redis中的字符串的索引下标规则和python一致！！！**

**注意：在redis中执行某个命令希望只有当键不存在时，才进行相关操作，则只需要在命令后加上nx！！！**

**注意：在redis中执行某个命令希望只有当键存在时，才进行相关操作，则只需要在命令后加上xx！！！**

字符串内容为整型数字

##### 按照key给指定value自增1

````
incr 键
````

##### 按照key给指定value增加指定值

````shell
incr 键 值
````

##### 按照key给指定value自减1

````
decr 键
````

##### 按照key给指定value减少指定值

````shell
decr 键 值
````

#### 临时键值对

生存时间time to live，缩写为ttl，是指键值对距离被删除的剩余秒数

如果键值对被重新set ，则ttl将会被重置

##### 设定生存时间

````shell
expire 键 秒数
````

##### 查看生存时间剩余秒数

````shell
ttl 键
````

##### 毫秒版生存时间

````shell
pexpire 键 毫秒数
````

##### 查看毫秒生存时间所剩余的毫秒数

````shell
pttl 键
````

##### 取消生存周期(持久化)

````shell
persist 键
````

![image-20221120165826543](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\redis基础操作1.png)

#### 散列表

key-field-value 键-字段-值

![image-20221120180939510](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\Redis散列表.png)

Redis散列表基本操作

##### 添加/修改一个键与一至多对字段和值

````shell
hset 键 field1... value1 ... 
````

##### 按照key和field获取一对value

````shell
hget key field1
````

##### 按照key和field获取多对value

````shell
hmget key field...
````

##### 按照key获取所有的field和value

````shell
hgetall key
````

##### 删除一至多对field-value

````shell
hdel key field1...
````

##### 查看一个散列表中所有的Field

````shell
hkeys key
````

##### 查看散列表中对应key的所有value

````shell
hvals key
````

##### 统计一个散列表中某个key下有多少对field-value

````shell
hlen key
````

##### 查看一个field是否存在

````shell
hexists key field
````

##### 按照key和filed查看value的长度

````shell
hstrlen key field
````

#### Redis列表

数据结构：key-value0-value1-value2-... 键-有序值列队

![image-20221120183224608](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\Redis队列数据结构.png)

##### 从右侧插入数据

````shell
rpush 列表名称 有序value
````

##### 查看列表中的数据

````shell
lrange 列表名称 开始索引 结束索引
````

##### 从左侧插入数据

````shell
lpush 列表名称 有序value
````

##### 从右侧弹出数据

````shell
rpop 列表名称 弹出数据的个数
````

##### 从左侧弹出数据

````shell
lpop 列表名称 弹出数据的个数
````

##### 修改列表指定位置的值

````shell
lset 列表名称 索引 新value
````

##### 在列表指定value位置插入指定值

````shell
linsert 列表名称 after/befor 定位value value
````

##### 按照索引查看值

````shell
lindex key 索引
````

##### 查看列表长度

````shell
llen key
````

##### 删除指定个数指定值

````shell
lrem key 数量 value
````

注意：数量为正数表示从左侧开始删除，数量为负数代表从右侧开始删除

##### 将列表修剪到给定范围

````shell
ltrim key 开始索引 结束索引
````