f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\公交站点长度统计.csv",'r',encoding="UTF-8")
data = f.readlines()[1:]
p = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\公交线路长度统计.csv",'w',encoding="UTF-8")
p.close()
p = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\公交线路长度统计.csv",'a',encoding="UTF-8")
p.write("线路名称,线路长度\n")
lines = list(set(list(map(lambda x:x.strip('\n').split(',')[0],data))))
for line in lines:
    s = 0.0
    for i in data:
        if i.strip('\n').split(',')[0] == line:
            s += eval(i.strip('\n').split(',')[-1])
    p.write("{},{}\n".format(line,s))
p.close()
f.close()
