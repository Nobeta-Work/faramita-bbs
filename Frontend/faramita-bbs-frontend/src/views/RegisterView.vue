<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref, h } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage, type FormInst, type FormRules, NForm, NFormItem, NInput, NIcon, NAvatar, NRadioGroup, NRadio } from 'naive-ui';
import { PersonOutline, LockClosedOutline, FlagOutline, GridOutline, CameraOutline, CloseOutline, Person, ArrowForwardOutline } from '@vicons/ionicons5';
import { uploadAvatar } from '@/api/file';
import { register } from '@/api/user';
import FaramitaLogo from '@/assets/images/logo/FaramitaBBSLogo.png';

// Fonts
const fontLink = document.createElement('link')
fontLink.href = 'https://fonts.googleapis.com/css2?family=Lato:wght@300;400;700&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap'
fontLink.rel = 'stylesheet'
document.head.appendChild(fontLink)

const router = useRouter()
const message = useMessage()
const formRef = ref<FormInst | null>(null)
const avatarDisplayUrl = ref('')
const loading = ref(false)

const renderDefaultAvatar = () => h(NIcon, null, { default: () => h(Person) })

const registerForm = reactive({
    nickname: '',
    username: '',
    password: '',
    password2: '',
    sex: 2,
    race: '',
    avatar: ''
})

// Avatar Crop Logic (Native Implementation from UserProfile.vue)
const showModal = ref(false);
const tempAvatarUrl = ref('');
const croppingAvatar = ref(false);
const nativeCropImgRef = ref<HTMLImageElement | null>(null);
const cropContainerRef = ref<HTMLDivElement | null>(null);

// Native Crop State
const scale = ref(1);
const offset = ref({ x: 0, y: 0 });
const isDragging = ref(false);
const lastMousePos = ref({ x: 0, y: 0 });

const rules: FormRules = {
    nickname: { required: true, message: 'Nickname is required', trigger: 'blur' },
    username: { required: true, message: 'Username is required', trigger: 'blur' },
    password: { required: true, message: 'Password is required', trigger: 'blur' },
    password2: {
        required: true,
        validator: (_, value) => {
            if (!value) return new Error('Please confirm your password')
            if (value !== registerForm.password) return new Error('Passwords do not match')
            return true
        },
        trigger: 'blur'
    }
};

const showForm = ref(false);

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
});

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId)
  window.removeEventListener('mousemove', handleMouseMove)
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
})

const handleFileChange = (e: Event) => {
    const input = e.target as HTMLInputElement;
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const reader = new FileReader();
        reader.onload = (e) => {
            tempAvatarUrl.value = e.target?.result as string;
            // Reset crop state
            scale.value = 1;
            offset.value = { x: 0, y: 0 };
            showModal.value = true;
            // Reset file input
            input.value = '';
        };
        reader.readAsDataURL(file);
    }
};

const handleCropMouseDown = (e: MouseEvent) => {
    isDragging.value = true;
    lastMousePos.value = { x: e.clientX, y: e.clientY };
    e.preventDefault();
};

const handleCropMouseMove = (e: MouseEvent) => {
    if (!isDragging.value) return;
    const dx = e.clientX - lastMousePos.value.x;
    const dy = e.clientY - lastMousePos.value.y;
    offset.value.x += dx;
    offset.value.y += dy;
    lastMousePos.value = { x: e.clientX, y: e.clientY };
};

const handleCropMouseUp = () => {
    isDragging.value = false;
};

const handleCropWheel = (e: WheelEvent) => {
    const zoomSpeed = 0.001;
    const delta = -e.deltaY;
    const newScale = Math.max(0.1, Math.min(5, scale.value + delta * zoomSpeed));
    scale.value = newScale;
    e.preventDefault();
};

