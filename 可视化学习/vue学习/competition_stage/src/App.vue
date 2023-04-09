<template>
  <div id="app">
<!--    <router-link to="/task1">task1</router-link>-->
<!--    <router-link to="/task2">task2</router-link>-->
    <router-view></router-view>
  </div>
</template>

<script>
import Task1 from './components/Task1'
import Task2 from './components/Task2'

export default {
  name: 'App',
  components: {
    Task1,
    Task2
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
