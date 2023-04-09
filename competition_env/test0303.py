import pymysql
from flask import Flask
from flask import jsonify

app = Flask(__name__)

@app.route("/task")
def return_data():
    conn = pymysql.connect(
        host="localhost",
        port=3306,
        user="root",
        password="password",
        database="comp"
    )
    cur = conn.cursor()
    query_sql = "select * from `order`;"
    cur.execute(query_sql)
    data = list(map(lambda x:list(x),cur.fetchall()))
    return jsonify(data)

if __name__ == "__main__":
    app.run(debug=True)
# conn = pymysql.connect(
#     host="localhost",
#     port=3306,
#     user="root",
#     password="password",
#     database="comp"
# )
#
# cur = conn.cursor()
# f = open("D:\\学习\\大数据\\技能大赛\\competition_env\\test.csv","r+",encoding="gbk")
# lines = f.readlines()[1:]
# for line in lines:
#     line = line.strip('\n').split(',')
#     insert_sql = f"insert into `order` values('{line[0]}','{line[1]}',{line[2]},{line[3]},{line[4]},{line[5]});"
#     cur.execute(insert_sql)
# conn.commit()
# cur.close()
# conn.close()