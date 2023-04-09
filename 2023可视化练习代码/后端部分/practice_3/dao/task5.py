import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")

cur = conn.cursor()

query_sql = "select louceng,quyu from house;"
cur.execute(query_sql)
query_data = cur.fetchall()
task5_x = []
task5_y = [[],[],[]]
for item in query_data:
    if item[1] not in task5_x:
        task5_x.append(item[1])
for qy in task5_x:
    di = 0
    zhong = 0
    gao = 0
    for item in query_data:
        if item[1] == qy:
            if item[0] == "低层":
                di += 1
            elif item[0] == "中层":
                zhong += 1
            else:
                gao += 1
    task5_y[0].append(di)
    task5_y[1].append(zhong)
    task5_y[2].append(gao)
print(task5_x)
print(task5_y)
cur.close()
conn.close()