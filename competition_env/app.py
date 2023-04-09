from flask import Flask
from flask import jsonify

from dao.query_sku_info import base_sku_info_data
from dao.query_user_info import base_user_info_data
from dao.query_base_region import base_region_data
from dao.query_base_province import base_province_data
from dao.query_order_info import base_order_info_data
from dao.query_order_detail import base_order_detail_data

app = Flask(__name__)

def return_restful(messages,data):
    dic = {}
    dic['code'] = 0
    dic['message'] = messages
    dic['data'] = data
    return jsonify(dic)

@app.route("/base_province")
def return_province():
    return return_restful("province's data",base_province_data)
@app.route("/base_region")
def return_region():
    return return_restful("regions's data",base_region_data)
@app.route("/order_detail")
def return_order_detail():
    return return_restful("order_detail's data",base_order_detail_data)
@app.route("/order_info")
def return_order_info():
    return return_restful("order_info's data",base_order_info_data)
@app.route("/sku_info")
def return_sku_info():
    return return_restful("sku_info's data",base_sku_info_data)
@app.route("/user_info")
def return_user_info():
    return return_restful("user_info's data",base_user_info_data)

if __name__ == "__main__":
    app.run(debug=True)