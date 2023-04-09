import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="hotel")
cur = conn.cursor()
query_sql = "select * from city_let_rate;"
cur.execute(query_sql)
data = cur.fetchall()
city_let_rate = list(map(lambda x:list(x),data))
print("city_let_rate's length:",len(city_let_rate))

cur.close()
conn.close()