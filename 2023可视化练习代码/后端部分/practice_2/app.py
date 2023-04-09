from flask import Flask
from flask_cors import CORS
from flask import jsonify

from practice_2.dao.task2 import task2_x,task2_y
from practice_2.dao.task3 import task3_x,task3_z,task3_haop,task3_chap,task3_tousu
from practice_2.dao.task4 import task4_x,task4_y
from practice_2.dao.task5 import dic,task5_x
from practice_2.dao.task6 import task6_x,task6_y

app = Flask(__name__)

CORS(app,resource=r"/*")

@app.route('/task2/data')
def task_1():
    result = [task2_x,task2_y]
    return jsonify(result)

@app.route('/task3/data')
def task_3():
    result = [task3_x,task3_haop,task3_chap,task3_tousu]
    return jsonify(result)

@app.route('/task4/data')
def task_4():
    result = [task4_x,task4_y]
    return jsonify(result)

@app.route('/task5/data')
def task_5():
    result = [task5_x,dic]
    return jsonify(result)

@app.route('/task6/data')
def task_6():
    result = [task6_x,task6_y]
    return jsonify(result)

if __name__=="__main__":
    app.run(debug=True)