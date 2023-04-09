from flask import Flask
from flask import render_template

from data_process.task2_data import data_nanjin,data_beijing,data_shanghai
from data_process.task3_data import task3_x,task3_y
from data_process.task4_data import task4_x,task4_y
from data_process.task5_data import task5_x,task5_y
from data_process.task6_data import task6_x,task6_chapin,task6_haopin

app = Flask(__name__,template_folder="templates")

@app.route('/task2')
def qipao():
    return render_template("task2.html",data_nanjin=data_nanjin,data_beijing=data_beijing,data_shanghai=data_shanghai)

@app.route('/task3')
def zhuzhuang():
    return render_template("task3.html",task3_x=task3_x,task3_y=task3_y)

@app.route('/task4')
def tiaoxing():
    return render_template("task4.html",task4_x=task4_x,task4_y=task4_y)

@app.route('/task5')
def pie():
    return render_template("task5.html",task5_x=task5_x,task5_y=task5_y)

@app.route('/task6')
def duidie():
    return render_template("task6.html",task6_x=task6_x,task6_chapin=task6_chapin,task6_haopin=task6_haopin)


if __name__=="__main__":
    app.run(debug=True)