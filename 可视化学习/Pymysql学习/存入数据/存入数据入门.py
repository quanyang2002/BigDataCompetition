import pymysql

f = open("../源数据/insert_data.csv",'r')
data = f.readlines()[1:]
conn = pymysql.connect(host="localhost",
                       port=3306,
                       charset='UTF8',
                       user='root',
                       password="password",
                       database="pymysql")
# if conn:
#     print(conn.get_server_info())
cur = conn.cursor()

sql = "insert into student values('{}',{});"
for line in data:
    tmp = line.strip('\n').split(',')
    cur.execute(sql.format(tmp[0],tmp[1]))
conn.commit()
cur.close()
conn.close()


