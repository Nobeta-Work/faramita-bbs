<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, h } from 'vue'
import { useRoute } from 'vue-router'
import {
  NUpload, NIcon, useMessage, NSpin, NModal, NForm, NFormItem, NInput, NRadioGroup, NRadio, NAvatar
} from 'naive-ui'
import {
  ArrowForwardOutline, LogoGithub, LogoTwitter, PencilOutline, CloseOutline, CameraOutline, Person
} from '@vicons/ionicons5'
import { getProfileByUid, updateAvatar, updateProfile } from '@/api/user'
import { downloadAvatar } from '@/api/file'
import { useUserStore } from '@/stores/user'
import type { User, Blog } from '@/types'
import { DateUtils } from '@/types/date'
import { BlogUtils } from '@/types/blog'
import router from '@/router'

// Fonts
const fontLink = document.createElement('link')
fontLink.href = 'https://fonts.googleapis.com/css2?family=Lato:wght@300;400;700&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap'
fontLink.rel = 'stylesheet'
document.head.appendChild(fontLink)

const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

const uid = computed(() => Number(route.params.uid))
const isCurrentUser = computed(() => userStore.userInfo?.id === uid.value)

const user = ref<User | null>(null)
const userAvatarUrl = ref<string | undefined>(undefined)
const blogList = ref<Blog[]>([])

const renderDefaultAvatar = () => h(NIcon, null, { default: () => h(Person) })
const loading = ref(false)
const pageLoaded = ref(false)

// Edit Profile Logic
const showEditModal = ref(false)
const editForm = ref({
  id: 0, nickname: '', avatar: '', sex: 0, race: '', signature: '', password: '', passwordConfirm: ''
})
const saving = ref(false)

// Avatar Crop Logic (Native Implementation)
const showCropModal = ref(false)
const tempAvatarUrl = ref('')
const croppingAvatar = ref(false)
const nativeCropImgRef = ref<HTMLImageElement | null>(null)
const cropContainerRef = ref<HTMLDivElement | null>(null)

// Native Crop State
const scale = ref(1)
const offset = ref({ x: 0, y: 0 })
const isDragging = ref(false)
const lastMousePos = ref({ x: 0, y: 0 })

const handleAvatarChange = (options: any) => {
  const file = options.file?.file
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      tempAvatarUrl.value = e.target?.result as string
      // Reset crop state
      scale.value = 1
      offset.value = { x: 0, y: 0 }
      showCropModal.value = true
    }
    reader.readAsDataURL(file)
  }
}

const handleCropMouseDown = (e: MouseEvent) => {
  isDragging.value = true
  lastMousePos.value = { x: e.clientX, y: e.clientY }
  e.preventDefault()
}

const handleCropMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return
  const dx = e.clientX - lastMousePos.value.x
  const dy = e.clientY - lastMousePos.value.y
  offset.value.x += dx
  offset.value.y += dy
  lastMousePos.value = { x: e.clientX, y: e.clientY }
}

const handleCropMouseUp = () => {
  isDragging.value = false
}

const handleCropWheel = (e: WheelEvent) => {
  const zoomSpeed = 0.001
  const delta = -e.deltaY
  const newScale = Math.max(0.1, Math.min(5, scale.value + delta * zoomSpeed))
  scale.value = newScale
  e.preventDefault()
}

const confirmCrop = async () => {
  if (!nativeCropImgRef.value || !cropContainerRef.value) return
  
  croppingAvatar.value = true
  try {
    const canvas = document.createElement('canvas')
    canvas.width = 400
    canvas.height = 400
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const img = nativeCropImgRef.value
    const container = cropContainerRef.value
    
    // 背景填黑
    ctx.fillStyle = '#000'
    ctx.fillRect(0, 0, 400, 400)
    
    // 计算比例：Canvas(400) 与 容器物理尺寸 的比例
    const containerSize = container.offsetWidth
    const drawScale = 400 / containerSize
    
    // 计算图片在容器中的实际渲染尺寸
    const renderWidth = img.offsetWidth * scale.value
    const renderHeight = img.offsetHeight * scale.value
    
    // 映射到 Canvas 上的尺寸
    const canvasDrawWidth = renderWidth * drawScale
    const canvasDrawHeight = renderHeight * drawScale
    
    // 映射到 Canvas 上的偏移（以中心为原点）
    const canvasOffsetX = offset.value.x * drawScale
    const canvasOffsetY = offset.value.y * drawScale
    
    // 绘制图片：Canvas 中心点为 (200, 200)
    ctx.drawImage(
      img,
      200 + canvasOffsetX - canvasDrawWidth / 2,
      200 + canvasOffsetY - canvasDrawHeight / 2,
      canvasDrawWidth,
      canvasDrawHeight
    )
    
    const blob = await new Promise<Blob | null>(resolve => canvas.toBlob(resolve, 'image/png'))
    
    if (blob) {
      const file = new File([blob], 'avatar.png', { type: 'image/png' })
      await updateAvatar(uid.value, file)
      message.success('头像更新成功')
      showCropModal.value = false
      fetchProfile()
    }
  } catch (error) {
    message.error('上传失败')
  } finally {
    croppingAvatar.value = false
  }
}

