import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
query_sql = "select station_name,count(distinct line_name) num from t_station_main_gongjiao group by station_name;"
cur.execute(query_sql)
data = cur.fetchall()
station_data = list(map(lambda x:list(x),data))
cur.close()
conn.close()



