<template>
  <router-view></router-view>
</template>
<script>
import Task2 from './components/Task2'
import Task3 from './components/Task3'
import Task4 from './components/Task4'
import Task5 from "./components/Task5"
import Task6 from "./components/Task6"

export default {
  name: 'App',
  components:{
    Task2,
    Task3,
    Task4,
    Task5,
    Task6
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
