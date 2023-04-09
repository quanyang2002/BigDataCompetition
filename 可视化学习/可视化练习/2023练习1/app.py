from flask import Flask
from flask import render_template

from data_process.task1_data import task1_x,task1_y
from data_process.task2_data import task2_x,task2_y
from data_process.task4_data import task4_x,task4_y
from data_process.task5_data import task5_x,task5_y
from data_process.task3_data import task3_x,task3_y
from data_process.task6_data import task6_x,task6_y

app = Flask(__name__,template_folder="templates")

@app.route('/task1')
def task1():
    return render_template('task1.html',task1_x=task1_x,task1_y=task1_y)
@app.route('/task2')
def task2():
    return render_template('task2.html',task2_x=task2_x,task2_y=task2_y)
@app.route('/task3')
def task3():
    return render_template('task3.html',task3_x=task3_x,task3_y=task3_y)
@app.route('/task4')
def task4():
    return render_template('task4.html',task4_x=task4_x,task4_y=task4_y)
@app.route('/task5')
def task5():
    return render_template('task5.html',task5_x=task5_x,task5_y=task5_y)
@app.route('/task6')
def task6():
    return render_template('task6.html',task6_x=task6_x,task6_y=task6_y)

if __name__ == "__main__":
    app.run(debug=True)