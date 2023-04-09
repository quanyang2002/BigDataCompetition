f = open('../static/punchcard.csv','r',encoding='utf-8')
data = f.readlines()

addresses = []
for line in data[1:]:
    address = line.strip('\n').split(',')[-2]
    if address != "" and address not in addresses:
        addresses.append(address)
print(addresses)

dic = {}
for address in addresses:
    for line in data[1:]:
        cur_course = line.strip('\n').split(',')[5]
        cur_address = line.strip('\n').split(',')[8]
        if cur_address == address:
            if address not in list(dic.keys()):
                dic[address] = [cur_course]
            elif cur_course not in dic[address]:
                dic[address].append(cur_course)
print(dic)