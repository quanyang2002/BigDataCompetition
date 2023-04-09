# redis笔记

##  redis官网

​	http://redis.io/

​	http://www.redis.cn/

## redis数据库的一些概念及操作

​	默认16个数据库，类似数组下表从零开始，初始默认使用零号库；
​	统一密码管理，16个库都是同样密码，要么都OK要么一个也连接不上，redis默认端口是6379；
​	select命令切换数据库：select 0-15；
​	dbsize：查看当前数据库的key的数量；
​	flushdb：清空当前库；
​	flushall；通杀全部库；

## redis的五大数据类型

### 	string（字符串）

​		string是redis最基本的类型，你可以理解成与Memcached一模一样的类型，一个key对应一个value；
​		string类型是二进制安全的。意思是redis的string可以包含任何数据。如jpg图片或者序列化的对象 ；
​		string类型是Redis最基本的数据类型，一个redis中字符串value最多可以是512M；

### 	list（列表）

​		redis 列表是简单的字符串列表，按照插入顺序排序。你可以添加一个元素导列表的头部（左边）或者尾部（右边）。它的底层实际		是个链表。

### 	set（集合）

​		redis的set是string类型的无序集合。它是通过HashTable实现的。

### 	hash（哈希，类似java里的Map）

​		redis的hash 是一个键值对集合；
​		redis hash是一个string类型的field和value的映射表，hash特别适合用于存储对象；
​		类似Java里面的Map<String,Object>；

### 	zset(sorted set：有序集合)

​		redis的zset 和 set 一样也是string类型元素的集合,且不允许重复的成员；
​		不同的是每个元素都会关联一个double类型的分数；
​		redis正是通过分数来为集合中的成员进行从小到大的排序。zset的成员是唯一的,但分数(score)却可以重复；

## redis常见数据类型操作命令参考网址

​	http://redisdoc.com/

## redis常用命令

### key操作命令

#### 获取所有键

> 语法：keys pattern

```text
127.0.0.1:6379> keys *
1) "javastack"
```

- *表示通配符，表示任意字符，会遍历所有键显示所有的键列表，时间复杂度O(n)，在生产环境不建议使用。

#### 获取键总数

> 语法：dbsize

```text
127.0.0.1:6379> dbsize
(integer) 6
```

获取键总数时不会遍历所有的键，直接获取内部变量，时间复杂度O(1)。

#### 查询键是否存在

> 语法：exists key [key ...]

```text
127.0.0.1:6379> exists javastack java
(integer) 2
```

查询查询多个，返回存在的个数。

#### 删除键

> 语法：del key [key ...]

```text
127.0.0.1:6379> del java javastack
(integer) 1
```

可以删除多个，返回删除成功的个数。

#### 查询键类型

> 语法： type key

```text
127.0.0.1:6379> type javastack
string
```

#### 移动键

> 语法：move key db

如把javastack移到2号数据库。

```text
127.0.0.1:6379> move javastack 2
(integer) 1
127.0.0.1:6379> select 2
OK
127.0.0.1:6379[2]> keys *
1) "javastack"
```

#### 查询key的生命周期（秒）

> 秒语法：ttl key
> 毫秒语法：pttl key

```text
127.0.0.1:6379[2]> ttl javastack
(integer) -1
```

-1：永远不过期。

#### 设置过期时间

> 秒语法：expire key seconds
> 毫秒语法：pexpire key milliseconds

```text
127.0.0.1:6379[2]> expire javastack 60
(integer) 1
127.0.0.1:6379[2]> ttl javastack
(integer) 55
```

#### 设置永不过期

> 语法：persist key

```text
127.0.0.1:6379[2]> persist javastack
(integer) 1
```

#### 更改键名称

> 语法：rename key newkey

```text
127.0.0.1:6379[2]> rename javastack javastack123
OK
```



### 字符串(String) 操作命令

字符串是Redis中最基本的数据类型，单个数据能存储的最大空间是512M。

#### 存放键值

> 语法：set key value [EX seconds] [PX milliseconds] [NX|XX]

nx：如果key不存在则建立，xx：如果key存在则修改其值，也可以直接使用setnx/setex命令。

```text
127.0.0.1:6379> set javastack 666
OK
```

#### 获取键值

> 语法：get key

```text
127.0.0.1:6379[2]> get javastack
"666"
```

#### 值递增/递减

如果字符串中的值是数字类型的，可以使用incr命令每次递增，不是数字类型则报错。

> 语法：incr key

```text
127.0.0.1:6379[2]> incr javastack
(integer) 667
```

一次想递增N用incrby命令，如果是浮点型数据可以用incrbyfloat命令递增。

同样，递减使用decr、decrby命令。

#### 批量存放键值

