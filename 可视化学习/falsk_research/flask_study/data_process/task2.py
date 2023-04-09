f = open('../static/punchcard.csv','r',encoding='utf-8')
data = f.readlines()

classes = []
for line in data[1:]:
    cur_class = line.strip('\n').split(',')[2]
    if cur_class not in classes:
        classes.append(cur_class)
# print(classes)
chuqin = []
for classidx in classes:
    s = 0
    quexi = 0
    for line in data[1:]:
        cur_class = line.strip('\n').split(',')[2]
        if cur_class == classidx:
            s += 1
            if line.strip('\n').split(',')[-1]=="":
                quexi += 1
    chuqin.append((s-quexi)/s)
print(classes)
print(chuqin)