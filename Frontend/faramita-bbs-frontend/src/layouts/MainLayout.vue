<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { NLayout, NLayoutContent } from 'naive-ui'
import TopNav from '@/components/TopNav.vue'
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'

const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)

const gridOffsetX = ref('0px')
const gridOffsetY = ref('0px')
const gridWaveX = ref('0px')
const gridWaveY = ref('0px')
const gridPointerX = ref('50%')
const gridPointerY = ref('50%')
const gridWaveAlpha = ref('0')

const gridStyle = computed(() => ({
  '--grid-offset-x': gridOffsetX.value,
  '--grid-offset-y': gridOffsetY.value,
  '--grid-wave-x': gridWaveX.value,
  '--grid-wave-y': gridWaveY.value,
  '--grid-pointer-x': gridPointerX.value,
  '--grid-pointer-y': gridPointerY.value,
  '--grid-wave-alpha': gridWaveAlpha.value,
}))

const handlePointerMove = (event: PointerEvent) => {
  const relativeX = event.clientX / window.innerWidth - 0.5
  const relativeY = event.clientY / window.innerHeight - 0.5
  const x = relativeX * 1.4
  const y = relativeY * 1.1

  gridOffsetX.value = `${x.toFixed(2)}px`
  gridOffsetY.value = `${y.toFixed(2)}px`
  gridWaveX.value = `${(-x * 1.6).toFixed(2)}px`
  gridWaveY.value = `${(-y * 1.6).toFixed(2)}px`
  gridPointerX.value = `${event.clientX + 24}px`
  gridPointerY.value = `${event.clientY + 24}px`
  gridWaveAlpha.value = '0.26'
}

const resetGridOffset = () => {
  gridOffsetX.value = '0px'
  gridOffsetY.value = '0px'
  gridWaveX.value = '0px'
  gridWaveY.value = '0px'
  gridWaveAlpha.value = '0'
}

onMounted(() => {
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerleave', resetGridOffset)
})

onUnmounted(() => {
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerleave', resetGridOffset)
})
</script>

<template>
  <n-layout class="main-layout" :class="{ 'main-layout--dark': isDark }" position="absolute">
    <TopNav />
    <n-layout-content class="content" :native-scrollbar="false" :style="gridStyle">
      <div class="grid-lines-container" aria-hidden="true"></div>
      <div class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
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
  --grid-offset-x: 0px;
  --grid-offset-y: 0px;
  --grid-wave-x: 0px;
  --grid-wave-y: 0px;
  --grid-pointer-x: 50%;
  --grid-pointer-y: 50%;
  --grid-wave-alpha: 0;
}

.page-container {
  max-width: 100%;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 1;
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
.grid-lines-container {
  position: fixed;
  inset: -24px;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
  opacity: 0.55;
  transform: translate3d(var(--grid-offset-x), var(--grid-offset-y), 0);
  transition: transform 0.42s ease-out;
  background-image:
    linear-gradient(to right, rgba(0, 0, 0, 0.06) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0, 0, 0, 0.06) 1px, transparent 1px);
  background-size: 44px 44px;
  background-position: center;
}

.grid-lines-container::after {
  content: '';
  position: absolute;
  inset: 0;
  opacity: var(--grid-wave-alpha);
  transform: translate3d(var(--grid-wave-x), var(--grid-wave-y), 0) scale(1.002);
  transition: opacity 0.35s ease, transform 0.46s ease-out;
  background-image:
    linear-gradient(to right, rgba(0, 109, 119, 0.11) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0, 109, 119, 0.11) 1px, transparent 1px);
  background-size: 44px 44px;
  background-position: center;
  -webkit-mask-image: radial-gradient(circle at var(--grid-pointer-x) var(--grid-pointer-y), #000 0, rgba(0, 0, 0, 0.72) 96px, transparent 230px);
  mask-image: radial-gradient(circle at var(--grid-pointer-x) var(--grid-pointer-y), #000 0, rgba(0, 0, 0, 0.72) 96px, transparent 230px);
}

.main-layout--dark .grid-lines-container {
  opacity: 0.42;
  background-image:
    linear-gradient(to right, rgba(255, 255, 255, 0.07) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.07) 1px, transparent 1px);
}

.main-layout--dark .grid-lines-container::after {
  background-image:
    linear-gradient(to right, rgba(131, 197, 190, 0.13) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(131, 197, 190, 0.13) 1px, transparent 1px);
}

@media (prefers-reduced-motion: reduce) {
  .grid-lines-container,
  .grid-lines-container::after {
    transition: none;
    transform: none;
  }

  .grid-lines-container::after {
    opacity: 0;
  }
}
</style>
