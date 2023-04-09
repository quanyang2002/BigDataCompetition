import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
query_sql = "select city,count(*) num from hand group by city order by num desc limit 20;"
cur.execute(query_sql)
data = cur.fetchall()
data = list(map(lambda x:list(x),data))
# print(data)
task1_x = list(map(lambda x:x[0],data))
task1_y = list(map(lambda x:x[1],data))
# print(task1_x)
# print(task1_y)
cur.close()
conn.close()