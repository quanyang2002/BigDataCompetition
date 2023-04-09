import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")

cur = conn.cursor()

query_sql = "select name,count('精装修') jinshu,count('简装修') jianshu,count('毛坯') maoshu from house group by name;"
cur.execute(query_sql)
query_data = cur.fetchall()
task3_x = list(map(lambda x:x[0],query_data))
task3_jinshu = list(map(lambda x:x[1],query_data))
task3_jianshu = list(map(lambda x:x[2],query_data))
task3_maoshu = list(map(lambda x:x[3],query_data))

cur.close()
conn.close()