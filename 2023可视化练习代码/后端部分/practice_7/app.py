from flask import Flask
from flask_cors import CORS
from flask import jsonify

from practice_7.dao.task2 import year_month,provinces,counts
from practice_7.dao.task3 import modules,counts1
from practice_7.dao.task4 import dic

app = Flask(__name__)

CORS(app,resource = r'/*')

def return_stand(data,msg):
    dic = {}
    dic['code'] = 0
    dic['data'] = data
    dic['message'] = msg
    return jsonify(dic)

@app.route('/task2/data')
def task_2():
    result = [year_month,provinces,counts]
    return return_stand(result,"task2's data")

@app.route('/task3/data')
def task_3():
    result = [provinces,counts1,modules]
    return return_stand(result,"task3's data")

@app.route('/task4/data')
def task_4():
    result = [provinces,dic]
    return return_stand(result,"task4's data")

if __name__ == "__main__":
    app.run(debug=True)