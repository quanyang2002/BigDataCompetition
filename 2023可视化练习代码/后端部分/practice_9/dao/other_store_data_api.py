import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="takeout")
cur = conn.cursor()
query_sql = "select * from other_store_data;"
cur.execute(query_sql)
data = cur.fetchall()
other_store_data = list(map(lambda x:list(x),data))
print("other_store_data's length",len(other_store_data))

cur.close()
conn.close()