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

mysql_url = "mysql+mysqlconnector://root:password@localhost:3306/spider"

engine = sqlalchemy.create_engine(mysql_url)

sqlalchemy.orm.session.Session = sqlalchemy.orm.session.sessionmaker(bind=engine)

session = sqlalchemy.orm.session.Session()

canyins = session.query(canyin)

task4_x = ['北京','上海','南京']
task4_y = []

for city in task4_x:
    tmp = []
    for canyin in canyins:
        if city == canyin.城市:
            tmp.append(canyin.客单价)
    task4_y.append(round(sum(tmp)/len(tmp),1))
print(task4_x)
print(task4_y)

session.close()