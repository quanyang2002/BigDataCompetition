import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = " select name,louceng from house;"
cur.execute(query_sql)

query_data = cur.fetchall()
data = list(map(lambda x:list(x),query_data))
task6_x = list(set(list(map(lambda x:x[0],data))))
task6_diceng = []
task6_zhongceng = []
task6_gaoceng = []
for area in task6_x:
    diceng = 0
    zhongceng = 0
    gaoceng = 0
    for i in data:
        if i[0] == area:
            if i[1] == "高层":
                gaoceng += 1
            elif i[1] == "中层":
                zhongceng += 1
            else:
                diceng += 1
    task6_diceng.append(diceng)
    task6_zhongceng.append(zhongceng)
    task6_gaoceng.append(gaoceng)

cur.close()
conn.close()