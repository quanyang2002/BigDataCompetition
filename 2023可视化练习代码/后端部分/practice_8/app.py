from flask import Flask
from flask import jsonify
from flask_cors import CORS

from practice_8.dao.night_api import night
from practice_8.dao.platform_api import platform
from practice_8.dao.platform_rate_api import platform_rate
from practice_8.dao.city_let_rate_api import city_let_rate
from practice_8.dao.radar_lines_api import radar_lines

app = Flask(__name__)

CORS(app,resource=r'/*')

def return_stand(data,msg):
    dic = {}
    dic['code'] = 0
    dic['message'] = msg
    dic['data'] = data
    return jsonify(dic)

@app.route('/night/data')
def night_data():
    return return_stand(night,"night's data")

@app.route('/platform/data')
def platform_data():
    return return_stand(platform,"platform's data")

@app.route('/platform_rate/data')
def platform_rate_data():
    return return_stand(platform_rate,"platform_rate's data")

@app.route('/city_let_rate/data')
def city_let_rate_data():
    return return_stand(city_let_rate,"city_let_rate's data")

@app.route('/radar_lines/data')
def radar_lines_data():
    return return_stand(radar_lines,"radar_lines's data")

if __name__ == "__main__":
    app.run(debug=True)
