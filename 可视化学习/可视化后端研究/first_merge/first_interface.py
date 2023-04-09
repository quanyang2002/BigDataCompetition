from flask import Flask
from flask import jsonify
# from flask_cors import CORS

app = Flask(__name__)
# 解决跨域请求问题
# CORS(app,resource=r'/*')

@app.route('/getTask1Data',methods=['GET'])
def getTask1():
    task1_x = ['A','B','C']
    task1_y = [1,5,6]
    result = [task1_x,task1_y]
    print(jsonify(result))
    return jsonify(result)


if __name__=="__main__":
    app.run()