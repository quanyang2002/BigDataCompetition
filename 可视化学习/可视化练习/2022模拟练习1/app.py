from flask import Flask
from flask import render_template

from data_process.task1_data import table_header,class_count
from data_process.task2_data import table_header,pub1_avg
from data_process.task3_data import name,course_4,course_1,course_2,course_3
app = Flask(__name__,template_folder="templates")

@app.route('/task1')
def pie():
    return render_template('task1.html',task1_x=table_header,task1_y=class_count)
@app.route('/task2')
def bar():
    return render_template('task2.html',task2_x=table_header,task2_y=pub1_avg)
@app.route('/task3')
def line():
    return render_template('task3.html',name=name,course_4=course_4,course_1=course_1,course_2=course_2,course_3=course_3)

if __name__=="__main__":
    app.run(debug=True)