> 语法：mset key value [key value ...]

```text
127.0.0.1:6379[2]> mset java1 1 java2 2 java3 3
OK
```

#### 获取获取键值

> 语法：mget key [key ...]

```text
127.0.0.1:6379[2]> mget java1 java2
1) "1"
2) "2"
```

Redis接收的是UTF-8的编码，如果是中文一个汉字将占3位返回。

#### 获取值长度

> 语法：strlen key
> 127.0.0.1:6379[2]> strlen javastack (integer) 3

#### 追加内容

> 语法：append key value

```text
127.0.0.1:6379[2]> append javastack hi
(integer) 5
```

向键值尾部添加，如上命令执行后由666变成666hi

#### 获取部分字符

> 语法：getrange key start end

```text
> 127.0.0.1:6379[2]> getrange javastack 0 4
"javas"
```

### 集合(Set)操作命令

#### 存储值

> 语法：sadd key member [member ...]

```text
// 这里有8个值（2个java），只存了7个
127.0.0.1:6379> sadd langs java php c++ go ruby python kotlin java
(integer) 7
```

#### 获取元素

> 获取所有元素语法：smembers key

```text
127.0.0.1:6379> smembers langs
1) "php"
2) "kotlin"
3) "c++"
4) "go"
5) "ruby"
6) "python"
7) "java"
```

> 随机获取语法：srandmember langs count

```text
127.0.0.1:6379> srandmember langs 3
1) "c++"
2) "java"
3) "php"
```

#### 判断集合是否存在元素

> 语法：sismember key member

```text
127.0.0.1:6379> sismember langs go
(integer) 1
```

#### 获取集合元素个数

> 语法：scard key

```text
127.0.0.1:6379> scard langs
(integer) 7
```

#### 删除集合元素

> 语法：srem key member [member ...]

```text
127.0.0.1:6379> srem langs ruby kotlin
(integer) 2
```

#### 弹出元素

> 语法：spop key [count]

```text
127.0.0.1:6379> spop langs 2
1) "go"
2) "java"
```

### 有序集合Zset(sorted set)

和列表的区别：

1、列表使用链表实现，两头快，中间慢。有序集合是散列表和跳跃表实现的，即使读取中间的元素也比较快。

2、列表不能调整元素位置，有序集合能。

3、有序集合比列表更占内存。

#### 存储值

> 语法：zadd key [NX|XX] [CH] [INCR] score member [score member ...]

```text
127.0.0.1:6379> zadd footCounts 16011 tid 20082 huny 2893 nosy
(integer) 3
```

#### 获取元素分数

> 语法：zscore key member

```text
127.0.0.1:6379> zscore footCounts tid
"16011"
```

> 获取排名范围排名语法：zrange key start stop [WITHSCORES]

```text
// 获取所有，没有分数
127.0.0.1:6379> zrange footCounts 0 -1
1) "nosy"
2) "tid"
3) "huny"

// 获取所有及分数
127.0.0.1:6379> zrange footCounts 0 -1 Withscores
1) "nosy"
2) "2893"
3) "tid"
4) "16011"
5) "huny"
6) "20082"
```

> 获取指定分数范围排名语法：zrangebyscore key min max [WITHSCORES] [LIMIT offset count]

```text
127.0.0.1:6379> zrangebyscore footCounts 3000 30000 withscores limit 0 1
1) "tid"
2) "16011"
```

#### 增加指定元素分数

> 语法：zincrby key increment member

```text
127.0.0.1:6379> zincrby footCounts 2000 tid
"18011"
```

#### 获取集合元素个数

> 语法：zcard key

```text
127.0.0.1:6379> zcard footCounts
(integer) 3
```

#### 获取指定范围分数个数

> 语法：zcount key min max

```text
127.0.0.1:6379> zcount footCounts 2000 20000
(integer) 2
```

#### 删除指定元素

> 语法：zrem key member [member ...]

```text
127.0.0.1:6379> zrem footCounts huny
(integer) 1
```

#### 获取元素排名

> 语法：zrank key member

```text
127.0.0.1:6379> zrank footCounts tid
(integer) 1
```

#### 获得元素排序

redis Zrevrange 命令基本语法如下：

```
redis 127.0.0.1:6379> zrevrange key start stop [WITHSCORES]
```

实例

```
redis 127.0.0.1:6379> ZRANGE salary 0 -1 WITHSCORES        # 递增排列
1) "peter"
2) "3500"
3) "tom"
4) "4000"
5) "jack"
6) "5000"

redis 127.0.0.1:6379> ZREVRANGE salary 0 -1 WITHSCORES     # 递减排列
1) "jack"
2) "5000"
3) "tom"
4) "4000"
5) "peter"
6) "3500"
```

