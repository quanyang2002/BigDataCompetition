import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
query_sql = "select access_time from hand;"
cur.execute(query_sql)
data = cur.fetchall()
data = list(map(lambda x:list(x),data))
task2_x = []
task2_y = []
tmp = {}
for tim in data:
    day = tim[0].split()[0]
    tmp[day] = tmp.get(day,0) + 1
task2_x = list(tmp.keys())
task2_y = list(tmp.values())
# print(task2_x)
# print(task2_y)
task2_x = task2_x[-7:]
task2_y = task2_y[-7:]

cur.close()
conn.close()