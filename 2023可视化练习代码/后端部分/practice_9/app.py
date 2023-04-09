from flask import Flask
from flask_cors import CORS
from flask import jsonify

from practice_9.dao.distribution_operation_api import distribution_operation
from practice_9.dao.distribution_platform_data_api import distribution_platform_data
from practice_9.dao.distribution_platform_api import distribution_platform
from practice_9.dao.other_store_data_api import other_store_data
from practice_9.dao.order_data_api import order_data
from practice_9.dao.store_operation_data_api import store_operation_data
from practice_9.dao.store_basic_informations_api import store_basic_informations
app = Flask(__name__)

CORS(app,resource=r"/*")

def return_stand(data,msg):
    dic = {}
    dic['code'] = 0
    dic['message'] = msg
    dic['data'] = data
    return jsonify(dic)

@app.route('/distribution_operation/data')
def distribution_operation_data():
    return return_stand(distribution_operation,"distribution_operation's data")

@app.route('/distribution_platform_data/data')
def distribution_platform_data_data():
    return return_stand(distribution_platform_data,"distribution_platform_data's data")

@app.route('/distribution_platform/data')
def distribution_platform_data1():
    return return_stand(distribution_platform,"distribution_platform's data")

@app.route('/other_store_data/data')
def other_store():
    return return_stand(other_store_data,"other_store's data")

@app.route('/order_data/data')
def order():
    return return_stand(order_data,"order's data")

@app.route('/store_operation_data/data')
def store_operation():
    return return_stand(store_operation_data,"store_operaiotn's data")

@app.route('/store_basic_informations/data')
def store_basic_informations_data():
    return return_stand(store_basic_informations,"store_basic_informations's data")

if __name__=="__main__":
    app.run(debug=True)