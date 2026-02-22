import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Fonts
import 'vfonts/Lato.css'
import 'vfonts/FiraCode.css'
import '@/styles/global.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
