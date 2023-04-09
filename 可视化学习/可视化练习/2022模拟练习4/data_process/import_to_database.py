import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
f = open("../static/b.csv",'r',encoding='utf-8')

lines = f.readlines()
cur = conn.cursor()

insert_sql = "insert into house values({},'{}',{},'{}',{},{},'{}','{}','{}','{}');"

for line in lines:
    line = line.replace(',,',',null,')
    data = line.strip('\n').split(',')
    cur.execute(insert_sql.format(data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7],data[8],data[9]))
conn.commit()

cur.close()
conn.close()