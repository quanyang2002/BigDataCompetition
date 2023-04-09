import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select 城市,sum(推单数) from canyin group by 城市;"

cur.execute(query_sql)
query_data = cur.fetchall()
task5_x = []
task5_y = []
for item in query_data:
    task5_x.append(item[0])
    dic = {}
    dic['name']=item[0]
    dic['value']=int(item[1])
    task5_y.append(dic)
print(task5_x)
print(task5_y)


cur.close()
conn.close()