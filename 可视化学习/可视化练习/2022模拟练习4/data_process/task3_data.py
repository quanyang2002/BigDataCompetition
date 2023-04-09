import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select type,sum(zongjia)/sum(area),count(*) xiaol from house group by type order by xiaol desc limit 5;"
cur.execute(query_sql)

query_data = cur.fetchall()

data = list(map(lambda x:list(x),query_data))
print(data)

task3_x = list(map(lambda x:x[0],data))
task3_y = list(map(lambda x:float(x[1]),data))




cur.close()
conn.close()