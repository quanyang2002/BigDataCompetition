f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_4\\dao\\模拟测试5 停车场信息表 可视化题目数据.csv",'r',encoding="gbk")
data = f.readlines()

task2_x = []
for line in data:
    month = line.strip('\n').split(',')[1].split()[0].split('-')[1].lstrip('0')
    if month not in task2_x:
        task2_x.append(month)
task2_y = []
for month in task2_x:
    count = 0
    for line in data:
        m = line.strip('\n').split(',')[1].split()[0].split('-')[1].lstrip('0')
        if m == month:
            count += 1
    task2_y.append(count)

task2_x = list(map(lambda x:x+"月",task2_x))
print(task2_x,task2_y)

f.close()