const handleCropAndUpload = async () => {
    if (!nativeCropImgRef.value || !cropContainerRef.value) return;

    croppingAvatar.value = true;
    try {
        const canvas = document.createElement('canvas');
        canvas.width = 400;
        canvas.height = 400;
        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        const img = nativeCropImgRef.value;
        const container = cropContainerRef.value;

        // 背景填黑
        ctx.fillStyle = '#000';
        ctx.fillRect(0, 0, 400, 400);

        // 计算比例：Canvas(400) �?容器物理尺寸 的比�?        
        const containerSize = container.offsetWidth;
        const drawScale = 400 / containerSize;

        // 计算图片在容器中的实际渲染尺�?        
        const renderWidth = img.offsetWidth * scale.value;
        const renderHeight = img.offsetHeight * scale.value;

        // 映射�?Canvas 上的尺寸
        const canvasDrawWidth = renderWidth * drawScale;
        const canvasDrawHeight = renderHeight * drawScale;

        // 映射�?Canvas 上的偏移（以中心为原点）
        const canvasOffsetX = offset.value.x * drawScale;
        const canvasOffsetY = offset.value.y * drawScale;

        // 绘制图片：Canvas 中心点为 (200, 200)
        ctx.drawImage(
            img,
            200 + canvasOffsetX - canvasDrawWidth / 2,
            200 + canvasOffsetY - canvasDrawHeight / 2,
            canvasDrawWidth,
            canvasDrawHeight
        );

        const blob = await new Promise<Blob | null>(resolve => canvas.toBlob(resolve, 'image/png'));

        if (blob) {
            const croppedFile = new File([blob], 'avatar.png', { type: 'image/png' });
            const res: any = await uploadAvatar(croppedFile);
            registerForm.avatar = res as string;
            avatarDisplayUrl.value = URL.createObjectURL(croppedFile);
            message.success('Avatar uploaded successfully');
            showModal.value = false;
        }
    } catch (error) {
        message.error('Upload failed');
    } finally {
        croppingAvatar.value = false;
    }
};

const handleRegister = () => {
    formRef.value?.validate(async (errors) => {
        if (!errors) {
            loading.value = true;
            try {
                await register({
                    nickname: registerForm.nickname,
                    username: registerForm.username,
                    password: registerForm.password,
                    sex: registerForm.sex,
                    race: registerForm.race,
                    avatar: registerForm.avatar
                });
                message.success('Registration successful, please sign in');
                router.push('/login');
            } catch (error: any) {
                message.error(error.message || 'Registration failed');
            } finally {
                loading.value = false;
            }
        }
    });
};
</script>

