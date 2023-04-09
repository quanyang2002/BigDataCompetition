### SQLAlchemy程序开发流程

#### 导入相关包

````python
import sqlalchemy
# 表父类的结构定义
import sqlalchemy.ext.declarative
# orm的所有工具
import sqlalchemy.orm
# 数据库操作会话
import sqlalchemy.orm.session
````

#### 定义数据库连接地址

````python
mysql_url = "mysql+mysqlconnector://root:password@localhost:3306/sqlalchemyStudy"
````

其中：

- mysql 表示连接MySQL数据库
- mysqlconnector 表示使用mysqlconnector工具连接数据库
- root 表示登录MySQL的用户名
- password 表示MySQL中root用户对应的密码
- localhost 表示具体连接的是哪台机器上的MySQL
- 3306 表示MySQL的端口号
- sqlalchemyStudy 表示要连接到哪个数据库

#### 依据MySQL数据库中表的结构创建表映射类

##### MySQL数据库中表的结构

![image-20221027165407530](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\sqlAlchemy开发流程构建表映射查看MySQL表结构】.png)

##### 依据MySQL数据表结构创建映射类

````python
# 构建表结构映射类 继承结构定义父类
class User(sqlalchemy.ext.declarative.declarative_base()):
    # 数据库表名称
    __tablename__ = "user"
    # 映射表字段
    uid = sqlalchemy.Column(sqlalchemy.Integer,primary_key=True)
    uname = sqlalchemy.Column(sqlalchemy.String)
    uage = sqlalchemy.Column(sqlalchemy.Integer)
````

#### 根据定义好的数据库连接地址创建连接器

````python
# 配置连接器
engine = sqlalchemy.create_engine(mysql_url,encoding="UTF-8",echo=True)
````

其中：

- encoding 表示设置字符编码为UTF-8
- echo 表示输出所有执行流程信息

#### 根据连接器创建session类型并实例化session对象

````python
# 创建session类型
sqlalchemy.orm.session.Session = sqlalchemy.orm.sessionmaker(bind=engine)
# 实例化session对象
session = sqlalchemy.orm.session.Session()
````

**注意：bind表绑定之前创建的连接器**

#### 根据操作类型执行相应的session方法

##### 增加数据

````python
# 实例化要插入的数据对象
user = User(uid=4,uname="quanyang",uage=20)
# 对象操作=sql处理
session.add(user)
````

##### 删除数据

````python
# 对象操作=sql处理
# 删除uid为4的数据=查询uid为4的数据实体+删除数据实体
user = session.query(User).get(4)
session.delete(user)
````

##### 更新数据

````python
# 实例化要更新的数据对象
user = User(uid=4,uname="quanhr",uage=21)
# 对象操作=sql处理
# 聚合操作(旧数据与新数据的聚合)=更新操作
session.merge(user)
````

##### 查询数据

````python
# 查找uid为3的用户信息
user = session.query(User).filter_by(uid=3).all()
# 或者
user = session.query(User).get(3)
# 查找年龄大于20岁的用户信息
user = session.query(User).filter(User.uage>20).all()
# 分页查询 设置设置偏移量和获取数据的数量
user = session.query(User).filter(User.uage>20).offset(0).limit(2).all()
# 查找uid在某个范围内的用户信息
user = session.query(User).filter(User.uid.in_([1,2,3])).all()
````

#### 提交事务

````python
# 事务提交
session.commit()
````

**注意：该步骤不可少！！**

#### 释放连接

````python
# 释放连接
session.close()
````