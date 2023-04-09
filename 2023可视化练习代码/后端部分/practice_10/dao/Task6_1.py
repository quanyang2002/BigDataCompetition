from practice_10.dao.Task6 import station_data

f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\OD站点统计.csv","w",encoding="UTF-8")
f.close()
f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\OD站点统计.csv","a",encoding="UTF-8")
f.write("OD站点名称,共用线路的名称\n")
for station in station_data:
    if station[1] > 1:
        f.write("{},{}\n".format(station[0],station[1]))
f.close()