<template>
    <div class="register-page" :class="{ 'loaded': showForm }">
        <canvas ref="canvasRef" class="bg-canvas"></canvas>

        <div class="register-container animate-section" v-if="showForm">
            <div class="register-card animate-scale-in">
                <div class="header-section">
                    <img :src="FaramitaLogo" alt="Faramita BBS Logo" class="logo-image" />
                    <h1 class="title">JOIN US</h1>
                    <div class="decorative-line"></div>
                    <p class="subtitle">Create your persona to enter Faramita BBS.</p>
                </div>
                
                <n-form ref="formRef" :model="registerForm" :rules="rules" class="register-form" @keyup.enter="handleRegister">
                    <div class="form-content">
                        <!-- Avatar Section -->
                        <div class="avatar-section">
                            <label class="avatar-wrapper">
                                <input type="file" accept="image/*" style="display: none" @change="handleFileChange" />
                                <n-avatar
                                    :size="120"
                                    :src="avatarDisplayUrl"
                                    :render-icon="renderDefaultAvatar"
                                    class="avatar-img"
                                />
                                <div class="avatar-overlay">
                                    <n-icon size="24" color="white"><CameraOutline /></n-icon>
                                </div>
                            </label>
                            <span class="avatar-hint">UPLOAD AVATAR</span>
                        </div>

                        <!-- Fields Grid -->
                        <div class="fields-grid">
                            <n-form-item label="NICKNAME" path="nickname">
                                <n-input v-model:value="registerForm.nickname" placeholder="Your persona name" class="custom-input">
                                    <template #prefix><n-icon :component="PersonOutline" /></template>
                                </n-input>
                            </n-form-item>
                            
                            <n-form-item label="USERNAME" path="username">
                                <n-input v-model:value="registerForm.username" placeholder="Your login ID" class="custom-input">
                                    <template #prefix><n-icon :component="GridOutline" /></template>
                                </n-input>
                            </n-form-item>
                            
                            <n-form-item label="PASSWORD" path="password">
                                <n-input type="password" show-password-on="click" v-model:value="registerForm.password" placeholder="Create password" class="custom-input">
                                    <template #prefix><n-icon :component="LockClosedOutline" /></template>
                                </n-input>
                            </n-form-item>
                            
                            <n-form-item label="CONFIRM PASSWORD" path="password2">
                                <n-input type="password" show-password-on="click" v-model:value="registerForm.password2" placeholder="Confirm password" class="custom-input">
                                    <template #prefix><n-icon :component="LockClosedOutline" /></template>
                                </n-input>
                            </n-form-item>
                            
                            <n-form-item label="IDENTITY" path="sex" class="full-width">
                                <n-radio-group v-model:value="registerForm.sex" name="sex" class="custom-radio-group">
                                    <n-radio :value="1">MALE</n-radio>
                                    <n-radio :value="0">FEMALE</n-radio>
                                    <n-radio :value="2">MYSTERY</n-radio>
                                </n-radio-group>
                            </n-form-item>
                            
                            <n-form-item label="RACE" path="race" class="full-width">
                                <n-input v-model:value="registerForm.race" placeholder="e.g. Human, Elf, Cyborg..." class="custom-input">
                                    <template #prefix><n-icon :component="FlagOutline" /></template>
                                </n-input>
                            </n-form-item>
                        </div>
                    </div>
                    
                    <div class="form-actions">
                        <button class="submit-btn" @click.prevent="handleRegister" :disabled="loading">
                            <span>{{ loading ? 'CREATING...' : 'CREATE ACCOUNT' }}</span>
                            <n-icon :component="ArrowForwardOutline" />
                        </button>
                        
                        <div class="divider-wrapper">
                            <span class="divider-line"></span>
                            <span class="divider-text">OR</span>
                            <span class="divider-line"></span>
                        </div>
                        
                        <div class="secondary-actions">
                            <button class="text-btn" @click.prevent="router.push('/login')">ALREADY HAVE AN ACCOUNT?</button>
                            <span class="dot-separator">?</span>
                            <button class="text-btn" @click.prevent="router.push('/')">RETURN HOME</button>
                        </div>
                    </div>
                </n-form>
            </div>
        </div>

        <!-- Avatar Crop Modal -->
        <div v-if="showModal" class="modal-overlay">
            <div class="crop-modal-content">
                <div class="modal-header">
                    <h3>CROP AVATAR</h3>
                    <button class="close-btn" @click="showModal = false">
                        <n-icon size="24"><CloseOutline /></n-icon>
                    </button>
                </div>
                <div class="modal-body">
                    <div 
                        class="native-cropper-wrapper" 
                        ref="cropContainerRef"
                        @mousedown="handleCropMouseDown"
                        @mousemove="handleCropMouseMove"
                        @mouseup="handleCropMouseUp"
                        @mouseleave="handleCropMouseUp"
                        @wheel="handleCropWheel"
                    >
                        <img 
                            ref="nativeCropImgRef"
                            :src="tempAvatarUrl" 
                            class="native-crop-image"
                            :style="{
                                transform: `translate(${offset.x}px, ${offset.y}px) scale(${scale})`
                            }"
                        />
                        <div class="crop-overlay">
                            <div class="crop-viewport"></div>
                        </div>
                    </div>
                </div>
                <div class="modal-actions">
                    <button class="save-btn" @click="handleCropAndUpload" :disabled="croppingAvatar">
                        {{ croppingAvatar ? 'UPLOADING...' : 'CONFIRM UPLOAD' }}
                    </button>
                </div>
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

