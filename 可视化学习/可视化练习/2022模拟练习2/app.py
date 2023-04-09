from flask import Flask
from flask import render_template

from data_process.task1_data import gongsi,data
from data_process.task2_data import styles,count
from data_process.task3_data import name,shangyue,benyue,zongpaifang

app = Flask(__name__,template_folder="templates")

@app.route('/task1')
def pie():
    return render_template("task1.html",legend=gongsi,data=data)

@app.route('/task2')
def tiaoxing():
    return render_template("task2.html",styles=styles,data=count)

@app.route('/task3')
def zhu():
    return render_template("task3.html",songs=name,shangyue=shangyue,benyue=benyue,zongpaifang=zongpaifang)


if __name__=="__main__":
    app.run()