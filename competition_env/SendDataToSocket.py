from socket import *
import time
import random
udp_socket = socket(AF_INET,SOCK_RAW)
sink = ("127.0.0.1",8999)
f = open("data_merge.txt","r+",encoding="utf-8")
while True:
    source = f.readlines()
    for line in source:
        udp_socket.sendto(line.encode("utf-8"),sink)
        time.sleep(random.choice([1,2,3]))
    # f.seek(0,0)
    udp_socket.close()