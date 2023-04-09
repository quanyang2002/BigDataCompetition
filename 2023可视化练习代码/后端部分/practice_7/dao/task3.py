import pymysql
from practice_7.dao.task2 import provinces
# 堆叠柱状图
conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")

cur = conn.cursor()
query_module_sql = "select distinct business_module_name from hand;"
cur.execute(query_module_sql)
modules = list(map(lambda x:x[0],cur.fetchall()))
print(modules)
counts1 = []
for module in modules:
    tmp = []
    for province in provinces:
        query_count_sql = f'select count(*) from hand where province="{province}" and business_module_name="{module}";'
        cur.execute(query_count_sql)
        tmp.append(cur.fetchall()[0][0])
    counts1.append(tmp)
# print(counts)

cur.close()
conn.close()