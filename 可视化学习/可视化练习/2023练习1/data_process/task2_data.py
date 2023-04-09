import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select method_name,count(*) jishu from hand group by method_name order by jishu desc limit 10;"

cur.execute(query_sql)
query_data = cur.fetchall()
data = list(map(lambda x:list(x),query_data))
print(data)
task2_x = []
task2_y = []
for item in data:
    task2_x.append(item[0])
    task2_y.append(item[1])

cur.close()
conn.close()