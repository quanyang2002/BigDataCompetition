import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select faxinggongsi from music;"

cur.execute(query_sql)
query_data = cur.fetchall()

gongsi = []
for item in query_data:
    if item[0].strip() not in gongsi:
        gongsi.append(item[0].strip())
data = []
for gs in gongsi:
    dic = {}
    dic['name']=gs
    for item in query_data:
        tmp = item[0].strip()
        if tmp == gs:
            dic['value'] = dic.get('value',0)+1
    data.append(dic)
print(data)
cur.close()
conn.close()