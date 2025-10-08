<template>
    <div class="register-container">
        <transition name="scale-in">
            <div class="register-form" v-show="showForm">
                <img class="faramita-logo" src="@/assets/images/logo/FaramitaBBSLogo.png">
                <h1>注册账号</h1>
                <el-form ref="formRef" :model="registerForm" label-width="80px" class="el-form"
                status-icon  @keyup.enter="handleRegister" :rules="registerRules">
                    <div class="user-info-row">
                        <el-form-item class="el-form-item avatar-item" label="头像" prop="avatar">
                            <el-upload ref="uploadRef" class="avatar-uploader" :show-file-list="false" :limit="1" :on-exceed="handleExceed"
                            :http-request="handleAvatarUpload" :before-upload="beforeAvatarUpload">
                                <el-avatar :size="130" :src="avatarDisplayUrl"></el-avatar>
                            </el-upload>
                        </el-form-item>
                        <div class="user-info-fields">
                            <el-form-item class="el-form-item" label="昵称" prop="nickname">
                                <el-input v-model="registerForm.nickname" placeholder="这里输入昵称" :prefix-icon="Flag"/>
                            </el-form-item>
                            <el-form-item class="el-form-item" label="账号" prop="username">
                                <el-input v-model="registerForm.username" placeholder="这里输入账号" :prefix-icon="User" />
                            </el-form-item>
                        </div>
                    </div>
                    <el-form-item class="el-form-item" label="密码" prop="password">
                        <el-input type="password" show-password="true" v-model="registerForm.password" placeholder="这里输入密码" :prefix-icon="Lock"/>
                    </el-form-item>
                    <el-form-item class="el-form-item" label="确认密码" prop="password2">
                        <el-input type="password" show-password="true" v-model="registerForm.password2" placeholder="这里输入密码" :prefix-icon="Lock"/>
                    </el-form-item>
                    <el-form-item class="el-form-item" label="性别" prop="sex">
                        <el-radio-group v-model="registerForm.sex">
                            <el-radio value="1">男</el-radio>
                            <el-radio value="0">女</el-radio>
                            <el-radio value="2">神秘</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item class="el-form-item" label="种族" prop="race">
                        <el-input v-model="registerForm.race" placeholder="这里输入种族" :prefix-icon="Grid" />
                    </el-form-item>
                    <div class="register-btn-container">
                        <el-button @click="handleRegister">注册</el-button>
                    </div>
                </el-form>
            </div>
        </transition>
    </div>
</template>

<script setup lang="ts">
import defaultAvatar from '@/assets/images/default-avatar.png'
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, genFileId, type UploadInstance, type FormInstance, type FormRules, type UploadProps, type UploadRawFile, type UploadRequestOptions } from 'element-plus';
import { User, Lock, Flag, Grid} from '@element-plus/icons-vue'
import { uploadAvatar } from '@/api/file';
import { register } from '@/api/user';

const router = useRouter()
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const avatarDisplayUrl = ref(defaultAvatar)

// 动效变量
const showForm = ref(false)
onMounted(() => {
    setTimeout(() => {
        showForm.value = true
    }, 100)
})

const registerForm = reactive({
    nickname: '',
    username: '',
    password: '',
    password2: '',
    sex: 2,
    race: '',
    avatar: '',
})
// # 头像请求
// 头像上传前校验
const handleExceed: UploadProps['onExceed'] = (files) => {
    uploadRef.value!.clearFiles()
    const file = files[0] as UploadRawFile
    
    // 在手动触发上传前进行文件大小校验
    const isLt10M = file.size / 1024 /1024 < 10
    if (!isLt10M) {
        ElMessage.error('头像大小不能超过10MB')
        return
    }
    
    file.uid = genFileId()
    uploadRef.value!.handleStart(file)
    
    // 手动触发上传
    const options: UploadRequestOptions = {
        file: file,
        filename: file.name,
        data: {},
        action: '',
        method: 'POST',
        headers: {},
        withCredentials: false,
        onProgress: () => {},
        onSuccess: () => {},
        onError: () => {}
    };
    handleAvatarUpload(options)
}
const beforeAvatarUpload = (file: File) => {
    const isLt10M = file.size / 1024 /1024 < 10
    if (!isLt10M) {
        ElMessage.error('头像大小不能超过10MB')
    }
    return isLt10M
}
// 处理头像上传
const handleAvatarUpload = async (options: UploadRequestOptions) => {
    const { file } = options
    try {
        // 创建本地预览URL
        const localPreviewURL = URL.createObjectURL(file as File)
        avatarDisplayUrl.value = localPreviewURL;

        const response = await uploadAvatar(file as File)
        registerForm.avatar = response as unknown as string
        console.log('头像上传成功，registerForm.avatar:', registerForm.avatar) // 添加调试信息
        ElMessage.success('头像上传成功')
    } catch (error) {
        console.error('头像上传失败', error)
    }
}
// # 注册请求
// 表单校验规则
const registerRules = reactive<FormRules>({
    nickname: [
        {required: true, message: '请输入昵称', trigger: 'blur'},
        {min: 1, max: 20, message: '昵称长度应在1到20个字符之间', trigger: 'blur'},
    ],
    username: [
        { required: true, message: '请输入账号', trigger: 'blur'},
        { min: 6, max: 30, message: '账号长度应在6到30个字符之间', trigger: 'blur'},
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 30, message: '密码长度应在6到30个字符之间', trigger: 'blur' }
    ],
    password2: [
        { 
            required: true,
            validator: (_, value, callback) => {
                if (!value) {
                    callback(new Error('请再次输入密码'))
                } else if (value !== registerForm.password) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            }, 
            trigger: ['blur', 'change'] 
        }
    ],
    sex: [
        { required: true, message: '请选择性别', trigger: 'change' }
    ],
    race: [
        { required: true, message: '请输入种族', trigger: 'blur' },
        { min: 1, max: 20, message: '种族长度应在1到20个字符之间', trigger: 'blur' }
    ],
    avatar: [
        { required: true, message: '请上传头像', trigger: 'change' },
        {
            validator: (_, value, callback) => {
                if (!value || value === '') {
                    callback(new Error('请上传头像'))
                } else {
                    callback()
                }
            },
            trigger: 'change'
        }
    ]
})
const handleRegister =  async () =>  {
    if (!formRef.value) return

    try {
        // 表单验证
        await formRef.value.validate()
        // 头像验证
        if (!registerForm.avatar) {
            ElMessage.error('请上传头像')
            return
        }
        // 调用注册API
        const response = await register({
            nickname: registerForm.nickname,
            username: registerForm.username,
            password: registerForm.password,
            sex: registerForm.sex,
            race: registerForm.race,
            avatar: registerForm.avatar
        })
        console.log('>注册:', response)
        ElMessage.success('注册成功')

        router.push('/login')
    } catch (error) {
        console.log('注册失败', error)
    }
}


