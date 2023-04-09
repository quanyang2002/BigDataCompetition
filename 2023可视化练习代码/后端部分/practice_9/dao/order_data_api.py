import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="takeout")
cur = conn.cursor()
query_sql = "select * from order_data;"
cur.execute(query_sql)
data = cur.fetchall()
order_data = list(map(lambda x:list(x),data))
print("order_data's length",len(order_data))

cur.close()
conn.close()