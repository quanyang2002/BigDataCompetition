import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select 城市,sum(用户投诉数),sum(差评数),sum(好评数) from canyin group by 城市;"

cur.execute(query_sql)
query_data = cur.fetchall()
task3_x=[]
task3_y=[]
for item in query_data:
    task3_x.append(item[0])
    task3_y.append(float(sum(item[1:])/3))
print(task3_x)
print(task3_y)

cur.close()
conn.close()