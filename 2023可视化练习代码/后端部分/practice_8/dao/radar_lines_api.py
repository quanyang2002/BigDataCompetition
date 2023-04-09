import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="hotel")
cur = conn.cursor()
query_sql = "select * from radar_lines;"
cur.execute(query_sql)
data = cur.fetchall()
radar_lines = list(map(lambda x:list(x),data))
print("radar_lines's length:",len(radar_lines))

cur.close()
conn.close()