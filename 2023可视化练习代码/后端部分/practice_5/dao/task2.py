import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")

cur = conn.cursor()

query_sql = "select business_module_name,count(*) from hand group by business_module_name;"

cur.execute(query_sql)

data = cur.fetchall()

data = list(map(lambda x:list(x),data))

task2_x = list(map(lambda x:x[0],data))
task2_y = list(map(lambda x:x[1],data))

print(data)

cur.close()
conn.close()