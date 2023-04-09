import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_user_sql = 'select username,count(*) jishu from hand where business_module_name="碳计算工具模块" group by username order by jishu desc limit 5;'
query_data_sql = 'select username,access_time from hand where business_module_name="碳计算工具模块";'

time_limit = [f'2022-10-0{i}' for i in range(1,8)]

cur.execute(query_user_sql)
data = list(map(lambda x:list(x),cur.fetchall()))
user_data = list(map(lambda x:x[0],data))
print(user_data)
cur.execute(query_data_sql)
zong_data = list(map(lambda x:list(x),cur.fetchall()))
print(zong_data)
task7_y = []
for user in user_data:
    tmp = []
    for tim in time_limit:
        cont = 0
        for line in zong_data:
            if line[0] == user and line[1].split()[0]==tim:
                cont += 1
        tmp.append(cont)
    task7_y.append(tmp)
print(task7_y)
cur.close()
conn.close()