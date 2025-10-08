<template>
    <div class="login-container">
        <transition name="scale-in">
            <div class="login-form" v-if="showForm">
                <img class="faramita-logo" src="@/assets/images/logo/FaramitaBBSLogo.png">
                <h1>登录账号</h1>
                <el-form ref="formRef" :model="loginForm" label-width="80px" class="el-form"
                status-icon :rules="rules" @keyup.enter="handleLogin">
                    <el-form-item class="el-form-item" label="账号" prop="username">
                        <el-input v-model="loginForm.username" placeholder="这里输入账号" :prefix-icon="User" />
                    </el-form-item>
                    <el-form-item class="el-form-item" label="密码" prop="password">
                        <el-input type="password" show-password="true" v-model="loginForm.password" placeholder="这里输入密码" :prefix-icon="Lock"/>
                    </el-form-item>
                    <el-form-item>
                        <img src="@/assets/images/icons/ciallo.png" alt="ciallo" @click="handleImageClick">
                        <el-button style="align-self: center;" @click="handleLogin">登录</el-button>
                        <router-link class="to-register" to="/register">没有账号？点击注册</router-link>
                    </el-form-item>
                </el-form>
            </div>
        </transition>
    </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { login } from '@/api/user';
import { useUserStore } from '@/stores/user';
import type { UserInfo } from '@/types';
import { User, Lock} from '@element-plus/icons-vue'
import cialloAudioUrl from '@/assets/musics/ciallo.mp3'
const router = useRouter()
const store = useUserStore()
const formRef = ref<FormInstance>()
const audio = ref<HTMLAudioElement | null>(null)

const loginForm = reactive({
    username: '',
    password: '',
})

// 动画变量
const showForm = ref(false)
onMounted(() => {
    setTimeout(() => {
        showForm.value = true
    }, 100)
})
// 音频播放方法
const playAudio = () => {
    if (audio.value) {
        audio.value.play().catch(err => {
            console.error("播放失败", err)
        })
    }
}
onMounted(() => {
    audio.value = new Audio(cialloAudioUrl)
})
onUnmounted(() => {
    if (audio.value) {
        audio.value.pause()
        audio.value.src = ''
        audio.value = null
    }
})
// 图片点击效果
const handleImageClick = (event: Event) => {
    const img = event.target as HTMLImageElement
    // 振动属性
    // 确保图片有alt属性
    if (img.getAttribute('alt') === 'ciallo') {
        img.classList.add('shaking')
        
        // 1秒后移除振动类
        setTimeout(() => {
            img.classList.remove('shaking')
        }, 1000)
    }
    // 播放音频
    playAudio()
    // 弹出Ciallo
    ElMessage.success({
        message: 'Ciallo～(∠・ω- )⌒☆-',
        duration: 2 * 1000
    })
}

// 登录处理
const handleLogin = async () => {
    if (!formRef.value) return

    try {
        // 表单验证，强制触发所有字段的验证
        const valid = await formRef.value.validate()
        if (!valid) {
            ElMessage.error('>请正确填写登录信息<')
            return
        }

        // 检查表单数据是否为空
        if (!loginForm.username || !loginForm.password) {
            ElMessage.error('>账号和密码不能为空<')
            return
        }

        // 检查账号和密码长度
        if (loginForm.username.length < 6 || loginForm.username.length > 30) {
            ElMessage.error('>账号长度必须在6-30位之间<')
            return
        }

        if (loginForm.password.length < 6 || loginForm.password.length > 30) {
            ElMessage.error('>密码长度必须在6-30位之间<')
            return
        }

        // 发起登录请求
        console.log('>发起登录请求:', loginForm)
        const response:UserInfo = await login(loginForm)

        // 登录成功，保存token和用户信息
        store.setUserInfo(response)

        ElMessage.success('>登录成功<')
        router.push('/')
    } catch (error: any) {
        console.error('>登录失败',error)
    }
}
// 异常处理
// 表单验证规则
const rules = reactive<FormRules>({
    username: [
        { required: true, message: '请输入账号', trigger: 'blur' },
        { required: true, message: '请输入账号', trigger: 'change' },
        { min: 6, max: 30, message: '账号长度必须在6-30位之间', trigger: 'blur' },
        { min: 6, max: 30, message: '账号长度必须在6-30位之间', trigger: 'change' },
        { 
            pattern: /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]{6,30}$/, 
            message: '账号只能包含字母、数字和特殊字符', 
            trigger: 'blur' 
        },
        { 
            pattern: /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]{6,30}$/, 
            message: '账号只能包含字母、数字和特殊字符', 
            trigger: 'change' 
        }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { required: true, message: '请输入密码', trigger: 'change' },
        { min: 6, max: 30, message: '密码长度必须在6-30位之间', trigger: 'blur' },
        { min: 6, max: 30, message: '密码长度必须在6-30位之间', trigger: 'change' },
        { 
            pattern: /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]{6,30}$/, 
            message: '密码只能包含字母、数字和特殊字符', 
            trigger: 'blur' 
        },
        { 
            pattern: /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]{6,30}$/, 
            message: '密码只能包含字母、数字和特殊字符', 
            trigger: 'change' 
        }
    ]
});
</script>