</script>

<style scoped>
.register-container {
    height: 100vh;
    background-image: url('@/assets/images/bg/bg01.png');
    background-size: 100%;
    background-position: center;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    overflow: hidden;
    transition: background-size 0.5s ease;
}

/* 当logo悬浮时，背景图放大 */
.register-container:has(.faramita-logo:hover) {
    background-size: 102%; /* 背景图稍微放大 */
}

/* 半透明遮罩 */
.register-container::before {
    content: '';
    position: absolute;
    background-color: rgba(0, 0, 0, 0.6);
    z-index: 0;
    width: 100%;
    height: 100%;
}

.register-form {
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
    color: aqua;
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
    white-space: nowrap;
}

/* 修改输入框样式 */
:deep(.el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    box-shadow: none;
    transition: all 0.3s ease;
    font-size: 15px;
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
    color: rgba(255, 255, 255, 0.73);
}

/* 修改图标样式 */
:deep(.el-input__prefix-inner) {
    color: rgba(255, 255, 255, 0.8);
}
/* 修改标签项 */

/* 修改图标样式 */
:deep(.el-input__prefix-inner) {
    color: rgba(255, 255, 255, 0.8);
}

/* 修改性别选择器样式 - 简洁紫色系 */
:deep(.el-radio-group) {
    display: flex;
    width: 100%;
    gap: 10px;
}

:deep(.el-radio) {
    margin-right: 0;
    padding: 6px 12px;
    border-radius: 6px;
    transition: all 0.2s ease;
    border-bottom: 3px solid #ccc;
}

:deep(.el-radio__label) {
    color: #739efb;
    font-size: 18px;
    font-weight: 500;
    padding-left: 6px;
}

:deep(.el-radio:hover .el-radio__label) {
    color: #f748d1;
}

:deep(.el-radio.is-checked .el-radio__label) {
    color: #fd52e7;
}

:deep(.el-radio__inner) {
    background-color: rgba(138, 43, 226, 0.1);
    border-color: rgba(138, 43, 226, 0.5);
    transition: all 0.2s ease;
}

:deep(.el-radio.is-checked .el-radio__inner) {
    background-color: #8a2be2;
    border-color: #8a2be2;
}

:deep(.el-radio__inner:hover) {
    border-color: #9370db;
}



/* 修改按钮样式 */
.el-button {
    background: linear-gradient(90deg, #1036f255, #098af3e3);
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
    /* margin-left: 10px; 向左移动按钮 */
}

.el-button:hover {
    background-position: 100% 0; /* 悬浮时渐变色两端互换 */
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(61, 90, 255, 0.818);
}

.el-button:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(31, 97, 162, 0.3);
}

.user-info-row {
    display: flex;
    width: 80%;
    margin-bottom: 20px;
    align-items: flex-start;
}
.avatar-item {
    flex: 0 0 auto;
    margin-right: 20px;
    margin-bottom: 0;
    width: auto;
}
.avatar-uploader {
    display: flex;
    justify-content: center;
    align-items: center;
}
.user-info-fields {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin-top: 0;
    margin-top: 2%;
}
.user-info-fields .el-form-item {
    width: 100%;
}
.register-btn-container {
    display: flex;
    width: 100%;
    justify-content: center;
    /* background-color: #373336; */
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