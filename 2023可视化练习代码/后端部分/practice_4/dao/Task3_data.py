f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_4\\dao\\模拟测试5 停车场信息表 可视化题目数据.csv",'r',encoding="gbk")
data = f.readlines()
dates = [f"2018-01-0{i}" for i in range(1,10)]
dates.append("2018-01-10")
# print(dates)
# 计算每日停车总数
counts = []
for date in dates:
    count = 0
    for line in data:
        timeIn = line.strip('\n').split(',')[1]
        timeOut = line.strip('\n').split(',')[2]
        if timeIn.split()[0] == date or timeOut.split()[0] == date:
            count += 1
    counts.append(count)
# print(counts)

# 计算总时长
times = []
for date in dates:
    s = 0
    for line in data:
        timeIn = line.strip('\n').split(',')[1]
        timeOut = line.strip('\n').split(',')[2]
        # 同一天
        if timeIn.split()[0] == timeOut.split()[0]:
            inTime = timeIn.replace('00','01').split()[1].split(":")
            outTime = timeOut.replace('00','01').split()[1].split(":")
            inTime = list(map(lambda x:x.lstrip('0'),inTime))
            outTime = list(map(lambda x:x.lstrip('0'),outTime))
            # 小时相同
            if inTime[0] == outTime[0]:
                s += eval(outTime[1])-eval(inTime[1])
            else:
                # print(outTime[0],inTime[0],outTime[1],inTime[1])
                s += (((eval(outTime[0])-eval(inTime[0]))*60) - (eval(outTime[1])-eval(inTime[1])))
        else:
            # 不同天
            # 判断到底是进入时间与当前遍历时间同天还是出场时间与遍历时间同天
            if timeIn.split()[0] == date:
                s += (24-eval(inTime[0]))*60 - eval(timeIn[1])
            elif timeOut.split()[0] == date:
                s += eval(outTime[0])*60 + eval(outTime[1])
    times.append(s)
# print(times)

avgs = [times[i]/counts[i] for i in range(len(times))]
avgs = list(map(lambda x:round(x),avgs))
print(dates)
print(avgs)
f.close()