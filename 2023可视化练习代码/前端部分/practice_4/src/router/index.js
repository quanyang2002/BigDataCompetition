import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'

import Task1 from "../components/Task1";
import Task2 from "../components/Task2";
import Task3 from "../components/Task3";

Vue.use(Router)

export default new Router({
  mode:'history',
  routes: [
    {
      path: '/task1',
      name: 'task1',
      component: Task1
    },
    {
      path: '/task2',
      name: 'task2',
      component: Task2
    },
    {
      path: '/task3',
      name: 'task3',
      component: Task3
    }
  ]
})
