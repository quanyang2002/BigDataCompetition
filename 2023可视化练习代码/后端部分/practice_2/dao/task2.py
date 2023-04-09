import sqlalchemy
from sqlalchemy.ext.declarative import declarative_base
import sqlalchemy.orm
import sqlalchemy.orm.session

from practice_2.dao.task1 import lst


class canyin(declarative_base()):
    __tablename__="canyin"
    餐厅id = sqlalchemy.Column(sqlalchemy.String,primary_key=True)
    城市 = sqlalchemy.Column(sqlalchemy.String)
    客单价 = sqlalchemy.Column(sqlalchemy.Integer)
    推单数 = sqlalchemy.Column(sqlalchemy.Integer)
    接单数 = sqlalchemy.Column(sqlalchemy.Integer)
    整体时长 = sqlalchemy.Column(sqlalchemy.Integer)
    到店时长 = sqlalchemy.Column(sqlalchemy.Integer)
    取餐时长 = sqlalchemy.Column(sqlalchemy.Integer)
    用户投诉数 = sqlalchemy.Column(sqlalchemy.Integer)
    差评数 = sqlalchemy.Column(sqlalchemy.Integer)
    好评数 = sqlalchemy.Column(sqlalchemy.Integer)
class cantin_addr(declarative_base()):
    __tablename__="cantin_addr"
    城市 = sqlalchemy.Column(sqlalchemy.String,primary_key=True)
    经度 = sqlalchemy.Column(sqlalchemy.Integer)
    纬度 = sqlalchemy.Column(sqlalchemy.Integer)

mysql_url = "mysql+mysqlconnector://root:password@localhost:3306/spider"

engine = sqlalchemy.create_engine(mysql_url)

sqlalchemy.orm.session.Session = sqlalchemy.orm.sessionmaker(bind=engine)

session = sqlalchemy.orm.session.Session()

cities = session.query(cantin_addr)

lst = list(map(lambda x:list(x),lst))
task2_x = []
task2_y = []

for city in cities:
    for item in lst:
        if city.城市 == item[0]:
            task2_x.append(city.城市)
            task2_y.append([city.经度,city.纬度,item[1]+20])
print(task2_x)
print(task2_y)

session.close()
