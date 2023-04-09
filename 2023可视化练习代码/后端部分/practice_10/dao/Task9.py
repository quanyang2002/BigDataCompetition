import pymysql
f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\乘车时长.csv",'r',encoding="utf-8")
data = f.readlines()[1:]
conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
dates = []
for line in data:
    tmp = line.strip('\n').split(',')
    if tmp[0] not in dates:
        dates.append(tmp[0])
cur = conn.cursor()
for date in dates:
    tmp_tmp = []
    for line in data:
        tmp = line.strip('\n').split(',')
        if tmp[0] == date:
            tmp_tmp.append(eval(tmp[-1]))
    insert_sql = 'insert into t_ic_average_time values("{}",{})'.format(date,sum(tmp_tmp)/len(tmp_tmp))
    cur.execute(insert_sql)
conn.commit()
cur.close()
conn.close()
f.close()