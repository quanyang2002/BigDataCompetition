from flask import Flask
from flask import jsonify
from flask_cors import CORS

from dao.task1_data import task1_x,task1_y
from dao.task2_data import task2_x,task2_y
from dao.task3_data import task3_x,task3_benyue,task3_shangyue,task3_zong

app = Flask(__name__)

# 跨域问题解决
CORS(app,resource=r'/*')

@app.route('/task1/data')
def task_1():
    result = []
    for item in task1_x:
        tmp = {}
        tmp['name']=item
        tmp['value'] = task1_y[task1_x.index(item)]
        result.append(tmp)
    return jsonify(result)

@app.route('/task2/data')
def task_2():
    result = [task2_x,task2_y]
    return jsonify(result)

@app.route('/task3/data')
def task_3():
    result = [task3_x,task3_benyue,task3_shangyue,task3_zong]
    return jsonify(result)


if __name__=="__main__":
    app.run(debug=True)