const openEditModal = () => {
  if (!user.value) return
  editForm.value = { ...user.value, password: '', passwordConfirm: '' }
  showEditModal.value = true
}

const handleSaveProfile = async () => {
  if (!editForm.value.nickname) {
    message.warning('Nickname is required')
    return
  }
  saving.value = true
  try {
    const { id, nickname, sex, race, signature } = editForm.value
    // Assuming updateProfile takes User object.
    // If backend requires partial update, we send what we have.
    // Construct a user object to update
    const userToUpdate = { ...user.value, nickname, sex, race, signature } as User

    await updateProfile(userToUpdate.id, userToUpdate)
    message.success('Profile Updated')
    showEditModal.value = false
    fetchProfile()

    // Update store if current user
    if (userStore.userInfo && userStore.userInfo.id === id) {
       userStore.userInfo = { ...userStore.userInfo, nickname }
    }
  } catch (error) {
    message.error('Failed to update profile')
  } finally {
    saving.value = false
  }
}


// Canvas Refs
const canvasRef = ref<HTMLCanvasElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null
let animationFrameId: number
let mouseX = -1000
let mouseY = -1000

// Fetch Data
const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await getProfileByUid(uid.value)
    user.value = res.user
    blogList.value = res.blogList
    document.title = `${res.user.nickname} - Estetica`

    if (res.user.avatar) {
        try {
            const blob = await downloadAvatar(res.user.avatar)
            userAvatarUrl.value = URL.createObjectURL(blob)
        } catch (e) {
            console.error(e)
            userAvatarUrl.value = undefined
        }
    } else {
        userAvatarUrl.value = undefined
    }
  } catch (error) {
    message.error('无法加载用户信息')
  } finally {
    loading.value = false
    // Trigger page animation after loading
    setTimeout(() => {
      pageLoaded.value = true
    }, 100)
  }
}

const sexText = (sex: number) => {
  switch(sex) {
    case 1: return 'MALE'
    case 0: return 'FEMALE'
    default: return 'MYSTERY'
  }
}

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
  fetchProfile()
  resizeHandler = initCanvas()
  window.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  cancelAnimationFrame(animationFrameId)
  window.removeEventListener('mousemove', handleMouseMove)
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
})

// Canvas loop handles theme change naturally by checking document.documentElement class

</script>

