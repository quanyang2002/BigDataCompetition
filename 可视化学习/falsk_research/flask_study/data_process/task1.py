f = open("../static/punchcard.csv",'r',encoding='utf-8')
data = f.readlines()

courses = []
for line in data[1:]:
    course = line.strip('\n').split(',')[5]
    if course not in courses:
        courses.append(course)
# print(courses)
for course in courses:
    course_data = {}
    for line in data[1:]:
        cur_course = line.strip('\n').split(',')[5]
        if cur_course == course:
            face = line.strip('\n').split(',')[-1]
            if face != "":
                course_data[face] = course_data.get(face,0)+1
    print(course,course_data)
f.close()
