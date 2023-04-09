import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = 'select access_time from hand where business_module_name="碳计算工具模块" order by access_time desc;'

cur.execute(query_sql)
query_data = cur.fetchall()
data = list(map(lambda x:list(x),query_data))
dic = {}
for tim in data:
    cur_tim = tim[0].split()[0]
    dic[cur_tim] = dic.get(cur_tim,0)+1
task3_x = []
task3_y = []
data = list(dic.items())
data = list(map(lambda x:list(x),data[:6]))
task3_x = list(map(lambda x:x[0],data))[::-1]
task3_y = list(map(lambda x:x[1],data))[::-1]
cur.close()
conn.close()