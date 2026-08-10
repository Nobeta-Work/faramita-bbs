<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, h } from 'vue'
import { useRoute } from 'vue-router'
import {
  NUpload, NIcon, useMessage, NSpin, NModal, NForm, NFormItem, NInput, NRadioGroup, NRadio, NAvatar
} from 'naive-ui'
import {
  ArrowForwardOutline, LogoGithub, LogoTwitter, PencilOutline, CloseOutline, CameraOutline, Person
} from '@vicons/ionicons5'
import { getUserInfo, updateCurrentUserAvatar, updateCurrentUserProfile } from '@/api/user'
import { getPublicBlogPage } from '@/api/blog'
import { resolveAvatarUrl } from '@/utils/avatar'
import { useUserStore } from '@/stores/user'
import type { BlogPublicBriefVO, UserInfoVO, UserSex } from '@/types'
import { DateUtils } from '@/types/date'
import router from '@/router'

// Fonts
const fontLink = document.createElement('link')
fontLink.href = 'https://fonts.googleapis.com/css2?family=Lato:wght@300;400;700&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap'
fontLink.rel = 'stylesheet'
document.head.appendChild(fontLink)

const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

const uid = computed(() => String(route.params.uid || ''))
const isCurrentUser = computed(() => String(userStore.userInfo?.id ?? '') === uid.value)

const user = ref<UserInfoVO | null>(null)
const userAvatarUrl = computed(() => resolveAvatarUrl(user.value?.avatar))
const blogList = ref<BlogPublicBriefVO[]>([])

interface JourneyDateParts {
  year: string
  month: string
  day: string
  time: string
  full: string
}

const formatJourneyDate = (value: string): JourneyDateParts => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return {
      year: '----',
      month: '--',
      day: '--',
      time: '--:--',
      full: value,
    }
  }

  return {
    year: String(date.getFullYear()),
    month: String(date.getMonth() + 1).padStart(2, '0'),
    day: String(date.getDate()).padStart(2, '0'),
    time: `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`,
    full: DateUtils.formatToDateOnly(date),
  }
}

const journeyStops = computed(() => blogList.value.slice(0, 6).map((blog, index) => ({
  blog,
  index,
  date: formatJourneyDate(blog.createTime),
})))

const latestPublishedText = computed(() => {
  const latest = blogList.value[0]
  return latest ? DateUtils.isoToDateOnly(latest.createTime) : '暂无发布'
})

const renderDefaultAvatar = () => h(NIcon, null, { default: () => h(Person) })
const loading = ref(false)
const pageLoaded = ref(false)

