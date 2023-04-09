import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="takeout")
cur = conn.cursor()
query_sql = "select * from store_basic_informations;"
cur.execute(query_sql)
data = cur.fetchall()
store_basic_informations = list(map(lambda x:list(x),data))
print("store_basic_informations's length",len(store_basic_informations))

cur.close()
conn.close()