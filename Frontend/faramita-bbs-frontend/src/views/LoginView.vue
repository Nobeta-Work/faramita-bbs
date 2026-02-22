<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage, type FormInst, type FormRules, NForm, NFormItem, NInput, NIcon } from 'naive-ui';
import { login } from '@/api/user';
import { useUserStore } from '@/stores/user';
import { PersonOutline, LockClosedOutline, ArrowForwardOutline } from '@vicons/ionicons5';
import FaramitaLogo from '@/assets/images/logo/FaramitaBBSLogo.png';

// Fonts
const fontLink = document.createElement('link')
fontLink.href = 'https://fonts.googleapis.com/css2?family=Lato:wght@300;400;700&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap'
fontLink.rel = 'stylesheet'
document.head.appendChild(fontLink)

const router = useRouter()
const store = useUserStore()
const message = useMessage()
const formRef = ref<FormInst | null>(null)

const loginForm = reactive({
    username: '',
    password: '',
})

const rules: FormRules = {
    username: { required: true, message: 'Username is required', trigger: 'blur' },
    password: { required: true, message: 'Password is required', trigger: 'blur' }
}

const showForm = ref(false)
const loading = ref(false)

// Canvas Refs
const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let animationFrameId: number
let mouseX = -1000
let mouseY = -1000

// Canvas Animation Logic
const initCanvas = () => {
  if (!canvasRef.value) return
  const canvas = canvasRef.value
  ctx = canvas.getContext('2d')
  if (!ctx) return

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  window.addEventListener('resize', resize)
  resize()

  const gap = 30 // Grid gap
  const pointSize = 1.5
  
  const animate = () => {
    if (!ctx || !canvas) return
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    
    // Theme colors
    const isDark = document.documentElement.classList.contains('dark')
    ctx.fillStyle = isDark ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.15)'
    
    const time = Date.now() * 0.001

    for (let x = 0; x <= canvas.width; x += gap) {
      for (let y = 0; y <= canvas.height; y += gap) {
        // Distance to mouse
        const dx = x - mouseX
        const dy = y - mouseY
        const dist = Math.sqrt(dx * dx + dy * dy)
        
        // Wave effect params
        const maxDist = 250
        let offsetX = 0
        let offsetY = 0
        let scale = 1

        if (dist < maxDist) {
          const force = (maxDist - dist) / maxDist
          // Repulsion + Wave
          const angle = Math.atan2(dy, dx)
          const wave = Math.sin(dist * 0.05 - time * 2) * 5 * force
          
          offsetX = Math.cos(angle) * (force * 20 + wave)
          offsetY = Math.sin(angle) * (force * 20 + wave)
          scale = 1 + force * 1.5
        }

        ctx.beginPath()
        ctx.arc(x + offsetX, y + offsetY, pointSize * scale, 0, Math.PI * 2)
        ctx.fill()
      }
    }
    
    animationFrameId = requestAnimationFrame(animate)
  }
  animate()
  
  return resize
}

const handleMouseMove = (e: MouseEvent) => {
  mouseX = e.clientX
  mouseY = e.clientY
}

let resizeHandler: (() => void) | undefined

onMounted(() => {
    setTimeout(() => {
        showForm.value = true
    }, 100)
    resizeHandler = initCanvas()
    window.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId)
  window.removeEventListener('mousemove', handleMouseMove)
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
})

const handleLogin = async () => {
    formRef.value?.validate(async (errors) => {
        if (!errors) {
            loading.value = true
            try {
                const res = await login(loginForm)
                store.setUserInfo(res)
                message.success('Welcome back')
                router.push('/')
            } catch (error: any) {
                message.error(error.message || 'Login failed')
            } finally {
                loading.value = false
            }
        }
    })
}
</script>

