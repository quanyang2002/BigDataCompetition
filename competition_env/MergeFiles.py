import os

sources = os.listdir("D:\\学习\\大数据\\技能大赛\\competition_env\\source")

f = open("./data_merge.txt","a+",encoding="utf-8")
for file in sources:
    p = open("D:\\学习\\大数据\\技能大赛\\competition_env\\source\\" + file,"r+",encoding="utf-8")
    cur_source = p.readlines()
    for line in cur_source:
        if (line != "" and line != "\n"):
            f.write(line)
    p.close()
f.close()

