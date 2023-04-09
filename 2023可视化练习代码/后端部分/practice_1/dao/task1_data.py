import sqlalchemy
import sqlalchemy.ext.declarative
from sqlalchemy.orm import *

mysql_url = "mysql+mysqlconnector://root:password@localhost:3306/spider"

class Music(sqlalchemy.ext.declarative.declarative_base()):
    __tablename__="music"
    name = sqlalchemy.Column(sqlalchemy.String,primary_key=True)
    singer = sqlalchemy.Column(sqlalchemy.String)
    zhuanji = sqlalchemy.Column(sqlalchemy.String)
    zuoci = sqlalchemy.Column(sqlalchemy.String)
    zuoqu = sqlalchemy.Column(sqlalchemy.String)
    faxinggongsi = sqlalchemy.Column(sqlalchemy.String)
    faxingtime = sqlalchemy.Column(sqlalchemy.String)
    shichang = sqlalchemy.Column(sqlalchemy.String)
    score = sqlalchemy.Column(sqlalchemy.String)
    style = sqlalchemy.Column(sqlalchemy.String)
    shangyuebofang = sqlalchemy.Column(sqlalchemy.Integer)
    benyue = sqlalchemy.Column(sqlalchemy.Integer)

engine = sqlalchemy.create_engine(mysql_url,encoding="UTF-8")

sqlalchemy.orm.session.Session = sqlalchemy.orm.sessionmaker(bind=engine)
session = sqlalchemy.orm.session.Session()

musics_list = session.query(Music)

data_dic = {}

for music in musics_list:
    gongsi = music.faxinggongsi.strip()
    data_dic[gongsi] = data_dic.get(gongsi,0) + 1

task1_x = list(data_dic.keys())
# print(task1_x)
task1_y = list(map(lambda x:x[1],list(data_dic.items())))
# print(task1_y)



session.close()




