<script setup lang="ts">
import TopNav from '@/components/TopNav.vue';
</script>

<template>
  <div class="app-container">
    <TopNav />
    <router-view></router-view>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  overflow: hidden;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

#app {
  height: 100%;
  width: 100%;
}
</style>

<style scoped>
.app-container {
  height: 100%;
  width: 100%;
}
</style>