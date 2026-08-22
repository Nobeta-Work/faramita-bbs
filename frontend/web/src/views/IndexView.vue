<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { Search, ArrowForwardOutline } from '@vicons/ionicons5'
import { getPublicBlogPage } from '@/api/blog'
import type { BlogPublicBriefVO } from '@/types/blog'
import ParticleBackground from '@/components/ParticleBackground.vue'

// Fonts
const fontLink = document.createElement('link')
fontLink.href = 'https://fonts.googleapis.com/css2?family=Agbalumo:wght@400&display=swap'
fontLink.rel = 'stylesheet'
document.head.appendChild(fontLink)

const router = useRouter()
const keyword = ref('')
const showContent = ref(false)
const recentBlogs = ref<BlogPublicBriefVO[]>([])

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ name: 'BlogList', query: { keyword: keyword.value } })
  } else {
    router.push({ name: 'BlogList' })
  }
}

const fetchRecentBlogs = async () => {
  try {
    const res = await getPublicBlogPage({
      pageNum: 1,
      pageSize: 3,
      sortField: 'createTime',
      sortOrder: 'desc',
    })
    recentBlogs.value = res.records || []
  } catch (error) {
    console.error('Failed to fetch recent blogs:', error)
  }
}

onMounted(() => {
  setTimeout(() => {
    showContent.value = true
  }, 100)
  fetchRecentBlogs()
})
</script>

<template>
  <div class="index-page" :class="{ 'loaded': showContent }">
    <ParticleBackground class="bg-canvas" />
    
    <div class="content-wrapper">
      <div class="hero-section animate-section">
        <div class="meta-line animate-fade-in" style="animation-delay: 0.1s">
          <span class="date">EST. 2025</span>
          <span class="divider">/</span>
          <span class="issue">Version 0.4.1</span>
        </div>

        <div class="title-wrapper animate-slide-in" style="animation-delay: 0.2s">
          <span class="logo-text">Para</span>
          <span class="logo-text italic">BBS</span>
        </div>
        
        <div class="subtitle-wrapper animate-fade-in" style="animation-delay: 0.3s">
          <p class="subtitle">在此岸彼岸 · 在所有的岸上</p>
          <div class="decorative-line"></div>
        </div>
        
        <div class="search-container animate-scale-in" style="animation-delay: 0.4s">
          <div class="search-box">
            <n-icon class="search-icon" :component="Search" />
            <input 
              v-model="keyword" 
              type="text"
              placeholder="Discover topics..." 
              class="hero-input" 
              @keyup.enter="handleSearch"
            />
            <button class="explore-btn" @click="handleSearch">
              <span>EXPLORE</span>
              <n-icon :component="ArrowForwardOutline" />
            </button>
          </div>
        </div>
      </div>

      <!-- Recent Blogs Section -->
      <div class="recent-section animate-fade-in" style="animation-delay: 0.6s" v-if="recentBlogs.length > 0">
        <div class="recent-header">
          <span class="recent-title">LATEST STORIES</span>
          <div class="recent-line"></div>
        </div>
        <div class="recent-grid">
          <div 
            v-for="blog in recentBlogs" 
            :key="blog.id" 
            class="recent-card"
            @click="router.push(`/blog/${blog.id}`)"
          >
            <div class="recent-meta">
              <span class="recent-category">{{ blog.tags?.[0]?.name || 'Article' }}</span>
              <span class="recent-date">{{ blog.createTime?.split(' ')[0] }}</span>
            </div>
            <h3 class="recent-blog-title">{{ blog.title }}</h3>
            <p class="recent-author">by {{ blog.author.nickname }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideInUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.animate-fade-in {
  opacity: 0;
  animation: fadeIn 1s ease-out forwards;
}

.animate-slide-in {
  opacity: 0;
  animation: slideInUp 1s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.animate-scale-in {
  opacity: 0;
  animation: scaleIn 1s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.index-page {
  min-height: calc(100vh - 70px);
  min-height: calc(100dvh - 70px);
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow-x: hidden;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: 'Lato', sans-serif;
  transition: background-color 0.5s ease, color 0.5s ease;
  z-index: 1;
}

.bg-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  pointer-events: none;
  opacity: 0;
  transition: opacity 1.5s ease;
}

.index-page.loaded .bg-canvas {
  opacity: 1;
}

.content-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 1200px;
  min-height: calc(100dvh - 70px);
  padding: 30px 40px 60px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-section {
  text-align: center;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 0;
}

.meta-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  font-size: 0.75rem;
  letter-spacing: 4px;
  color: var(--text-tertiary);
  margin-bottom: 30px;
  text-transform: uppercase;
}

.divider {
  color: var(--accent-color);
}

.title-wrapper {
  margin-bottom: 20px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 100%;
  padding: 0 8px;
  box-sizing: border-box;
  line-height: 1;
}

.logo-text {
  font-family: 'Agbalumo', serif;
  font-size: clamp(4rem, 10vw, 8rem);
  font-weight: 400;
  margin: 0;
  color: var(--text-primary);
  letter-spacing: -2px;
  white-space: nowrap;
  text-shadow: 0 10px 30px rgba(0,0,0,0.05);
}

html.dark .logo-text {
  text-shadow: 0 10px 30px rgba(0,0,0,0.3);
}

.logo-text.italic {
  font-style: italic;
  color: var(--accent-color);
  transform: translateX(clamp(0px, 2vw, 30px));
  margin-top: -10px;
}

.subtitle-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 50px;
}

