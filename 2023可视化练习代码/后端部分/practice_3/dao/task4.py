import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")

cur = conn.cursor()

query_sql = "select quyu,count(*) num from house group by quyu;"
cur.execute(query_sql)
query_data = cur.fetchall()
task4_x = list(map(lambda x:x[0],query_data))
dic = []
for item in query_data:
    tmp = {}
    tmp['name'] = item[0]
    tmp['value'] = item[1]
    dic.append(tmp)
print(task4_x)

cur.close()
conn.close()