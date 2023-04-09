import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select 城市,avg(客单价) from canyin group by 城市;"

cur.execute(query_sql)
query_data = cur.fetchall()
task4_x = []
task4_y = []
for item in query_data:
    task4_x.append(item[0])
    task4_y.append(float(item[1]))
print(task4_x)


cur.close()
conn.close()