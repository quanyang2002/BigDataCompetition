import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select business_module_name,count(*) jishu from hand group by business_module_name order by jishu desc;"

cur.execute(query_sql)
query_data = cur.fetchall()
data = list(map(lambda x:list(x),query_data))
print(data)
task1_x = []
task1_y = []
for item in data:
    task1_x.append(item[0])
    task1_y.append(item[1])


cur.close()
conn.close()