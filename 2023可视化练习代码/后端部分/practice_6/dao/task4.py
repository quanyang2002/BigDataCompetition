import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
query_user_sql = "select distinct username from hand;"
cur.execute(query_user_sql)
users = cur.fetchall()
# print(users)
query_user_date_sql = "select username,access_time from hand;"
cur.execute(query_user_date_sql)
users_dates_all = cur.fetchall()
# print(users_dates_all)
calc = {}
for user in users:
    user_date_person = [x[1].split()[0] for x in users_dates_all if x[0] == user[0]]
    user_access_count = len(user_date_person)
    user_date_person = list(set(user_date_person))
    user_date_person.sort()
    # print(user_date_person)
    flag = 0
    tmp = []
    month = []
    for i in range(1,len(user_date_person)):
        cha = eval(user_date_person[i].split('-')[2].lstrip('0')) - eval(user_date_person[i-1].split('-')[2].lstrip('0'))
        if cha == 1:
            month.append(eval(user_date_person[i-1].split('-')[1].lstrip('0')))
        tmp.append(cha)
    month = list(set(month))
    month.sort()
    # print(month)
    # 条件放宽
    if month == [6,7,8,9] or "1" in str(tmp):
        flag = 1
    if flag == 1:
        calc[user[0]] = user_access_count
dat = list(calc.items())
dat.sort(key=lambda x:x[1],reverse=True)
for item in dat:
    print(f"用户名：{item[0]}，累计访问次数：{item[1]}")
task4_x = list(map(lambda x:x[0],dat))[:5]
task4_y = list(map(lambda x:x[1],dat))[:5]
print(task4_x,task4_y)
cur.close()
conn.close()