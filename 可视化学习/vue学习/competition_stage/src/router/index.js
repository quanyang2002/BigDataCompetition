import Task1 from "../components/Task1";
import Task2 from "../components/Task2";

import VueRouter from "vue-router";
import Vue from 'vue';

Vue.use(VueRouter);

export default new VueRouter({
  mode:'history',
  routes:[
    {
      path:'/task1',
      component:Task1
    },
    {
      path:'/task2',
      component:Task2
    }
  ]
})
