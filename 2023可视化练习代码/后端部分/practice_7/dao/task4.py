import pymysql

conn = pymysql.connect(host="localhost",
                       user="root",
                       password="password",
                       port=3306,
                       database="spider")
cur = conn.cursor()
query_data_sql = "select province,count(*) from hand group by province;"
cur.execute(query_data_sql)
dic = []
for item in cur.fetchall():
    if item[0] != "未知":
        tmp = {}
        tmp['name'] = item[0]
        tmp['value'] = item[1]
        dic.append(tmp)
print(dic)

cur.close()
conn.close()