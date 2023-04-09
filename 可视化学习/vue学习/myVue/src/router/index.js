import Vue from 'vue'
import VueRouter from "vue-router"

// 引入组件
import Content from '../components/Content'
import main from '../components/main'

// 安装路由
Vue.use(VueRouter);

// 配置导出路由
export default new VueRouter({
  routes : [
    {
      // 路由路径
      path : '/content',
      // 路由名称
      name : 'content',
      // 跳转的组件
      component: Content
    },
    {
      // 路由路径
      path : '/main',
      // 路由名称
      name : 'main',
      // 跳转的组件
      component: main
    }
  ]
});

