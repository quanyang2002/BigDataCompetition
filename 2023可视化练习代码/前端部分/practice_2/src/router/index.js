import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import Task2 from '../components/Task2'
import Task3 from '../components/Task3'
import Task4 from '../components/Task4'
import Task5 from "../components/Task5"
import Task6 from "../components/Task6"

Vue.use(Router)

export default new Router({
  mode:'history',
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
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
    },
    {
      path: '/task4',
      name: 'task4',
      component: Task4
    },
    {
      path: '/task5',
      name: 'task5',
      component: Task5
    },
    {
      path: '/task6',
      name: 'task6',
      component: Task6
    }
  ]
})
