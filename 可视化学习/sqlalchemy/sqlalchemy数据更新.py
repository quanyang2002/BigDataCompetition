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

def main():
    # 配置连接器
    engine = sqlalchemy.create_engine(mysql_url,encoding="UTF-8",echo=True)
    # 创建session类型
    sqlalchemy.orm.session.Session = sqlalchemy.orm.sessionmaker(bind=engine)
    # 实例化session对象
    session = sqlalchemy.orm.session.Session()
    # 实例化要插入的数据对象
    user = User(uid=4,uname="quanhr",uage=21)
    # 对象操作=sql处理
    # 聚合操作=更新操作
    session.merge(user)
    # 事务提交
    session.commit()
    # 释放连接
    session.close()

if __name__=="__main__":
    main()