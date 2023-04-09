from flask import Flask
from flask import jsonify
from flask_cors import CORS

from practice_10.dao.gongjiao import data
from practice_10.dao.Task5 import line_data
from practice_10.dao.Task6 import station_data
from practice_10.dao.Task8_1 import take_time
from practice_10.dao.接驳站点数据库可视化后端 import jiebo_data
from practice_10.dao.枢纽站点可视化后端 import shuniu_data

app = Flask(__name__)
CORS(app,resource=r"/*")

def return_stand(data,msg):
    dic = {}
    dic["code"] = 0
    dic["message"] = msg
    dic["data"] = data
    return jsonify(dic)

@app.route("/t_staion_main_gongjiao/data")
def gongjiao():
    return return_stand(data,"gongjiao's data")
@app.route("/公交线路长度统计/data")
def lin_dis():
    return return_stand(line_data,"公交线路长度's data")
@app.route("/all_stations/data")
def od():
    return return_stand(station_data,"stations's data")
@app.route("/take_time/data")
def take():
    return return_stand(take_time,"take_time's data")
@app.route("/jiebo/data")
def jiebo():
    return return_stand(jiebo_data,"jiebo's data")
@app.route("/shuniu/data")
def shuniu():
    return return_stand(shuniu_data,"shuniu's data")

if __name__ == "__main__":
    app.run(debug=True)