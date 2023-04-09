import pymysql
# 时间轴柱状图
conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
query_date_province_sql = "select access_time,province from hand;"

cur.execute(query_date_province_sql)
date_province = cur.fetchall()
provinces = list(set(map(lambda x:x[1],date_province)))
provinces.remove("未知")
# print(provinces)
year_month = [f"2022-0{i}" for i in range(5,10)]
year_month.append("2022-10")
counts = []
for month in year_month:
    tmp = []
    for province in provinces:
        query_count_sql = f'select count(*) from hand where access_time like "{month}%" and province="{province}";'
        cur.execute(query_count_sql)
        tmp.append(cur.fetchall()[0][0])
    counts.append(tmp)
print(counts)
cur.close()
conn.close()