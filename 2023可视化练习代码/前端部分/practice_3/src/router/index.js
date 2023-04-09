import Vue from 'vue'
import Router from 'vue-router'

import Task1 from '../components/Task1'
import Task2 from "../components/Task2"
import Task3 from "../components/Task3"
import Task4 from "../components/Task4"
import Task5 from "../components/Task5"

Vue.use(Router)

export default new Router({
  mode:'history',
  routes:[
    {
      path:'/task1',
      component:Task1
    },
    {
      path:'/task2',
      component:Task2
    },
    {
      path:'/task3',
      component:Task3
    },
    {
      path:'/task4',
      component:Task4
    },
    {
      path:'/task5',
      component:Task5
    }
  ]
})
