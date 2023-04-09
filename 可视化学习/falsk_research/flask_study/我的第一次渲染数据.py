from flask import *

app = Flask(__name__,template_folder='templates')
# app.config["JSON_AS_ASCII"]=False

@app.route('/first')
def my_first():
    xzhou = ["A",'B','C']
    data = [1000,200,1000]
    # name = '全阳'
    # age = 21
    # mychart = open("./templates/第一次js渲染.js",'r')
    return render_template('简单图(测试用).html',xzhou=xzhou,data=data)

if __name__=="__main__":
    app.run(host="127.0.0.1",port=5000,debug=True)
