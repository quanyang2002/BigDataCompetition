import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select name,zhuangxiu from house;"
cur.execute(query_sql)
query_data = cur.fetchall()
data = list(map(lambda x:list(x),query_data))
task4_x = []
for i in data:
    if i[0] not in task4_x:
        task4_x.append(i[0])
task4_jingzhuang = []
task4_jianzhuang = []
task4_maopi = []
for area in task4_x:
    jinzhuang_tmp = 0
    jianzhuang_tmp = 0
    maopi = 0
    for i in data:
        if i[0] == area:
            if i[1] == "精装修":
                jinzhuang_tmp += 1
            elif i[1] == "简装修":
                jianzhuang_tmp += 1
            else:
                maopi += 1
    task4_jingzhuang.append(jinzhuang_tmp)
    task4_jianzhuang.append(jianzhuang_tmp)
    task4_maopi.append(maopi)
cur.close()
conn.close()