### 列表(List)操作命令

列表类型是一个有序的字段串列表，内部是使用双向链表实现，所有可以向两端操作元素，获取两端的数据速度快，通过索引到具体的行数比较慢。

列表类型的元素是有序且可以重复的。

#### 存储值

> 左端存值语法：lpush key value [value ...]

```text
127.0.0.1:6379> lpush list lily sandy
(integer) 2
```

> 右端存值语法：rpush key value [value ...]

```text
127.0.0.1:6379> rpush list tom kitty
(integer) 4
```

> 索引存值语法：lset key index value

```text
127.0.0.1:6379> lset list 3 uto
OK
```

#### 弹出元素

> 左端弹出语法：lpop key

```text
127.0.0.1:6379> lpop list
"sandy"
```

> 右端弹出语法：rpop key

```text
127.0.0.1:6379> rpop list
"kitty"
```

#### 获取元素个数

> 语法：llen key

```text
127.0.0.1:6379> llen list
(integer) 2
```

#### 获取列表元素

> 两边获取语法：lrange key start stop

```text
127.0.0.1:6379> lpush users tom kitty land pony jack maddy
(integer) 6

127.0.0.1:6379> lrange users 0 3
1) "maddy"
2) "jack"
3) "pony"
4) "land"

// 获取所有
127.0.0.1:6379> lrange users 0 -1
1) "maddy"
2) "jack"
3) "pony"
4) "land"
5) "kitty"
6) "tom"

// 从右端索引
127.0.0.1:6379> lrange users -3 -1
1) "land"
2) "kitty"
3) "tom"
```

> 索引获取语法：lindex key index

```text
127.0.0.1:6379> lindex list 2
"ketty"

// 从右端获取
127.0.0.1:6379> lindex list -5
"sady"
```

#### 删除元素

> 根据值删除语法：lrem key count value

```text
127.0.0.1:6379> lpush userids 111 222 111 222 222 333 222 222
(integer) 8

// count=0 删除所有
127.0.0.1:6379> lrem userids 0 111
(integer) 2

// count > 0 从左端删除前count个
127.0.0.1:6379> lrem userids 3 222
(integer) 3

// count < 0 从右端删除前count个
127.0.0.1:6379> lrem userids -3 222
(integer) 2
```

> 范围删除语法：ltrim key start stop

```text
// 只保留2-4之间的元素
127.0.0.1:6379> ltrim list 2 4
OK
```

### 散列(Hash)操作命令

redis字符串类型键和值是字典结构形式，这里的散列类型其值也可以是字典结构。

#### 存放键值

> 单个语法：hset key field value

```text
127.0.0.1:6379> hset user name javastack
(integer) 1
```

> 多个语法：hmset key field value [field value ...]

```text
127.0.0.1:6379> hmset user name javastack age 20 address china
OK
```

> 不存在时语法：hsetnx key field value

```text
127.0.0.1:6379> hsetnx user tall 180
(integer) 0
```

#### 获取字段值

> 单个语法：hget key field

```text
127.0.0.1:6379> hget user age
"20"
```

> 多个语法：hmget key field [field ...]

```text
127.0.0.1:6379> hmget user name age address
1) "javastack"
2) "20"
3) "china"
```

> 获取所有键与值语法：hgetall key

```text
127.0.0.1:6379> hgetall user
1) "name"
2) "javastack"
3) "age"
4) "20"
5) "address"
6) "china"
```

> 获取所有字段语法：hkeys key

```text
127.0.0.1:6379> hkeys user
1) "name"
2) "address"
3) "tall"
4) "age"
```

> 获取所有值语法：hvals key

```text
127.0.0.1:6379> hvals user
1) "javastack"
2) "china"
3) "170"
4) "20"
```

#### 判断字段是否存在

> 语法：hexists key field

```text
127.0.0.1:6379> hexists user address
(integer) 1
```

#### 获取字段数量

> 语法：hlen key

```text
127.0.0.1:6379> hlen user
(integer) 4
```

#### 递增/减

> 语法：hincrby key field increment

```text
127.0.0.1:6379> hincrby user tall -10
(integer) 170
```

#### 删除字段

> 语法：hdel key field [field ...]

```text
127.0.0.1:6379> hdel user age
(integer) 1
```

### Redis中的遍历

#### 遍历整个数据库

````shell
# 从0开始 会返回遍历结束的游标地址
scan 0
# 从0开始 遍历符合要求的key
scan 0 match h*
# 从0开始 定义遍历结束位置
scan 0 count 10
# 从0开始 遍历指定类型的数据
scan 0 type set
````

#### 特定键值对的遍历

![image-20221207105745445](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\redis特定键值对的遍历.png)