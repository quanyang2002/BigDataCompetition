import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")

cursor = conn.cursor()

query_user_sql = "select distinct username from hand;"
query_user_date_sql = "select username,access_time from hand;"
query_user_count_sql = "select username,count(*) num from hand group by username;"

cursor.execute(query_user_sql)
users = cursor.fetchall()
cursor.execute(query_user_date_sql)
user_date = cursor.fetchall()
cursor.execute(query_user_count_sql)
user_count = cursor.fetchall()

def calc_dis(date1,date2):
    date1 = date1.split('-')
    date2 = date2.split('-')
    if date1[1] == date2[1]:
        return eval(date2[2].lstrip('0'))-eval(date1[2].lstrip('0'))
    else:
        if eval(date2[2].lstrip('0')) >= 8:
            return 7
        else:
            if eval(date1[1].lstrip('0')) in [5,7,8,10]:
                return (31-eval(date1[2].lstrip('0'))) + eval(date2[2].lstrip('0'))
            else:
                return (30 - eval(date1[2].lstrip('0'))) + eval(date2[2].lstrip('0'))

no_hot_users = []
for user in users:
    dates = []
    for item in user_date:
        if item[0] == user[0]:
            dates.append(item[1].split()[0])
    print(user[0],dates)
    p = 0
    dis = calc_dis(dates[-1],"2022-10-07")
    if dis >= 7:
        p = 1
    # for dat in range(1,len(dates)):
    #     dis = calc_dis(dates[dat-1],dates[dat])
    #     if dis >= 7:
    #         p = 1
    #         break
    if p == 1:
        no_hot_users.append(user[0])
for user in no_hot_users:
    for item in user_count:
        if user == item[0]:
            print(f"用户名：{user} 累计访问次数：{item[1]}")

cursor.close()
conn.close()