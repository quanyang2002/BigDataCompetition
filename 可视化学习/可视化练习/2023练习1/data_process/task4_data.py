import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select username,count(*) jishu from hand group by username order by jishu desc limit 20;"

cur.execute(query_sql)
query_data = cur.fetchall()
data = list(map(lambda x:list(x),query_data))
print(data)
task4_x = []
task4_y = []
for item in data:
    task4_x.append(item[0])
    task4_y.append(item[1])

cur.close()
conn.close()