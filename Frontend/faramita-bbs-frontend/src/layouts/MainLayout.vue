<script setup lang="ts">
import { NLayout, NLayoutContent } from 'naive-ui'
import TopNav from '@/components/TopNav.vue'
</script>

<template>
  <n-layout class="main-layout" position="absolute">
    <TopNav />
    <n-layout-content class="content" :native-scrollbar="false">
      <div class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
      
      <!-- Decorative Lines -->
      <div class="deco-lines-container">
        <div class="deco-group left">
          <div class="deco-line"></div>
          <div class="deco-line"></div>
          <div class="deco-line"></div>
        </div>
        <div class="deco-group right">
          <div class="deco-line"></div>
          <div class="deco-line"></div>
          <div class="deco-line"></div>
        </div>
      </div>
    </n-layout-content>
  </n-layout>
</template>

<style scoped>
.main-layout {
  height: 100vh;
  background-color: transparent !important;
}

:deep(.n-layout-scroll-container) {
  background-color: transparent !important;
}

.content {
  background-color: transparent !important;
}

.page-container {
  max-width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Decorative Lines Styles */
.deco-lines-container {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.deco-group {
  position: absolute;
  bottom: -50px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.deco-group.left {
  left: -250px;
  bottom: -100px;
  transform: rotate(45deg);
  transform-origin: bottom right;
}

.deco-group.right {
  right: -250px;
  bottom: -100px;
  transform: rotate(-45deg);
  transform-origin: bottom left;
}

.deco-line {
  width: 500px;
  height: 25px;
  background-color: var(--deco-line-color);
  opacity: 0.8;
}
</style>