<template>
  <div class="profile-page" :class="{ 'loaded': pageLoaded }">
    <canvas ref="canvasRef" class="bg-canvas"></canvas>

    <n-spin :show="loading">
      <div class="container" v-if="user">

        <!-- Header Section: Asymmetric Grid -->
        <header class="header-grid animate-section">
          <div class="info-col">
            <div class="meta-line animate-fade-in" style="animation-delay: 0.1s">
              <span class="uid">UID — {{ user.id.toString().padStart(6, '0') }}</span>
              <span class="date">JOINED {{ DateUtils.isoToDateOnly(user.createTime) }}</span>
              <!-- Edit Button (Only for Owner) -->
              <button v-if="isCurrentUser" class="edit-btn" @click="openEditModal">
                <n-icon size="14"><PencilOutline /></n-icon> EDIT
              </button>
            </div>

            <h1 class="nickname animate-slide-in" style="animation-delay: 0.2s">{{ user.nickname }}</h1>

            <div class="bio-block animate-fade-in" style="animation-delay: 0.3s">
              <p class="signature">{{ user.signature || 'No signature provided yet.' }}</p>
            </div>

            <div class="tags-line animate-fade-in" style="animation-delay: 0.4s">
              <span class="tag">{{ user.race || 'HUMAN' }}</span>
              <span class="divider">/</span>
              <span class="tag">{{ sexText(user.sex) }}</span>
            </div>

            <div class="social-links" v-if="false">
              <!-- Placeholder for social links if needed -->
              <n-icon size="20"><LogoGithub /></n-icon>
              <n-icon size="20"><LogoTwitter /></n-icon>
            </div>
          </div>

          <div class="avatar-col animate-scale-in" style="animation-delay: 0.3s">
            <div class="avatar-wrapper">
               <n-upload
                  v-if="isCurrentUser"
                  class="avatar-uploader"
                  :show-file-list="false"
                  :custom-request="handleAvatarChange"
                  accept="image/*"
                  trigger-style="height: 100%; width: 100%;"
                >
                  <div class="avatar-container-inner">
                    <n-avatar
                      :src="userAvatarUrl"
                      :render-icon="renderDefaultAvatar"
                      class="avatar-img"
                    />
                    <div class="change-btn">
                      <n-icon size="18"><CameraOutline /></n-icon>
                      <span>CHANGE</span>
                    </div>
                  </div>
                </n-upload>
                <!-- Visitor View -->
                <n-avatar
                  v-else
                  :src="userAvatarUrl"
                  :render-icon="renderDefaultAvatar"
                  class="avatar-img"
                />
            </div>
          </div>
        </header>

        <div class="divider-line animate-expand" style="animation-delay: 0.5s"></div>

        <!-- Content Section -->
        <section class="content-section">
          <div class="section-head animate-fade-in" style="animation-delay: 0.6s">
            <h2>Publications</h2>
            <span class="count">({{ blogList.length }})</span>
          </div>

          <div class="blog-grid">
            <div
              v-for="(blog, index) in blogList"
              :key="blog.bloguid"
              class="blog-card animate-card-in"
              :style="{ animationDelay: `${0.7 + index * 0.1}s` }"
              @click="router.push(`/blog/${blog.bloguid}`)"
            >
              <div class="card-meta">
                <span class="cat-tag">{{ BlogUtils.bigIdToString(blog.bigCategoryId) }}</span>
                <span class="time">{{ DateUtils.isoToDateOnly(blog.createTime) }}</span>
              </div>
              <h3 class="card-title">{{ blog.title }}</h3>
              <p class="card-summary">{{ blog.summary }}</p>
              <div class="card-footer">
                <span class="read-more">READ ENTRY</span>
                <n-icon><ArrowForwardOutline /></n-icon>
              </div>
            </div>

            <div v-if="blogList.length === 0" class="empty-state">
              <span class="void-text">VOID</span>
            </div>
          </div>
        </section>

      </div>
    </n-spin>

    <!-- Edit Modal -->
    <n-modal v-model:show="showEditModal" :mask-closable="true">
      <div class="edit-modal-content">
        <div class="modal-header">
          <h3>EDIT PROFILE</h3>
          <button class="close-btn" @click="showEditModal = false">
            <n-icon size="24"><CloseOutline /></n-icon>
          </button>
        </div>
        
        <n-form :model="editForm" label-placement="top" class="edit-form">
          <n-form-item label="NICKNAME">
            <n-input v-model:value="editForm.nickname" placeholder="Your persona name" />
          </n-form-item>
          
          <n-form-item label="SIGNATURE">
             <n-input 
               v-model:value="editForm.signature" 
               type="textarea" 
               placeholder="A brief bio..." 
               :autosize="{ minRows: 3, maxRows: 5 }"
             />
          </n-form-item>
          
          <n-form-item label="IDENTITY">
            <n-radio-group v-model:value="editForm.sex" name="sex">
              <n-radio :value="1">MALE</n-radio>
              <n-radio :value="0">FEMALE</n-radio>
              <n-radio :value="2">MYSTERY</n-radio>
            </n-radio-group>
          </n-form-item>
          
          <div class="modal-actions">
            <button class="save-btn" @click="handleSaveProfile" :disabled="saving">
              {{ saving ? 'SAVING...' : 'SAVE CHANGES' }}
            </button>
          </div>
        </n-form>
      </div>
    </n-modal>

    <!-- Avatar Crop Modal -->
    <n-modal v-model:show="showCropModal" :mask-closable="false">
      <div class="crop-modal-content">
      <div class="modal-header">
        <h3>CROP AVATAR</h3>
        <button class="close-btn" @click="showCropModal = false">
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
        <button class="save-btn" @click="confirmCrop" :disabled="croppingAvatar">
          {{ croppingAvatar ? 'UPLOADING...' : 'CONFIRM UPLOAD' }}
        </button>
      </div>
    </div>
    </n-modal>
  </div>
