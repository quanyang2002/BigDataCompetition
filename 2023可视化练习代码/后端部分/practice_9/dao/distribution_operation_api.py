import pymysql

conn = pymysql.connect(host="192.168.23.36",
                       port=3306,
                       user="root",
                       password="123456",
                       database="takeout")
cur = conn.cursor()
query_sql = "select * from distribution_operation;"
cur.execute(query_sql)
data = cur.fetchall()
distribution_operation = list(map(lambda x:list(x),data))
print("distribution_operation's length",len(distribution_operation))
cur.close()
conn.close()