import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select name,count(*) from house group by name;"

cur.execute(query_sql)
query_data = cur.fetchall()
data = list(map(lambda x:list(x),query_data))

task5_x = []
task5_y = []
for i in data:
    dic = {}
    dic['name'] = i[0]
    dic['value'] = i[1]
    task5_x.append(i[0])
    task5_y.append(dic)

cur.close()
conn.close()