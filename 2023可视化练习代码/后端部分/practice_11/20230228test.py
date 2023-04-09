from flask import Flask
from flask import jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app,resources=r"/*")

@app.route("/testapi")
def test():
    f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_11\\temp\\test.csv","r+",encoding="gbk")
    data = f.readlines()
    data = list(map(lambda x:x.split(","),data))
    return jsonify(data)

if __name__ == "__main__":
    app.run(debug=True)
