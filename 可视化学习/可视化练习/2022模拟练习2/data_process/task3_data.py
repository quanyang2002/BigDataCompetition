import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select name,shangyuebofang,benyue from music;"

cur.execute(query_sql)
query_data = cur.fetchall()

name = list(map(lambda x:x[0],query_data))
shangyue = list(map(lambda x:x[1],query_data))
benyue = list(map(lambda x:x[2],query_data))
zongpaifang = []
for i in range(len(shangyue)):
    zongpaifang.append(shangyue[i]+benyue[i])
cur.close()
conn.close()