.register-page {
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
    padding: 40px 0;
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

.register-page.loaded .bg-canvas {
    opacity: 1;
}

.register-container {
    position: relative;
    z-index: 10;
    width: 100%;
    max-width: 800px;
    padding: 0 20px;
}

.register-card {
    background: var(--modal-bg);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    padding: 50px;
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
    max-width: 250px;
    height: auto;
    margin: 0 auto 20px;
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

.form-content {
    display: grid;
    grid-template-columns: 180px 1fr;
    gap: 40px;
    align-items: start;
}

.avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
}

.avatar-wrapper {
    position: relative;
    cursor: pointer;
    border-radius: 0;
    overflow: hidden;
    display: block;
    box-shadow: 10px 10px 0px var(--line-color);
    transition: all 0.3s ease;
    background: var(--bg-primary);
}

.avatar-wrapper:hover {
    transform: translate(-4px, -4px);
    box-shadow: 14px 14px 0px var(--accent-color);
}

.avatar-img {
    border-radius: 0;
    --n-border-radius: 0;
    display: block;
}

.avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    opacity: 0;
    transition: opacity 0.3s;
    backdrop-filter: blur(2px);
}

.avatar-wrapper:hover .avatar-overlay {
    opacity: 1;
}

.avatar-hint {
    font-size: 0.75rem;
    letter-spacing: 2px;
    color: var(--text-tertiary);
    font-family: 'Lato', sans-serif;
}

.fields-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0 20px;
}

.full-width {
    grid-column: 1 / -1;
}

.register-form :deep(.n-form-item-label) {
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

.custom-radio-group :deep(.n-radio__label) {
    font-family: 'Lato', sans-serif;
    font-size: 0.85rem;
    letter-spacing: 1px;
}

.form-actions {
    margin-top: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.submit-btn {
    width: 100%;
    max-width: 400px;
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
    max-width: 400px;
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

/* Modal Styles */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.5);
    backdrop-filter: blur(5px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.crop-modal-content {
    background: var(--modal-bg);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    padding: 24px;
    width: 90vw;
    max-width: 600px;
    height: 70vh;
    max-height: 600px;
    display: flex;
    flex-direction: column;
    border: 1px solid var(--line-color);
    box-shadow: 0 25px 50px rgba(0,0,0,0.2);
    border-radius: 0;
    box-sizing: border-box;
    overflow: hidden;
}

.modal-header {
    flex-shrink: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--line-color);
}

.modal-header h3 {
    font-family: 'Playfair Display', serif;
    margin: 0;
    font-size: 1.5rem;
    letter-spacing: 2px;
    color: var(--text-primary);
}

.close-btn {
    background: none;
    border: none;
    cursor: pointer;
    color: var(--text-primary);
    transition: color 0.3s;
}

.close-btn:hover {
    color: var(--accent-color);
}

.modal-body {
    flex: 1;
    min-height: 0;
    position: relative;
    margin: 16px 0;
    background: #111;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
}

.native-cropper-wrapper {
    width: 100%;
    max-width: 360px;
    aspect-ratio: 1 / 1;
    position: relative;
    cursor: move;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    box-shadow: 0 0 20px rgba(0,0,0,0.5);
    border: 1px solid rgba(255,255,255,0.1);
}

.native-crop-image {
    max-width: none;
    max-height: none;
    user-select: none;
    pointer-events: none;
    transition: transform 0.05s linear;
}

.crop-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
    background: rgba(0, 0, 0, 0.2);
}

.crop-viewport {
    width: 100%;
    height: 100%;
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-sizing: border-box;
    position: relative;
}

.crop-viewport::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    box-shadow: 0 0 0 1000px rgba(0, 0, 0, 0.3);
}

.modal-actions {
    flex-shrink: 0;
    padding-top: 16px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid var(--line-color);
}

.save-btn {
    background: var(--text-primary);
    color: var(--bg-primary);
    border: none;
    padding: 12px 30px;
    font-family: 'Lato', sans-serif;
    font-weight: 700;
    letter-spacing: 2px;
    cursor: pointer;
    transition: all 0.3s;
}

.save-btn:hover:not(:disabled) {
    background: var(--accent-color);
    transform: translateY(-2px);
    box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

.save-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

@media (max-width: 768px) {
    .form-content {
        grid-template-columns: 1fr;
        gap: 30px;
    }
    
    .fields-grid {
        grid-template-columns: 1fr;
    }
    
    .register-card {
        padding: 40px 30px;
    }
}
</style>