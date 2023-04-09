from flask import Flask
from flask import jsonify
from flask_cors import CORS

from dao.task2 import task2_x,task2_y
from dao.task3 import task3_x,task3_y
from dao.task5 import task5_x,task5_y
from dao.task4 import task4_x,task4_y

app = Flask(__name__)

CORS(app,resource=r'/*')

def return_stand(data,msg):
    dic = {}
    dic['code'] = 0
    dic['message'] = msg
    dic['data'] = data
    return jsonify(dic)

@app.route('/task2/data')
def task2():
    result = [task2_x,task2_y]
    return return_stand(result,"task2_data")

@app.route('/task3/data')
def task3():
    result = [task3_x,task3_y]
    return return_stand(result,"task3_data")

@app.route('/task5/data')
def task5():
    result = [task5_x,task5_y]
    return return_stand(result,"task5_data")

@app.route('/task4/data')
def task4():
    result = [task4_x,task4_y]
    return return_stand(result,"task4_data")

if __name__=="__main__":
    app.run(debug=True)
