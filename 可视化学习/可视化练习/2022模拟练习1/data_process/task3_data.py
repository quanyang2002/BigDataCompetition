f = open(r'static/学生成绩.csv','r',encoding='gbk')
data = f.readlines()
name = []
course_1 = []
course_2 = []
course_3 = []
course_4 = []
for line in data:
    dat = line.strip('\n').split(',')
    if dat[0] == '软件1801':
        name.append(dat[1])
        course_4.append(eval(dat[-1]))
        course_3.append(eval(dat[-2]))
        course_2.append(eval(dat[-3]))
        course_1.append(eval(dat[-4]))
