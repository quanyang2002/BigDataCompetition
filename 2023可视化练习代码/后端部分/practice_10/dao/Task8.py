f = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\ICdata_20220301_utf8sig_sam.csv",'r',encoding="UTF-8")
p = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\乘车时长.csv",'w',encoding="UTF-8")
p.close()
p = open("D:\\学习\\大数据\\技能大赛\\2023可视化练习代码\\后端部分\\practice_10\\dao\\乘车时长.csv",'a',encoding="UTF-8")
p.write("日期,公交线路,平均乘车时长(分钟)\n")
data = f.readlines()[1:]
dates = []
lines = []
for line in data:
    cur_date = line.strip('\n').split(',')[3].split()[0]
    cur_gongjiao = eval(line.strip('\n').split(',')[1])
    if cur_gongjiao > 0 and cur_gongjiao not in lines:
        lines.append(cur_gongjiao)
    if cur_date not in dates:
        dates.append(cur_date)
# print(lines)
# print(dates)
def judge(dat):
    if dat == "00":
        return '0'
    else:
        return dat.lstrip('0')
def calc_dis(time1,time2):
    tim1 = time1.split()
    tim2 = time2.split()
    tim1_tmp = tim1[1].split(':')
    tim2_tmp = tim2[1].split(':')
    if tim1[0] == tim2[0]:
        if tim1_tmp[0] == tim2_tmp[0]:
            return eval(judge(tim2_tmp[1])) - eval(judge(tim1_tmp[1]))
        else:
            if eval(tim2_tmp[0]) - eval(tim1_tmp[0]) == 1:
                return (60 - eval(judge(tim1_tmp[1]))) + eval(judge(tim2_tmp[1]))
            else:
                return (eval(tim2_tmp[0]) - eval(tim1_tmp[0]))*60 + eval(judge(tim2_tmp[1]))
    else:
        if (24 - eval(tim1_tmp[0])) == 1:
            return (60 - eval(judge(tim1_tmp[1]))) + eval(tim2_tmp[0]) * 60 + eval(judge(tim2_tmp[1]))
        else:
            return ((24 - eval(tim1_tmp[0]))-1)*60 + eval(tim2_tmp[0]) * 60 + eval(judge(tim2_tmp[1]))

for date in dates:
    for line in lines:
        tmp = []
        for i in data:
            data_tmp = i.strip('\n').split(',')
            if data_tmp[3].split()[0] == date and eval(data_tmp[1]) == line:
                tmp.append(calc_dis(data_tmp[3],data_tmp[7]))
        # print(tmp)
        p.write("{},{},{}\n".format(date.replace("/",'.'),line,sum(tmp)/len(tmp)))

f.close()
p.close()