import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")

cur = conn.cursor()
query_line_name_sql = "select distinct line_name from t_station_main_gongjiao;"
cur.execute(query_line_name_sql)
lines = cur.fetchall()
lines = list(map(lambda x:list(x),lines))
f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\公交站点长度统计.csv",'w',encoding="UTF-8")
f.close()
f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\公交站点长度统计.csv",'a',encoding="UTF-8")
f.write("线路名称,起始站点,结束站点,站点长度(米)\n")
for line in lines:
    query_stations_sql = f'select distinct station_name,jingdu,weidu from t_station_main_gongjiao where line_name="{line[0]}";'
    cur.execute(query_stations_sql)
    stations = cur.fetchall()
    stations = list(map(lambda x:list(x),stations))
    for i in range(1,len(stations)):
        f.write("{},{},{},{}\n".format(line[0],stations[i-1][0],stations[i][0],\
                                       ((stations[i][1]-stations[i-1][1])**2+(stations[i][2]-stations[i-1][2])**2)**0.5))
cur.close()
conn.close()