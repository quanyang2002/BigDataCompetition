import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")

cur = conn.cursor()

insert_sql = "insert into music value('{}','{}','{}','{}','{}','{}','{}','{}','{}','{}',{},{});"

f = open('../static/aa.csv','r',encoding='utf-8')
data = f.readlines()

for line in data[1:]:
    tmp = line.strip('\n').split(',')
    cur.execute(insert_sql.format(tmp[0],tmp[1],tmp[2],tmp[3],tmp[4],tmp[5],tmp[6],tmp[7],tmp[8],tmp[9],tmp[10],tmp[11]))
conn.commit()
cur.close()
conn.close()
f.close()