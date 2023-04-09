import pymysql
f = open("./接驳站点.csv",'w',encoding="UTF-8")
f.close()
f = open("./接驳站点.csv",'a',encoding="UTF-8")
f.write("接驳线路名称1,接驳站点1,接驳线路名称2,接驳站点2,长度\n")
conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
def calc_dis(j1,j2,w1,w2):
    result = (((j2-j1)*87622)**2+((w2-w1)*111194)**2)**0.5
    return result
query_sql = "select station_name,jingdu,weidu,line_name from t_station_main_gongjiao;"
cur.execute(query_sql)
data = list(map(lambda x:list(x),cur.fetchall()))
# print(data)
for i in range(0,len(data)-1):
    for j in range(0,len(data)):
        dis = calc_dis(data[i][1],data[j][1],data[i][2],data[j][2])
        if dis < 100 and data[i][-1] != data[j][-1]:
            f.write("{},{},{},{},{}\n".format(data[i][-1],data[i][0],data[j][-1],data[j][0],dis))
cur.close()
conn.close()
f.close()