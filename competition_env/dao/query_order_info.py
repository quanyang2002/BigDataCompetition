import pymysql

conn = pymysql.connect(
    host="localhost",
    port=3306,
    user="root",
    password="password",
    database="comp"
)

cur = conn.cursor()

query_sql = "select * from order_info;"
cur.execute(query_sql)
base_order_info_data = list(map(lambda x:list(x),cur.fetchall()))
# print(base_order_info_data)
cur.close()
conn.close()