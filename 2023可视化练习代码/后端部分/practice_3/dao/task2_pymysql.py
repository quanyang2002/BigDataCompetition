import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")

cur = conn.cursor()
query_hot_type_sql = "select type,count(*) num from house group by type order by num desc limit 5;"
cur.execute(query_hot_type_sql)
query_data = cur.fetchall()
task2_x = list(map(lambda x:x[0],query_data))
query_data_sql = "select type,sum(zongjia)/sum(area) junjia from house group by type;"
cur.execute(query_data_sql)
data = cur.fetchall()
task2_y = []
for tp in task2_x:
    for item in data:
        if item[0]==tp:
            task2_y.append(eval(str(item[1])[:str(item[1]).index('.')+2]))
print(task2_x,task2_y)
cur.close()
conn.close()