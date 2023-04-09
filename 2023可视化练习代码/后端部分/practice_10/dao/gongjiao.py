import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
query_sql = "select * from t_station_main_gongjiao;"
cur.execute(query_sql)
data = cur.fetchall()
data = list(map(lambda x:list(x),data))
# print(data)
cur.close()
conn.close()