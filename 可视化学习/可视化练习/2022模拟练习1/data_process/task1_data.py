f = open(r'static/学生成绩.csv','r',encoding='gbk')
data = f.readlines()
table_header = []
for line in data:
    cur_class = line.strip('\n').split(',')[0]
    if cur_class not in table_header:
        table_header.append(cur_class)
class_count = []
for clas in table_header:
    num = 0
    for line in data:
        cur_class = line.strip('\n').split(',')[0]
        if cur_class == clas:
           num += 1
    class_count.append(num)
f.close()
