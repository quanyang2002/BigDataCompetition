from flask import Flask
from flask_cors import CORS
from flask import jsonify

from practice_3.dao.task1 import task1_x,task1_y
from practice_3.dao.task2_pymysql import task2_x,task2_y
from practice_3.dao.task3 import task3_x,task3_jinshu,task3_jianshu,task3_maoshu
from practice_3.dao.task4 import task4_x,dic
from practice_3.dao.task5 import task5_x,task5_y

app = Flask(__name__)

CORS(app,resource=r"/*")

@app.route('/task1/data')
def task1():
    result = [task1_x,task1_y]
    return jsonify(result)

@app.route('/task2/data')
def task2():
    result = [task2_x,task2_y]
    return jsonify(result)

@app.route('/task3/data')
def task3():
    result = [task3_x,task3_jinshu,task3_jianshu,task3_maoshu]
    return jsonify(result)

@app.route('/task4/data')
def task4():
    result = [task4_x,dic]
    return jsonify(result)

@app.route('/task5/data')
def task5():
    result = [task5_x,task5_y]
    return jsonify(result)

if __name__=="__main__":
    app.run(debug=True)