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

sqlalchemy.orm.session.Session = sqlalchemy.orm.sessionmaker(bind=engine)

session = sqlalchemy.orm.session.Session()

canyins = session.query(canyin)

task3_x = ["北京","上海","南京"]
task3_y = []
task3_z = ['好评平均数','差评平均数','用户投诉平均数']
for city in task3_x:
    tousu = []
    chapin = []
    haopin = []
    for canyin in canyins:
        if canyin.城市 == city:
            tousu.append(canyin.用户投诉数)
            chapin.append(canyin.差评数)
            haopin.append(canyin.好评数)
    task3_y.append([sum(haopin)/len(haopin),sum(chapin)/len(chapin),sum(tousu)/len(tousu)])
for item in task3_y:
    task3_y[task3_y.index(item)]=list(map(lambda x:round(x,1),item))
print(task3_x)
task3_haop = list(map(lambda x:x[0],task3_y))
task3_chap = list(map(lambda x:x[1],task3_y))
task3_tousu = list(map(lambda x:x[2],task3_y))
print(task3_z)

session.close()
