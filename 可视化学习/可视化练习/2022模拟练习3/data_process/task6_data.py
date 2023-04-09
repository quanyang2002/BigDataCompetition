import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")

cur = conn.cursor()

query_sql = "select 城市,sum(好评数),sum(差评数) from canyin group by 城市;"

cur.execute(query_sql)
query_data = cur.fetchall()

task6_x = []
task6_haopin = []
task6_chapin = []
for item in query_data:
    task6_x.append(item[0])
    task6_haopin.append(int(item[1]))
    task6_chapin.append(int(item[2]))
print(task6_x)
print(task6_haopin)
print(task6_chapin)
cur.close()
conn.close()