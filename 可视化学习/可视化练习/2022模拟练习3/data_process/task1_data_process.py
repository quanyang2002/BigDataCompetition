f = open("../static/data2.txt",'r',encoding="utf-8")
p = open("../static/data2.csv",'a+',encoding="utf-8")
p.close()
p = open("../static/data2.csv",'w',encoding="utf-8")
data = f.readlines()

for line in data:
    line = line.replace('	',',').replace(' ',',')
    p.write(line)
p.close()