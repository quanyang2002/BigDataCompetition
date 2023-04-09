import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select 城市,count(*) as 商家数量 from canyin group by 城市;"

cur.execute(query_sql)

query_data = list(map(lambda x:list(x),cur.fetchall()))
query_data.sort(key=lambda x:x[1],reverse=True)
for i in range(len(query_data)):
    print("=={}: 城市 {}=餐厅数为{}个===".format(i+1,query_data[i][0],query_data[i][1]))

cur.close()
conn.close()