<template>
    <div class="login-page" :class="{ 'loaded': showForm }">
        <canvas ref="canvasRef" class="bg-canvas"></canvas>

        <div class="login-container animate-section" v-if="showForm">
            <div class="login-card animate-scale-in">
                <div class="header-section">
                    <img :src="FaramitaLogo" alt="Faramita BBS Logo" class="logo-image" />
                    <h1 class="title">SIGN IN</h1>
                    <div class="decorative-line"></div>
                    <p class="subtitle">Enter your credentials to access your account.</p>
                </div>
                
                <n-form ref="formRef" :model="loginForm" :rules="rules" class="login-form" @keyup.enter="handleLogin">
                    <n-form-item label="USERNAME" path="username">
                        <n-input v-model:value="loginForm.username" placeholder="Your username" class="custom-input">
                            <template #prefix>
                                <n-icon :component="PersonOutline" />
                            </template>
                        </n-input>
                    </n-form-item>
                    <n-form-item label="PASSWORD" path="password">
                        <n-input type="password" show-password-on="click" v-model:value="loginForm.password" placeholder="Your password" class="custom-input">
                            <template #prefix>
                                <n-icon :component="LockClosedOutline" />
                            </template>
                        </n-input>
                    </n-form-item>
                    
                    <div class="form-actions">
                        <button class="submit-btn" @click.prevent="handleLogin" :disabled="loading">
                            <span>{{ loading ? 'AUTHENTICATING...' : 'SIGN IN' }}</span>
                            <n-icon :component="ArrowForwardOutline" />
                        </button>
                        
                        <div class="divider-wrapper">
                            <span class="divider-line"></span>
                            <span class="divider-text">OR</span>
                            <span class="divider-line"></span>
                        </div>
                        
                        <div class="secondary-actions">
                            <button class="text-btn" @click.prevent="router.push('/register')">CREATE ACCOUNT</button>
                            <span class="dot-separator">•</span>
                            <button class="text-btn" @click.prevent="router.push('/')">RETURN HOME</button>
                        </div>
                    </div>
                </n-form>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* Animations */
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95) translateY(20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.animate-scale-in {
  opacity: 0;
  animation: scaleIn 0.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.login-page {
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    overflow: hidden;
    background-color: var(--bg-primary);
    color: var(--text-primary);
    font-family: 'Lato', sans-serif;
    transition: background-color 0.5s ease, color 0.5s ease;
}

.bg-canvas {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
    pointer-events: none;
    opacity: 0;
    transition: opacity 1.5s ease;
}

.login-page.loaded .bg-canvas {
    opacity: 1;
}

.login-container {
    position: relative;
    z-index: 10;
    width: 100%;
    max-width: 480px;
    padding: 0 20px;
}

.login-card {
    background: var(--modal-bg);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    padding: 60px 50px;
    border: 1px solid var(--line-color);
    box-shadow: 0 25px 50px rgba(0,0,0,0.1);
}

.header-section {
    text-align: center;
    margin-bottom: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.logo-image {
    width: 100%;
    max-width: 300px;
    height: auto;
    margin: 0 auto 30px;
    display: block;
    filter: drop-shadow(0 10px 15px rgba(0,0,0,0.1));
}

.title {
    font-family: 'Playfair Display', serif;
    font-size: 2.5rem;
    font-weight: 400;
    margin: 0 0 15px;
    letter-spacing: 4px;
    color: var(--text-primary);
}

.decorative-line {
    width: 40px;
    height: 1px;
    background-color: var(--accent-color);
    margin: 0 auto 20px;
}

.subtitle {
    color: var(--text-secondary);
    margin: 0;
    font-size: 0.9rem;
    font-style: italic;
    font-family: 'Playfair Display', serif;
}

.login-form :deep(.n-form-item-label) {
    font-size: 0.75rem;
    letter-spacing: 2px;
    color: var(--text-tertiary);
    font-family: 'Lato', sans-serif;
}

.custom-input {
    background: transparent;
}

.custom-input :deep(.n-input__input-el) {
    font-family: 'Lato', sans-serif;
    font-size: 1rem;
}

.form-actions {
    margin-top: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.submit-btn {
    width: 100%;
    background: var(--text-primary);
    color: var(--bg-primary);
    border: none;
    padding: 15px 0;
    font-family: 'Lato', sans-serif;
    font-weight: 700;
    letter-spacing: 2px;
    font-size: 0.85rem;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
    background: var(--accent-color);
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

.submit-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.divider-wrapper {
    display: flex;
    align-items: center;
    width: 100%;
    margin: 30px 0;
}

.divider-line {
    flex: 1;
    height: 1px;
    background-color: var(--line-color);
}

.divider-text {
    padding: 0 15px;
    font-size: 0.75rem;
    letter-spacing: 2px;
    color: var(--text-tertiary);
}

.secondary-actions {
    display: flex;
    align-items: center;
    gap: 15px;
}

.text-btn {
    background: transparent;
    border: none;
    color: var(--text-secondary);
    font-family: 'Lato', sans-serif;
    font-size: 0.75rem;
    letter-spacing: 1px;
    cursor: pointer;
    transition: color 0.3s ease;
    padding: 0;
}

.text-btn:hover {
    color: var(--accent-color);
}

.dot-separator {
    color: var(--text-tertiary);
    font-size: 0.8rem;
}

@media (max-width: 480px) {
    .login-card {
        padding: 40px 30px;
    }
    
    .title {
        font-size: 2rem;
    }
}
</style>
