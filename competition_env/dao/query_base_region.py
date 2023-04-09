import pymysql

conn = pymysql.connect(
    host="localhost",
    port=3306,
    user="root",
    password="password",
    database="comp"
)

cur = conn.cursor()

query_sql = "select * from base_region;"
cur.execute(query_sql)
base_region_data = list(map(lambda x:list(x),cur.fetchall()))
# print(base_region_data)
cur.close()
conn.close()