import pymysql
from data_process.task1_calc import query_data

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

query_sql = "select * from cantin_addr;"
def return_new_list(lst):
    new = [lst[1],lst[2],lst[0]]
    return new
cur.execute(query_sql)
data = cur.fetchall()
# print(data)
# print(query_data)
for item in query_data:
    for item1 in data:
        if item[0] == item1[0]:
            query_data[query_data.index(item)].append(item1[1])
            query_data[query_data.index(item)].append(item1[2])
data_nanjin = [return_new_list(query_data[1][1:])]
data_shanghai = [return_new_list(query_data[2][1:])]
data_beijing = [return_new_list(query_data[0][1:])]

cur.close()
conn.close()