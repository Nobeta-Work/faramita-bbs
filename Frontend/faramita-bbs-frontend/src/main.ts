import { createApp } from 'vue'
import { createPinia } from 'pinia' // pinia
import App from './App.vue'
import router from './router'
// Element Plus * 3
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const pinia = createPinia()
const app = createApp(App)

app.use(ElementPlus)
// 注册Element图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(pinia)
app.use(router)
app.mount('#app')
