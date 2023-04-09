<template>
  <div id="app">
    <first_vue_echarts/>
  </div>
</template>

<script>
import HelloWorld from './components/HelloWorld'
import first_vue_echarts from "./components/first_vue_echarts"

export default {
  name: 'App',
  components: {
    HelloWorld,
    first_vue_echarts
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
