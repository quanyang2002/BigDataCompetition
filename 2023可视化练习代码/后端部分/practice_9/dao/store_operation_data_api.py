import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="takeout")
cur = conn.cursor()
query_sql = "select * from store_operation_data;"
cur.execute(query_sql)
data = cur.fetchall()
store_operation_data = list(map(lambda x:list(x),data))
print("store_operation_data's length",len(store_operation_data))

cur.close()
conn.close()