import sqlalchemy
from sqlalchemy.ext.declarative import declarative_base
import sqlalchemy.orm
import sqlalchemy.orm.session

class house(declarative_base()):
    __tablename__= "house"
    id = sqlalchemy.Column(sqlalchemy.Integer,)
    name = sqlalchemy.Column(sqlalchemy.String)
    zongjia = sqlalchemy.Column(sqlalchemy.Integer)
    type = sqlalchemy.Column(sqlalchemy.String)
    area = sqlalchemy.Column(sqlalchemy.Integer)
    danjia = sqlalchemy.Column(sqlalchemy.Integer)
    chaoxiang = sqlalchemy.Column(sqlalchemy.String)
    louceng = sqlalchemy.Column(sqlalchemy.String)
    zhuangxiu = sqlalchemy.Column(sqlalchemy.String)
    quyu = sqlalchemy.Column(sqlalchemy.String)

mysql_url = "mysql+mysqlconnector://root:password@localhost:3306/spider"

engine = sqlalchemy.create_engine(mysql_url)

sqlalchemy.orm.session.Session = sqlalchemy.orm.session.sessionmaker(bind=engine)

session = sqlalchemy.orm.session.Session()

houses = session.query(house)

# 先求五个热门户型，再求热门户型的均价
house_types = {}
for house in houses:
    house_types[house.type] = house_types.get(house.type,0)+1

types = list(house_types.items())
print(types)
session.close()
