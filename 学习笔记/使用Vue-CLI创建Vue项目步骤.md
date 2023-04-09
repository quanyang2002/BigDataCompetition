### 使用Vue-CLI创建Vue项目步骤

#### 前置环境准备

##### 安装node.js

node官网:[Node.js (nodejs.org)](https://nodejs.org/zh-cn/)

下载windows版本

![image-20221021194058221](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\vue-cli创建项目.png)

**注意：选择长期维护版**

无脑安装中……

测试node.js是否安装成功

![image-20221021194410620](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\vue-cli创建项目测试node是否安装成功.png)

##### 安装npm国内镜像cnpm

在命令行中输入：

````shell
npm install -g cnpm –registry=http://registry.npm.taobao.org
````

##### 安装vue脚手架工具vue-cli

在命令行中输入：

````shell
npm install -g vue-cli
````

#### 使用vue-cli脚手架创建vue项目

##### 初始化vue项目

在命令行中输入：

````shell
vue init webpack 项目名称
````

命令解释：

- vue init表示初始化一个vue项目
- webpack表示整个项目是基于webpack构建的

初始化vue项目时弹出的信息解释：

````markdown
列表如下
(1)? Project name (VueTest002)：项目名称name can no longer contain capital letters（不能大写）
(2)? Project description (A Vue.js project) vue-cli新建项目(项目描述)；
(3)? Author (xhdx <zhuming3834@sina.com>) ；zhuming3834@sina.com(项目作者)；
(4)? Vue build
❯ Runtime + Compiler: recommended for most users
Runtime-only: about 6KB lighter min+gzip, but templates (or any Vue-specific HTML) are ONLY 
allowed in .vue files - render functions are required elsewhere
这里选择Runtime + Compiler: recommended for most users；
(5)? Install vue-router? (Y/n) y 是否使用vue-router，这里根据需求选择；
(6)? Use ESLint to lint your code? (Y/n) y 是否使用ESLint，这里根据需求选择；
(7)? Pick an ESLint preset (Use arrow keys)
❯ Standard (https://github.com/feross/standard)
Airbnb (https://github.com/airbnb/javascript) none (configure it yourself) 
这里选择	Standard(https://github.com/feross/standard)
(8)? Setup unit tests with Karma + Mocha? (Y/n) n 是否需要单元测试，这里根据需求选择；
(9)Setup e2e tests with Nightwatch? (Y/n) n是否需要单元测试，这里根据需求选择；
(10) Should we run `npm install` for you after the project has been created? 
(recommended) (Use arrow keys)
创建项目后需要需要安装项目所需要的依赖，这里选择no，本步骤作为步骤6说明
````

##### 安装项目所需依赖

首先进入项目目录

````shell
cd 项目名称
````

然后在命令行输入：

````shell
cnpm install
````

#### 运行Vue项目

在在命令行输入：

````shell
npm run dev
````

在浏览器输入：localhost:8080

![image-20221021200817914](D:\学习\大数据\技能大赛\学习笔记\学习笔记截图\vue-cli创建项目运行项目.png)