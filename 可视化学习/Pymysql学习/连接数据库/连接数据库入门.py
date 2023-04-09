import pymysql

"""
参数说明:
host:MySQL安装位置主机IP地址
port:MySQL端口号
charset:字符集
user:指定MySQL登录用户
password:指定MySQL登录用户密码
database:指定连接的数据库
"""
conn = pymysql.connect(host="localhost",
                       port=3306,
                       charset="UTF8",
                       user="root",
                       passwd="password",
                       database="pymysql")
if conn:
    print(conn.get_server_info())
# 关闭连接对象
conn.close()