import pymysql

conn = pymysql.connect(
    host="localhost",
    port=3306,
    user="root",
    password="password",
    database="comp"
)

cur = conn.cursor()

query_sql = "select * from base_province;"
cur.execute(query_sql)
base_province_data = list(map(lambda x:list(x),cur.fetchall()))
# print(data)
cur.close()
conn.close()