.subtitle {
  font-family: 'Playfair Display', serif;
  font-size: 1.6rem;
  color: var(--text-secondary);
  margin: 0 0 20px;
  font-style: italic;
  letter-spacing: 2px;
}

.decorative-line {
  width: 80px;
  height: 1px;
  background-color: var(--text-tertiary);
}

.search-container {
  width: 100%;
  max-width: 600px;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 40px;
  padding: 5px 10px 5px 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

html.dark .search-container {
  background: rgba(20, 20, 25, 0.6);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.search-container:focus-within {
  box-shadow: 0 15px 40px rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.3);
  transform: translateY(-2px);
}

html.dark .search-container:focus-within {
  box-shadow: 0 15px 40px rgba(168, 192, 255, 0.15);
  border-color: rgba(168, 192, 255, 0.3);
}

.search-box {
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 0;
}

.search-icon {
  font-size: 1.4rem;
  color: var(--text-tertiary);
  margin-right: 15px;
  transition: color 0.3s ease;
}

.search-container:focus-within .search-icon {
  color: var(--accent-color);
}

.hero-input {
  flex: 1;
  width: 0;
  min-width: 0;
  box-sizing: border-box;
  background: transparent;
  border: none;
  outline: none;
  font-family: 'Lato', sans-serif;
  font-size: 1.1rem;
  color: var(--text-primary);
  letter-spacing: 1px;
  padding: 15px 0;
}

.hero-input::placeholder {
  color: var(--text-tertiary);
  font-style: italic;
  font-family: 'Playfair Display', serif;
}

.explore-btn {
  flex: 0 0 auto;
  box-sizing: border-box;
  white-space: nowrap;
  background: var(--text-primary);
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--bg-primary);
  font-family: 'Lato', sans-serif;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
  padding: 12px 24px;
  border-radius: 30px;
  transition: all 0.3s ease;
}

.explore-btn:hover {
  background: var(--accent-color);
  transform: translateX(2px);
}

/* Recent Blogs Section */
.recent-section {
  width: 100%;
  max-width: 1000px;
  margin-top: 20px;
}

.recent-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.recent-title {
  font-family: 'Lato', sans-serif;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 3px;
  color: var(--text-secondary);
}

.recent-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, var(--line-color), transparent);
}

.recent-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.recent-card {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 25px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
}

html.dark .recent-card {
  background: rgba(20, 20, 25, 0.4);
  border-color: rgba(255, 255, 255, 0.05);
}

.recent-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.05);
  border-color: rgba(99, 102, 241, 0.2);
}

html.dark .recent-card:hover {
  background: rgba(30, 30, 35, 0.8);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3);
  border-color: rgba(168, 192, 255, 0.2);
}

.recent-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 0.75rem;
  font-family: 'Lato', sans-serif;
  letter-spacing: 1px;
}

.recent-category {
  color: var(--accent-color);
  font-weight: 700;
  text-transform: uppercase;
}

.recent-date {
  color: var(--text-tertiary);
}

.recent-blog-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 15px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recent-author {
  margin: auto 0 0 0;
  font-family: 'Lato', sans-serif;
  font-size: 0.85rem;
  color: var(--text-secondary);
  font-style: italic;
}

/* Responsive */
@media (max-width: 1024px) {
  .recent-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .recent-card:last-child {
    display: none;
  }
}

@media (max-width: 768px) {
  .index-page {
    align-items: flex-start;
  }

  .content-wrapper {
    min-height: 0;
    padding: 0 20px 48px;
  }

  .hero-section {
    margin-top: 1.5rem;
  }

  .meta-line {
    gap: 10px;
    margin-bottom: 18px;
    font-size: 0.68rem;
    letter-spacing: 3px;
  }

  .logo-text {
    font-size: clamp(3rem, 10vw, 5rem);
    letter-spacing: -3px;
  }
  
  .logo-text.italic {
    transform: none;
    margin-top: -6px;
  }
  
  .subtitle {
    font-size: 1.2rem;
  }

  .subtitle-wrapper {
    margin-bottom: 28px;
  }
  
  .search-container {
    padding: 5px;
    border-radius: 25px;
  }
  
  .search-icon {
    display: none;
  }
  
  .hero-input {
    padding: 10px 12px;
    font-size: 1rem;
  }
  
  .explore-btn {
    gap: 4px;
    padding: 10px 12px;
    font-size: 0.7rem;
    letter-spacing: 1px;
  }

  .recent-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .recent-section {
    margin-top: 34px;
  }
  
  .recent-card:nth-child(2) {
    display: none;
  }
}
</style>
