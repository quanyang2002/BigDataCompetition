import threading
import time
import random

# pre_path = ''
pre_path = '/opt/flume/data/'


def fun(nameSave, nameOpen):
    count = 1
    f2 = open(pre_path + nameOpen, 'w')
    random_list = ['1001', '1002', '1003', '1004', '1005', '1006']
    while True:
        f = open(pre_path + nameSave, encoding='utf-8')
        while True:
            f2 = open(pre_path + nameOpen, 'a')
            status = random.choice(random_list)
            line = f.readline()
            if nameSave == 'order_info.txt':
                line = line.replace("1005", status)
            f2.write(line)  # 将字符串写入文件中
            f2.close()
            time.sleep(random.uniform(0.5,1.5))
            if line:
                print(count)
                count = count + 1
            else:
                break


if __name__ == '__main__':
    threading.Thread(target=fun, args=('order_info.txt', 'info.txt')).start()
    threading.Thread(target=fun, args=('order_detail.txt', 'detail.txt')).start()
    threading.Thread(target=fun, args=('producerecord.txt', 'pr.txt')).start()
    threading.Thread(target=fun, args=('environmentdata.txt', 'ed.txt')).start()
    threading.Thread(target=fun, args=('changerecord.txt', 'cr.txt')).start()
