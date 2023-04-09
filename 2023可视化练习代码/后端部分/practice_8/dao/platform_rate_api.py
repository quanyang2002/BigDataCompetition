import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="hotel")
cur = conn.cursor()
query_sql = "select * from platform_rate;"
cur.execute(query_sql)
data = cur.fetchall()
platform_rate = list(map(lambda x:list(x),data))
print("platform_rate's length:",len(platform_rate))

cur.close()
conn.close()