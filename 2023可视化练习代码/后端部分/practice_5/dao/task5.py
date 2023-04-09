import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")

cur = conn.cursor()

query_sql = "select username,count(*) num from hand group by username order by num desc limit 20;"

cur.execute(query_sql)

data = cur.fetchall()

data = list(map(lambda x:list(x),data))

task5_x = list(map(lambda x:x[0],data))
task5_y = list(map(lambda x:x[1],data))

print(data)

cur.close()
conn.close()