import pymysql

from practice_5.dao.task5 import task5_x
from practice_6.dao.task2 import task2_x

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
task3_x = task2_x[-7:]
hot_users = task5_x[:5]
task3_y = []
for user in hot_users:
    tmp = []
    for tim in task3_x:
        query_sql = f'select count(*) from hand where username="{user}" and access_time like "{tim}%";'
        cur.execute(query_sql)
        dat = cur.fetchall()
        tmp.append(dat[0][0])
    task3_y.append(tmp)

print(task3_x)
print(task3_y)
print(hot_users)

cur.close()
conn.close()