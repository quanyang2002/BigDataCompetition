import pymysql

conn = pymysql.connect(host="localhost",user="root",password="password",port=3306,database="spider")
cur = conn.cursor()

query_sql = "select style from music;"

cur.execute(query_sql)
query_data = cur.fetchall()
styles = []
ori_styles = []
for item in query_data:
    tmp = item[0].strip().split()
    if len(tmp)>1:
        for i in tmp:
            ori_styles.append(i)
            if i not in styles:
                styles.append(i)
    else:
        if tmp != []:
            ori_styles.append(tmp[0])
            if tmp[0] not in styles:
                styles.append(tmp[0])

print(styles)
count = []
for style in styles:
    count.append(ori_styles.count(style))
cur.close()
conn.close()
