# 导入flask相关模块
from flask import Flask
from flask import render_template
# 导入数据
from data_process.data import xzhou
from data_process.data import data
# 实例化flask对象并指定HTML文件存放位置
app = Flask(__name__,template_folder="./templates")
# 设定路由规则
@app.route('/')
def Flask_ECharts():
    # 整合数据和网页
    return render_template('./index.html',xzhou=xzhou,data=data)

# 设置程序入口
if __name__=="__main__":
    # 启动服务
    app.run(host="127.0.0.1",port=5000,debug=True)