import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="takeout")
cur = conn.cursor()
query_sql = "select * from distribution_platform;"
cur.execute(query_sql)
data = cur.fetchall()
distribution_platform = list(map(lambda x:list(x),data))
print("distribution_platform's length",len(distribution_platform))
cur.close()
conn.close()