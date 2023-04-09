import pymysql

conn = pymysql.connect(host="localhost",
                       port=3306,
                       user="root",
                       password="password",
                       database="spider")
f = open("./接驳站点统计.csv",'w',encoding="UTF-8")
p = open("./接驳站点.csv",'r',encoding="UTF-8")
jiebo = p.readlines()[1:]
f.close()
f = open("./接驳站点统计.csv",'a',encoding="UTF-8")
f.write("线路名称,接驳站点数量,非接驳站点数量\n")
cur = conn.cursor()
query_lines_sql = "select line_name,count(station_name) from t_station_main_gongjiao group by line_name;"
cur.execute(query_lines_sql)
lines_counts = list(map(lambda x:list(x),cur.fetchall()))
def calc_dis(j1,j2,w1,w2):
    result = (((j2-j1)*87622)**2+((w2-w1)*111194)**2)**0.5
    return result

for line in lines_counts:
    jiebo_count = []
    for i in jiebo:
        tmp = i.strip('\n').split(',')
        if tmp[0] == line[0] and tmp[1] not in jiebo_count:
            jiebo_count.append(tmp[1])
    f.write("{},{},{}\n".format(line[0],len(jiebo_count),line[1]-len(jiebo_count)))

cur.close()
conn.close()
f.close()
p.close()