</template>

<style scoped>
/*
  Theme System
  Defining CSS Variables for Light/Dark modes
*/
/* Variables are now defined globally in src/styles/global.scss */

/* Page Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(60px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8) rotate(-5deg);
  }
  to {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

@keyframes expandWidth {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 100%;
    opacity: 1;
  }
}

@keyframes cardSlideUp {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Animation Classes */
.animate-fade-in {
  opacity: 0;
  animation: fadeIn 0.8s ease-out forwards;
}

.animate-slide-in {
  opacity: 0;
  animation: slideInLeft 1s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.animate-scale-in {
  opacity: 0;
  animation: scaleIn 1s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.animate-expand {
  width: 0;
  opacity: 0;
  animation: expandWidth 1s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.animate-card-in {
  opacity: 0;
  animation: cardSlideUp 0.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.profile-page {
  position: relative;
  min-height: 100vh;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: 'Lato', sans-serif;
  transition: background-color 0.5s ease, color 0.5s ease;
  overflow-x: hidden;
  max-width: 1200px;
  margin: 0 auto;
}

.bg-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  pointer-events: none;
  opacity: 0;
  transition: opacity 1s ease;
}

.profile-page.loaded .bg-canvas {
  opacity: 1;
}

.container {
  position: relative;
  z-index: 1;
  max-width: 1000px;
  margin: 0 auto;
  padding: 70px 40px 60px;
}

/* Header Grid */
.header-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 60px;
  margin-bottom: 60px;
  align-items: center;
  perspective: 1000px;
}

.info-col {
  display: flex;
  flex-direction: column;
}

.meta-line {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 0.75rem;
  letter-spacing: 2px;
  color: var(--text-tertiary);
  margin-bottom: 20px;
  font-family: 'Lato', sans-serif;
}

.edit-btn {
  background: transparent;
  border: 1px solid var(--text-tertiary);
  color: var(--text-secondary);
  padding: 4px 12px;
  font-size: 0.65rem;
  letter-spacing: 2px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
  border-radius: 20px;
}

.edit-btn:hover {
  border-color: var(--accent-color);
  color: var(--accent-color);
}

.nickname {
  font-family: 'Playfair Display', serif;
  font-size: 4.5rem;
  font-weight: 400;
  line-height: 1.1;
  margin: 0 0 30px;
  letter-spacing: -1px;
  color: var(--text-primary);
  position: relative;
  display: inline-block;
}

.nickname::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--accent-color), var(--accent-highlight));
  transition: width 1.2s cubic-bezier(0.4, 0, 0.2, 1) 0.5s;
}

.profile-page.loaded .nickname::after {
  width: 100%;
}

.bio-block {
  max-width: 500px;
  margin-bottom: 40px;
}

.signature {
  font-family: 'Playfair Display', serif;
  font-style: italic;
  font-size: 1.25rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.tags-line {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: var(--text-primary);
}

.tag {
  color: var(--accent-color);
  font-weight: 700;
}

.divider {
  margin: 0 10px;
  color: var(--text-tertiary);
}

/* Avatar */
.avatar-wrapper {
  width: 260px;
  height: 340px;
  position: relative;
  box-shadow: 20px 20px 0px var(--line-color);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--bg-primary);
  overflow: hidden;
}

.avatar-wrapper::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.1) 50%, transparent 70%);
  transform: translateX(-100%) translateY(-100%) rotate(45deg);
  transition: transform 0.8s;
}

.avatar-wrapper:hover::before {
  transform: translateX(100%) translateY(100%) rotate(45deg);
}

.avatar-wrapper:hover {
  transform: translate(-8px, -8px) scale(1.02);
  box-shadow: 28px 28px 0px var(--accent-color);
}

.avatar-img {
  width: 100% !important;
  height: 100% !important;
  transition: all 0.5s ease;
  border-radius: 0;
  --n-border-radius: 0;
}

.avatar-img :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-img :deep(.n-icon) {
  font-size: 80px;
}

.avatar-wrapper:hover .avatar-img {
  transform: scale(1.05);
}

/* Ensure NAvatar fills the container */
:deep(.n-avatar) {
  width: 100%;
  height: 100%;
}


