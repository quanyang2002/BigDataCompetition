from flask import Flask
from flask import url_for

app = Flask(__name__)

@app.route('/')
def hello():
    return "hello flask!"

@app.route('/user/<name>')
def user(name):
    return "hello %s"%name

# @app.route('/test')
# def test_url_for():
#     print(url_for('hello'))
#     print(url_for('user',name='quanyang'))
#     print(url_for('user',name='hany'))
#     print(url_for('test_url_for'))
#     print(url_for('test_url_for',num=2))
#     return 'Test Page!'

if __name__=="__main__":
    app.run(host="127.0.0.1",port=5000)