import sqlalchemy
from sqlalchemy.ext.declarative import declarative_base
import sqlalchemy.orm
import sqlalchemy.orm.session

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

sql_url = "mysql+mysqlconnector://root:password@localhost:3306/spider"

engine = sqlalchemy.create_engine(sql_url,encoding="UTF-8",echo=True)

sqlalchemy.orm.session.Session = sqlalchemy.orm.sessionmaker(bind=engine)

session = sqlalchemy.orm.session.Session()

cities = session.query(canyin)
dic = {}
for city in cities:
    dic[city.城市] = dic.get(city.城市,0) + 1

lst = list(dic.items())
lst.sort(key=lambda x:x[1],reverse=True)
for item in lst:
    print("==1: 城市 {}=餐厅数为{}个===".format(item[0],item[1]))

session.close()
