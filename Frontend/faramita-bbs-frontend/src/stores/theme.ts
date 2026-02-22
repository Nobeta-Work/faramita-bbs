import { defineStore } from 'pinia'
import { ref } from 'vue'
import { darkTheme, type GlobalTheme } from 'naive-ui'

export const useThemeStore = defineStore('theme', () => {
  // 从 localStorage 读取初始主题状态
  const savedTheme = localStorage.getItem('theme')
  const isDark = ref(savedTheme === 'dark')
  const theme = ref<GlobalTheme | null>(isDark.value ? darkTheme : null)

  // 初始化时应用主题
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    theme.value = isDark.value ? darkTheme : null

    // 更新 DOM
    if (isDark.value) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }

  return { isDark, theme, toggleTheme }
})
