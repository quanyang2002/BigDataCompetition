import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")

cur = conn.cursor()

query_sql = 'select access_time,business_module_name from hand where business_module_name="碳计算工具模块";'
cur.execute(query_sql)

data = cur.fetchall()

data = list(map(lambda x:list(x),data))
task4_dates = []
task4_counts = []
for item in data:
    date = item[0].split()[0]
    if date not in task4_dates:
        task4_dates.append(date)
for dat in task4_dates:
    count = 0
    for item in data:
        cur_day = item[0].split()[0]
        if cur_day == dat:
            count += 1
    task4_counts.append(count)

input_date = input("请输入日期[2022-06-06，2022-10-07]:")
task4_x = []
task4_y = []
days = eval(input_date.split('-')[2].lstrip('0'))
mounth = eval(input_date.split('-')[1].lstrip('0'))
year = input_date.split('-')[0]
if days > 6 :
    for day in range(days,days-7,-1):
        if len(str(day)) == 1:
            tmp = '-'.join(input_date.split('-')[:2])+'-0'+str(day)
            task4_x.append(tmp)
        else:
            tmp = '-'.join(input_date.split('-')[:2])+'-'+str(day)
            task4_x.append(tmp)
else:
    for day in range(days,0,-1):
        if len(str(day)) == 1:
            tmp = '-'.join(input_date.split('-')[:2])+'-0'+str(day)
            task4_x.append(tmp)
        else:
            tmp = '-'.join(input_date.split('-')[:2])+'-'+str(day)
            task4_x.append(tmp)
    if len(task4_x)<7:
        if mounth in [6,9]:
            for day in range(31,31-(7-len(task4_x)),-1):
                tmp =  year + '-' + '0' + str(mounth-1) + '-' +str(day)
                task4_x.append(tmp)
        else:
            for day in range(30,30-(7-len(task4_x)),-1):
                tmp = year + '-' + '0' + str(mounth-1) + '-' +str(day)
                task4_x.append(tmp)
# print(task4_x)
task4_x = task4_x[::-1]
for day in task4_x:
    if day not in task4_dates:
        task4_y.append(0)
    else:
        task4_y.append(task4_counts[task4_dates.index(day)])
print(task4_x)
print(task4_y)
cur.close()
conn.close()