.avatar-container-inner {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.avatar-uploader {
  width: 100%; height: 100%;
  cursor: pointer;
  display: block;
}

/* New Floating Change Button */
.change-btn {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 50px;
  background: rgba(26, 26, 26, 0.8); /* Dark semi-transparent */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  letter-spacing: 2px;
  font-size: 0.8rem;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(4px);
}

:global(html.dark) .change-btn {
  background: rgba(230, 230, 230, 0.8);
  color: #1a1a1a;
}

.avatar-wrapper:hover .change-btn {
  transform: translateY(0);
}

/* Deprecated Overlay Style Removal */
/* .overlay { ... } */


/* Divider */
.divider-line {
  height: 1px;
  background: var(--line-color);
  margin: 0 0 60px;
}

/* Content */
.section-head {
  display: flex;
  align-items: baseline;
  gap: 15px;
  margin-bottom: 40px;
}

.section-head h2 {
  font-family: 'Playfair Display', serif;
  font-size: 2rem;
  font-weight: 400;
  margin: 0;
}

.section-head .count {
  font-family: 'Playfair Display', serif;
  font-style: italic;
  color: var(--text-tertiary);
  font-size: 1.2rem;
}

/* Blog Grid */
.blog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
}

.blog-card {
  background: transparent;
  border: 1px solid var(--line-color);
  padding: 30px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  height: 280px;
  position: relative;
  overflow: hidden;
}

.blog-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, var(--card-hover), transparent);
  transition: left 0.6s;
}

.blog-card:hover::before {
  left: 100%;
}

.blog-card:hover {
  background: var(--card-hover);
  border-color: var(--accent-color);
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--text-tertiary);
  margin-bottom: 20px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.cat-tag {
  color: var(--accent-color);
}

.card-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.5rem;
  margin: 0 0 15px;
  font-weight: 400;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: all 0.3s;
  position: relative;
}

.blog-card:hover .card-title {
  color: var(--accent-color);
  transform: translateX(5px);
}

.card-summary {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.6;
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 20px;
  transition: color 0.3s;
}

.blog-card:hover .card-summary {
  color: var(--text-primary);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  letter-spacing: 2px;
  color: var(--text-primary);
  opacity: 0.6;
  transition: all 0.3s;
}

.blog-card:hover .card-footer {
  opacity: 1;
  color: var(--accent-color);
  transform: translateX(5px);
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 100px 0;
}

.void-text {
  font-family: 'Playfair Display', serif;
  font-size: 3rem;
  color: var(--line-color);
  letter-spacing: 10px;
}

/* Modal Styles */
.edit-modal-content {
  background: var(--modal-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 40px;
  width: 90%;
  max-width: 500px;
  border: 1px solid var(--line-color);
  box-shadow: 0 25px 50px rgba(0,0,0,0.2);
}

.crop-modal-content {
  background: var(--modal-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 24px;
  width: 90vw;
  max-width: 600px;
  height: 70vh; /* 降低高度占比，确保在小屏幕也安全 */
  max-height: 600px;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--line-color);
  box-shadow: 0 25px 50px rgba(0,0,0,0.2);
  border-radius: 12px;
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

.modal-body {
  flex: 1;
  min-height: 0;
  position: relative;
  margin: 16px 0;
  background: #111;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.native-cropper-wrapper {
  width: 100%;
  max-width: 360px; /* 限制最大宽度 */
  aspect-ratio: 1 / 1; /* 严格限制 1:1 */
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
  width: 100%; /* 铺满 1:1 的 wrapper */
  height: 100%;
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%; /* 圆形遮罩 */
  box-sizing: border-box;
  position: relative;
}

/* 通过阴影实现视口外的变暗效果 */
.crop-viewport::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-shadow: 0 0 0 1000px rgba(0, 0, 0, 0.3);
}

.modal-header h3 {
  font-family: 'Playfair Display', serif;
  margin: 0;
  font-size: 1.5rem;
  letter-spacing: 2px;
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

.edit-form :deep(.n-form-item-label) {
  font-size: 0.75rem;
  letter-spacing: 2px;
  color: var(--text-tertiary);
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

.save-btn:hover {
  background: var(--accent-color);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.15);
}

.save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .header-grid {
    grid-template-columns: 1fr;
    gap: 40px;
    text-align: center;
  }
  
  .info-col {
    order: 2;
  }
  
  .avatar-col {
    order: 1;
    display: flex;
    justify-content: center;
  }
  
  .meta-line {
    justify-content: center;
  }
  
  .tags-line {
    justify-content: center;
  }
  
  .bio-block {
    margin-left: auto;
    margin-right: auto;
  }

  .nickname {
    font-size: 3rem;
  }

  .container {
    padding: 80px 20px;
  }
}
</style>