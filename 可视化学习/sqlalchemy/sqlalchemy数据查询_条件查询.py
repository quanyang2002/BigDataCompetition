import sqlalchemy
# 父类的结构定义
import sqlalchemy.ext.declarative
# orm的所有工具
import sqlalchemy.orm
# 数据库操作的核心
import sqlalchemy.orm.session

# 定义mysql数据库方言以及连接地址
mysql_url = "mysql+mysqlconnector://root:password@localhost:3306/sqlalchemyStudy"

# 构建表结构映射类
class User(sqlalchemy.ext.declarative.declarative_base()):
    # 数据库表名称
    __tablename__ = "user"
    # 映射表字段
    uid = sqlalchemy.Column(sqlalchemy.Integer,primary_key=True)
    uname = sqlalchemy.Column(sqlalchemy.String)
    uage = sqlalchemy.Column(sqlalchemy.Integer)
    def __repr__(self):
        return "用户编号：%s，用户名：%s，用户年龄：%s"%(self.uid,self.uname,self.uage)

def main():
    # 配置连接器
    engine = sqlalchemy.create_engine(mysql_url,encoding="UTF-8",echo=True)
    # 创建session类型
    sqlalchemy.orm.session.Session = sqlalchemy.orm.sessionmaker(bind=engine)
    # 实例化session对象
    session = sqlalchemy.orm.session.Session()
    # 通过session.query()方法查找到数据对象
    # 根据id的查询有默认的实现session.query(User).get(1)
    #.all() 相当于执行了查询获取到了数据 不加.all()代表只定义了查询语句没有执行

    # 查找uid为3的用户信息
    # user = session.query(User).filter_by(uid=3).all()
    # 查找年龄大于20岁的用户信息
    # user = session.query(User).filter(User.uage>20).all()
    # 分页查询 设置设置偏移量和获取数据的数量
    # user = session.query(User).filter(User.uage>20).offset(0).limit(2).all()
    # 查找uid在某个范围内的用户信息
    user = session.query(User).filter(User.uid.in_([1,2,3])).all()
    print(user)
    # 释放连接
    session.close()

if __name__=="__main__":
    main()