f = open("./接驳站点.csv",'r',encoding="UTF-8")
p = open("./接驳站点(gbk).csv",'w',encoding="GBK")
p.close()
p = open("./接驳站点(gbk).csv",'a',encoding="GBK")
for line in f.readlines()[1:]:
    p.write(line)
f.close()
p.close()
