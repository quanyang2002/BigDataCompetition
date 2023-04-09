from flask import Flask
from flask_cors import CORS
from flask import jsonify

from practice_4.dao.Taks1_data import task1_x,task1_y
from practice_4.dao.Taks2_data import task2_x,task2_y
from practice_4.dao.Task3_data import dates,avgs

app = Flask(__name__)

CORS(app,resource=r'\*')

# 编写restful统一返回格式
def return_stand(message,dat):
    dic = {}
    dic['code'] = 0
    dic['message'] = message
    dic['data'] = dat
    return jsonify(dic)

@app.route("/task1/data")
def task1():
    return return_stand("task1's data",[task1_x,task1_y])
@app.route("/task2/data")
def task2():
    result = []
    for i in range(len(task2_x)):
        tmp = {}
        tmp['name'] = task2_x[i]
        tmp['value'] = task2_y[i]
        result.append(tmp)
    return return_stand("task2's data",result)
@app.route("/task3/data")
def task3():
    return return_stand("task3's data",[dates,avgs])

if __name__=="__main__":
    app.run(debug=True)