<style scoped>
.login-container {
    height: 100vh;
    background-image: url('@/assets/images/bg/bg01.png');
    background-size: 100%;
    background-position: center;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    overflow: hidden;
    transition: background-size 0.3s ease;
}
.login-container:has(.faramita-logo:hover) {
    background-size: 102%;
}
/* 半透明遮罩 */
.login-container::before {
    content: '';
    position: absolute;
    background-color: rgba(0, 0, 0, 0.6);
    z-index: 0;
    width: 100%;
    height: 100%;
}

.login-form {
    background-color: rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(5px);
    -webkit-backdrop-filter: blur(10px);
    z-index: 10;
    width: 80vh;
    padding: 30px;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    align-items: center;
    border: 1px solid rgb(85, 118, 123);
}

.faramita-logo {
    margin-bottom: 20px;
    width: 60%;
    height: auto;
    transition: transform 0.3s ease;
}

.faramita-logo:hover {
    transform: scale(1.05);
}

h1 {
    color: rgb(251, 119, 221);
    margin: 0 0 30px 0;
    font-size: 28px;
    font-weight: 600;
    text-align: center;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.el-form {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.el-form-item {
    margin-bottom: 20px;
    width: 80%;
}

/* 修改表单项标签样式 */
:deep(.el-form-item__label) {
    color: #ffffff;
    font-size: 18px;
    font-weight: 500;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 修改输入框样式 */
:deep(.el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    box-shadow: none;
    transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.4);
    background-color: rgba(255, 255, 255, 0.15);
}

:deep(.el-input__wrapper.is-focus) {
    border-color: #409eff;
    background-color: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

:deep(.el-input__inner) {
    color: #ffffff;
    height: 40px;
}

:deep(.el-input__inner::placeholder) {
    color: rgba(255, 255, 255, 0.6);
}

/* 修改图标样式 */
:deep(.el-input__prefix-inner) {
    color: rgba(255, 255, 255, 0.8);
}

/* 修改按钮样式 */
.el-button {
    background: linear-gradient(90deg, #373336, #af5de2);
    background-size: 200% 100%;
    border: none;
    color: white;
    font-weight: 500;
    padding: 12px 30px;
    border-radius: 8px;
    font-size: 20px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    margin-top: 10px;
    margin-left: 10px; /* 向左移动按钮 */
}

.el-button:hover {
    background-position: 100% 0; /* 悬浮时渐变色两端互换 */
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(255, 64, 217, 0.4);
}

.el-button:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(31, 97, 162, 0.3);
}

/* 图片样式 */
img[alt="ciallo"] {
    margin-bottom: 20px;
    margin-left: -20px; /* 向左移动图片 */
    border-radius: 8px;
    transition: transform 0.3s ease;
    cursor: pointer; /* 添加指针样式，表示可点击 */
    width: 50%;
}
img[alt="ciallo"]:hover {
    transform: scale(1.05);
}

/* 图片点击振动效果 */
@keyframes shake {
    0%, 100% { transform: translateX(0) translateY(0); }
    25% { transform: translateX(0) translateY(-5px); }
    75% { transform: translateX(0) translateY(5px); }
}

img[alt="ciallo"].shaking {
    animation: shake 1s ease-in-out;
}

.to-register {
    color: rgb(85, 164, 255);
    margin-bottom: auto;
    margin-left: auto;
}

/* 表单缩放动画 */
.scale-in-enter-active,
.scale-in-leave-active {
    transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.scale-in-enter-from,
.scale-in-leave-to {
    opacity: 0;
    transform: scale(0.7);
}

.scale-in-enter-to,
.scale-in-leave-from {
    opacity: 1;
    transform: scale(1);
}

</style>