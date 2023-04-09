import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")

cur = conn.cursor()

query_sql = "select name,sum(zongjia)/sum(area) from house group by name;"
cur.execute(query_sql)
query_data = cur.fetchall()
task1_x = list(map(lambda x:x[0],query_data))
query_data = list(map(lambda x:str(x[1])[:str(x[1]).index('.')+2],query_data))
task1_y = list(map(lambda x:eval(x),query_data))
print(task1_y)
print(task1_x)
# print(query_data)

cur.close()
conn.close()