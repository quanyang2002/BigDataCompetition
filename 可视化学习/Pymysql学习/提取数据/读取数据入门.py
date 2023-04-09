import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       charset="UTF8",
                       user="root",
                       password="password",
                       database="pymysql")
cur = conn.cursor()

sql = "select * from student;"
cur.execute(sql)

# 获取所有结果
# resultSet = cur.fetchall()
# 获取一条结果
# oneSet = cur.fetchone()
# 获取一些结果
# size : 返回结果的条数
manySet = cur.fetchmany(size=3)
# print(list(map(lambda x:list(x),resultSet)))
# print(oneSet)
print(manySet)
cur.close()
conn.close()
