import pymysql

conn = pymysql.connect(host="localhost",port=3306,user="root",password="password",database="spider")
cur = conn.cursor()

insert_sql = "insert into hand values({},{},'{}','{}','{}','{}','{}','{}','{}','{}','{}','{}','{}','{}','{}','{}');"

f = open('../static/网站用户行为数据.csv','r')
data = f.readlines()
# print(len(data))
print(len(data[0].strip('\n').split(',')))
for line in data[1:]:
    line = line.replace(',,',',null,')
    idx = line.index('[',0)
    if line[idx-1] == ',':
        line = line.replace(',[',',"[')
        line = line.replace('],',']",')
    tmp_front = line.strip('\n').split(',"[')[0].split(',')
    tmp_middle = '[' + line.strip('\n').split(',"[')[1].split(']",')[0] + ']'
    tmp_back = line.strip('\n').split(']",')[1].split(',')
    tmp = tmp_front
    tmp.append(tmp_middle)
    for i in tmp_back:
        tmp.append(i)
    # print(tmp)
    cur.execute(insert_sql.format(tmp[0],tmp[1],tmp[2],tmp[3],tmp[4],tmp[5],tmp[6],tmp[7],tmp[8],tmp[9],tmp[10],tmp[11],tmp[12],tmp[13],tmp[14],tmp[15]))

conn.commit()
cur.close()
conn.close()
f.close()