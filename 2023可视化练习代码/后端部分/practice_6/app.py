from flask import Flask
from flask_cors import CORS
from flask import jsonify

from dao.task1 import task1_x,task1_y
from dao.task2 import task2_x,task2_y
from dao.task3 import task3_x,task3_y,hot_users
from dao.task4 import task4_x,task4_y

app = Flask(__name__)

CORS(app,resource=r"/*")

def return_stand(data,msg):
    dic = {}
    dic['code'] = 0
    dic['message'] = msg
    dic['data'] = data
    return jsonify(dic)

@app.route('/task1/data')
def task1():
    result = [task1_x,task1_y]
    return return_stand(result,"task1’s data")

@app.route('/task2/data')
def task2():
    result = [task2_x,task2_y]
    return return_stand(result,"task1's data")

@app.route('/task3/data')
def task3():
    result = [task3_x,task3_y,hot_users]
    return return_stand(result,"task3's data")

@app.route('/task4/data')
def task4():
    result = [task4_x,task4_y]
    return return_stand(result,"task's data")

if __name__ == "__main__":
    app.run(debug=True)