import pymysql

f = open("接驳站点.csv",'r',encoding="UTF-8")
p = open("枢纽站点.csv",'w',encoding="UTF-8")
p.close()
p = open("枢纽站点.csv",'a',encoding="UTF-8")
p.write("线路名称,换乘枢纽站点,接驳数量\n")
jiebo = f.readlines()[1:]
conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
cur = conn.cursor()
query_lines_sql = "select distinct line_name from t_station_main_gongjiao;"
cur.execute(query_lines_sql)
lines = list(map(lambda x:list(x),cur.fetchall()))
for line in lines:
    query_stations_sql = f"select station_name from t_station_main_gongjiao where line_name='{line[0]}';"
    cur.execute(query_stations_sql)
    line_stations = list(map(lambda x:list(x),cur.fetchall()))
    for station in line_stations:
        count_lines = []
        for i in jiebo:
            tmp = i.strip('\n').split(',')
            if tmp[0] == line[0] and tmp[1] == station[0] and tmp[2] not in count_lines:
                count_lines.append(tmp[2])
        if len(count_lines) >= 3:
            p.write("{},{},{}\n".format(line[0],station[0],len(count_lines)))

cur.close()
conn.close()
f.close()
p.close()