// Edit Profile Logic
const showEditModal = ref(false)
const editForm = ref({
  nickname: '', sex: 2 as UserSex, race: ''
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
const activePointerId = ref<number | null>(null)
const lastPointerPos = ref({ x: 0, y: 0 })

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

const handleCropPointerDown = (e: PointerEvent) => {
  if (activePointerId.value !== null) return

  activePointerId.value = e.pointerId
  isDragging.value = true
  lastPointerPos.value = { x: e.clientX, y: e.clientY }
  cropContainerRef.value?.setPointerCapture(e.pointerId)
  e.preventDefault()
}

const handleCropPointerMove = (e: PointerEvent) => {
  if (!isDragging.value || activePointerId.value !== e.pointerId) return

  const dx = e.clientX - lastPointerPos.value.x
  const dy = e.clientY - lastPointerPos.value.y
  offset.value.x += dx
  offset.value.y += dy
  lastPointerPos.value = { x: e.clientX, y: e.clientY }
  e.preventDefault()
}

const handleCropPointerUp = (e: PointerEvent) => {
  if (activePointerId.value !== e.pointerId) return

  if (cropContainerRef.value?.hasPointerCapture(e.pointerId)) {
    cropContainerRef.value.releasePointerCapture(e.pointerId)
  }
  activePointerId.value = null
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
      await updateCurrentUserAvatar(file)
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
  editForm.value = {
    nickname: user.value.nickname,
    sex: user.value.sex,
    race: user.value.race
  }
  showEditModal.value = true
}

const handleSaveProfile = async () => {
  if (!editForm.value.nickname) {
    message.warning('Nickname is required')
    return
  }
  saving.value = true
  try {
    const { nickname, sex, race } = editForm.value

    await updateCurrentUserProfile({ nickname, sex, race })
    message.success('Profile Updated')
    showEditModal.value = false
    fetchProfile()

    // Update store if current user
    if (userStore.userInfo && String(userStore.userInfo.id ?? '') === uid.value) {
       userStore.userInfo = { ...userStore.userInfo, nickname, sex, race }
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
    const [profile, blogs] = await Promise.all([
      getUserInfo(uid.value),
      getPublicBlogPage({
        pageNum: 1,
        pageSize: 20,
        authorId: uid.value,
        sortField: 'createTime',
        sortOrder: 'desc'
      })
    ])
    user.value = profile
    blogList.value = blogs.records
    document.title = `${profile.nickname} - Para BBS`
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
    case 2: return 'FEMALE'
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
          <div class="section-head journey-head animate-fade-in" style="animation-delay: 0.6s">
            <div class="journey-title-block">
              <span class="section-kicker">CREATION JOURNEY</span>
              <h2>发布旅程</h2>
            </div>
            <div class="journey-stats" v-if="blogList.length">
              <div>
                <strong>{{ blogList.length }}</strong>
                <span>公开博客</span>
              </div>
              <div>
                <strong>{{ latestPublishedText }}</strong>
                <span>最近发布</span>
              </div>
            </div>
          </div>

          <div v-if="journeyStops.length" class="journey-map" aria-label="最近发布博客时间线">
            <article
              v-for="stop in journeyStops"
              :key="stop.blog.id"
              class="journey-stop animate-card-in"
              :class="{ 'is-latest': stop.index === 0 }"
              :style="{ animationDelay: `${0.7 + stop.index * 0.1}s` }"
              @click="router.push(`/blog/${stop.blog.id}`)"
            >
              <time class="journey-date" :datetime="stop.date.full">
                <span>{{ stop.date.month }}月</span>
                <strong>{{ stop.date.day }}</strong>
                <span>{{ stop.date.year }}</span>
              </time>

              <div class="journey-rail" aria-hidden="true">
                <span class="journey-dot">{{ stop.index + 1 }}</span>
              </div>

              <div class="journey-card">
                <div class="journey-card-head">
                  <span class="cat-tag">{{ stop.blog.tags?.[0]?.name || 'Article' }}</span>
                  <span class="time">{{ stop.date.time }} 发布</span>
                </div>
                <h3 class="card-title">{{ stop.blog.title }}</h3>
                <p class="card-summary">{{ stop.blog.summary || '这篇博客暂时没有摘要。' }}</p>
                <div class="card-footer">
                  <span>{{ stop.blog.likeCount || 0 }} likes</span>
                  <span class="read-more">
                    READ ENTRY
                    <n-icon><ArrowForwardOutline /></n-icon>
                  </span>
                </div>
              </div>
            </article>

            <p v-if="blogList.length > journeyStops.length" class="journey-note">
              展示最近 {{ journeyStops.length }} 篇公开发布，共 {{ blogList.length }} 篇。
            </p>
          </div>

          <div v-else class="empty-state">
            <span class="void-text">尚未发布</span>
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
          
          <n-form-item label="IDENTITY">
            <n-radio-group v-model:value="editForm.sex" name="sex">
              <n-radio :value="1">MALE</n-radio>
              <n-radio :value="2">FEMALE</n-radio>
              <n-radio :value="0">MYSTERY</n-radio>
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
          @pointerdown="handleCropPointerDown"
          @pointermove="handleCropPointerMove"
          @pointerup="handleCropPointerUp"
          @pointercancel="handleCropPointerUp"
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
.content-section {
  position: relative;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 15px;
  margin-bottom: 40px;
}

.section-kicker {
  display: block;
  margin-bottom: 10px;
  color: var(--accent-color);
  font-family: 'Lato', sans-serif;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 3px;
}

.section-head h2 {
  font-family: 'Playfair Display', serif;
  font-size: 2.35rem;
  font-weight: 400;
  margin: 0;
}

.journey-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border: 1px solid var(--line-color);
  background: var(--card-hover);
  min-width: 330px;
}

.journey-stats div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 18px;
}

.journey-stats div + div {
  border-left: 1px solid var(--line-color);
}

.journey-stats strong {
  color: var(--text-primary);
  font-family: 'Playfair Display', serif;
  font-size: 1.25rem;
  font-weight: 400;
  line-height: 1;
}

.journey-stats span {
  color: var(--text-tertiary);
  font-size: 0.72rem;
  letter-spacing: 1.5px;
}

.journey-map {
  display: flex;
  flex-direction: column;
  gap: 18px;
  position: relative;
}

.journey-stop {
  display: grid;
  grid-template-columns: 86px 42px minmax(0, 1fr);
  gap: 20px;
  cursor: pointer;
  position: relative;
}

.journey-date {
  align-items: flex-end;
  color: var(--text-tertiary);
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding-top: 18px;
  text-align: right;
}

.journey-date span {
  font-size: 0.72rem;
  letter-spacing: 1.5px;
}

.journey-date strong {
  color: var(--text-primary);
  font-family: 'Playfair Display', serif;
  font-size: 2.25rem;
  font-weight: 400;
  line-height: 1;
}

.journey-rail {
  display: flex;
  justify-content: center;
  position: relative;
}

.journey-rail::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: -18px;
  width: 1px;
  background: linear-gradient(180deg, transparent, var(--line-color) 18px, var(--line-color) calc(100% - 18px), transparent);
}

.journey-stop:last-of-type .journey-rail::before {
  bottom: 50%;
}

.journey-dot {
  align-items: center;
  background: var(--bg-primary);
  border: 1px solid var(--line-color);
  color: var(--text-secondary);
  display: inline-flex;
  font-size: 0.72rem;
  font-weight: 700;
  height: 32px;
  justify-content: center;
  letter-spacing: 0;
  margin-top: 22px;
  position: relative;
  width: 32px;
  z-index: 1;
}

.is-latest .journey-dot {
  background: var(--accent-color);
  border-color: var(--accent-color);
  color: var(--bg-primary);
}

.journey-card {
  background:
    linear-gradient(135deg, var(--card-hover), transparent 58%),
    var(--bg-primary);
  border: 1px solid var(--line-color);
  min-width: 0;
  padding: 22px 24px;
  position: relative;
  transition: border-color 0.25s ease, box-shadow 0.25s ease, transform 0.25s ease;
}

.journey-card::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 3px;
  background: var(--line-color);
  transition: background 0.25s ease;
}

.journey-stop:hover .journey-card {
  border-color: var(--accent-color);
  box-shadow: 10px 10px 0 var(--line-color);
  transform: translateX(5px);
}

.journey-stop:hover .journey-card::before,
.is-latest .journey-card::before {
  background: var(--accent-color);
}

.journey-card-head,
.card-footer {
  align-items: center;
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.journey-card-head {
  color: var(--text-tertiary);
  font-size: 0.72rem;
  letter-spacing: 1px;
  margin-bottom: 14px;
}

.cat-tag {
  color: var(--accent-color);
  font-weight: 700;
  text-transform: uppercase;
}

.time {
  color: var(--text-tertiary);
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

.journey-stop:hover .card-title {
  color: var(--accent-color);
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

.journey-stop:hover .card-summary {
  color: var(--text-primary);
}

.card-footer {
  font-size: 0.75rem;
  letter-spacing: 2px;
  color: var(--text-primary);
  opacity: 0.6;
  transition: all 0.3s;
}

.read-more {
  align-items: center;
  display: inline-flex;
  gap: 8px;
  white-space: nowrap;
}

.journey-stop:hover .card-footer {
  opacity: 1;
  color: var(--accent-color);
}

.journey-note {
  color: var(--text-tertiary);
  font-family: 'Playfair Display', serif;
  font-size: 1rem;
  font-style: italic;
  margin: 12px 0 0 148px;
}

.empty-state {
  border: 1px solid var(--line-color);
  background: var(--card-hover);
  text-align: center;
  padding: 80px 24px;
}

.void-text {
  font-family: 'Playfair Display', serif;
  font-size: 2.2rem;
  color: var(--line-color);
  letter-spacing: 6px;
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
  max-height: 100%;
  aspect-ratio: 1 / 1; /* 严格限制 1:1 */
  position: relative;
  cursor: move;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 0 20px rgba(0,0,0,0.5);
  border: 1px solid rgba(255,255,255,0.1);
  touch-action: none;
  -webkit-user-select: none;
  user-select: none;
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

  .journey-head {
    align-items: stretch;
    flex-direction: column;
  }

  .journey-stats {
    min-width: 0;
    width: 100%;
  }

  .journey-stop {
    grid-template-columns: 58px 28px minmax(0, 1fr);
    gap: 12px;
  }

  .journey-date {
    padding-top: 16px;
  }

  .journey-date strong {
    font-size: 1.65rem;
  }

  .journey-date span {
    font-size: 0.66rem;
  }

  .journey-dot {
    height: 24px;
    margin-top: 22px;
    width: 24px;
  }

  .journey-card {
    padding: 18px;
  }

  .journey-card-head,
  .card-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .journey-note {
    margin-left: 0;
  }

  .crop-modal-content {
    width: min(92vw, 420px);
    height: auto;
    max-height: calc(100dvh - 32px);
    padding: 18px;
  }

  .modal-body {
    flex: 0 1 auto;
    max-height: min(68vw, calc(100dvh - 180px));
  }

  .native-cropper-wrapper {
    width: min(68vw, calc(100dvh - 180px));
    max-width: 100%;
  }
}
</style>
