from flask import Flask
from flask import render_template

from data_process.zhexiantu_data import data,date,data1,data2
from data_process.zhuzhuangtu_data import xzhou,zhuzhuangtu_data1
from data_process.juhezhu_data import juhezhu_xzhou,juhezhu_data_a,juhezhu_data_b

app = Flask(__name__,template_folder="templates")

@app.route('/zhexiantu1')
def zhexian():
    return render_template("折线图.html",data=data,date=date,data1=data1,data2=data2)
@app.route('/zhuzhuangtu')
def zhuzhuang():
    return render_template("柱状图.html",xzhou=xzhou,zhuzhuangtu_data1=zhuzhuangtu_data1)
@app.route('/juhezhuzhuangtu')
def juhezhu():
    return render_template("聚合柱状图.html",juhezhu_xzhou=juhezhu_xzhou,juhezhu_data_a=juhezhu_data_a,juhezhu_data_b=juhezhu_data_b)
if __name__ == "__main__":
    app.run()