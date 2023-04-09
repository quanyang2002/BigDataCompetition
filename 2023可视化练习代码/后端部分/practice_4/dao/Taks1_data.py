f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_4\\dao\\模拟测试5 停车场信息表 可视化题目数据.csv",'r',encoding="gbk")
data = f.readlines()
task1_x = []
for line in data:
    month = line.strip('\n').split(',')[1].split()[0].split('-')[1].lstrip('0')
    if month not in task1_x:
        task1_x.append(month)
task1_y = []
for m in task1_x:
    s = 0
    for line in data:
        mon = line.strip('\n').split(',')[1].split()[0].split('-')[1].lstrip('0')
        if mon == m:
            s += eval(line.strip('\n').split(',')[3])
    task1_y.append(s)
task1_x = list(map(lambda x:x+"月",task1_x))
print(task1_x,task1_y)

f.close()