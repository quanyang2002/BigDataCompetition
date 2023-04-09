import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select name,sum(zongjia)/sum(area) from house group by name;"

cur.execute(query_sql)

query_data = cur.fetchall()
data = list(map(lambda x:list(x),query_data))
task2_y = list(map(lambda x:float(x[1]),data))
task2_x = list(map(lambda x:x[0],data))

cur.close()
conn.close()