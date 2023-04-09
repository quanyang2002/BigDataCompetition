from data_process.task1_data import class_count
f = open(r'static/学生成绩.csv','r',encoding='gbk')
data = f.readlines()
table_header = []
for line in data:
    cur_class = line.strip('\n').split(',')[0]
    if cur_class not in table_header:
        table_header.append(cur_class)
pub1_avg = []
if_idx = [0,4,8,12]
for clas in table_header:
    s = 0.0
    for line in data:
        tmp = line.strip('\n').split(',')
        if tmp[0] == clas:
            score = list(map(lambda x:eval(x),tmp[2:]))
            for idx in if_idx:
                s += score[idx]
    pub1_avg.append(s/(class_count[table_header.index(clas)]*4))

