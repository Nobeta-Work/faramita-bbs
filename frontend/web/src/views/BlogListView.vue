<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NInput, NSelect, NPagination, NModal,
  NForm, NFormItem, NIcon, NEmpty, NSpin, useMessage, useDialog, NAvatar
} from 'naive-ui'
import { Search, Add, ArrowForward, GridOutline, Person, HeartOutline } from '@vicons/ionicons5'
import { createPrivateBlog, getPublicBlogPage } from '@/api/blog'
import { resolveAvatarUrl } from '@/utils/avatar'
import type { BlogPageQuery, BlogPublicBriefVO } from '@/types'
import { DateUtils } from '@/types/date'
import { useUserStore } from '@/stores/user'

// Fonts
const fontLink = document.createElement('link')
fontLink.href = 'https://fonts.googleapis.com/css2?family=Lato:wght@300;400;700&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap'
fontLink.rel = 'stylesheet'
document.head.appendChild(fontLink)

const router = useRouter()
const route = useRoute()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const loading = ref(false)
const blogList = ref<BlogPublicBriefVO[]>([])
const total = ref(0)
const showCreateModal = ref(false)

const handleCreateClick = () => {
  if (!userStore.isAuthenticated) {
    dialog.warning({
      title: '需要登录',
      content: '开启创作旅程前，请先登录您的账号。',
      positiveText: '去登录',
      negativeText: '再看看',
      onPositiveClick: () => {
        router.push('/login')
      }
    })
    return
  }
  showCreateModal.value = true
}

const searchForm = ref<BlogPageQuery>({
  pageNum: 1,
  pageSize: 9,
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : '',
  sortField: 'createTime',
  sortOrder: 'desc'
})

const createForm = ref({
  title: ''
})

const sortOptions = [
  { label: '最新发布', value: 'createTime' },
  { label: '最近更新', value: 'updateTime' }
]

// 默认头像图标渲染函数
const renderDefaultAvatar = () => h(NIcon, null, { default: () => h(Person) })

const fetchBlogs = async () => {
  loading.value = true
  try {
    const params: BlogPageQuery = {
      ...searchForm.value,
      keyword: searchForm.value.keyword?.trim() || undefined
    }
    
    const res = await getPublicBlogPage(params)
    blogList.value = res.records
    total.value = res.total
  } catch (error) {
    message.error('获取博客列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.value.pageNum = 1
  fetchBlogs()
}

const handlePageChange = (page: number) => {
  searchForm.value.pageNum = page
  fetchBlogs()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleCreateBlog = async () => {
  if (!createForm.value.title) {
    message.warning('请输入标题')
    return
  }
  try {
    const blogId = await createPrivateBlog({
      title: createForm.value.title,
      folderId: 0
    })
    message.success('创建成功')
    showCreateModal.value = false
    createForm.value.title = ''
    router.push(`/workspace/blogs/${blogId}`)
  } catch (error) {
    message.error('创建失败')
  }
}

onMounted(() => {
  fetchBlogs()
})
</script>

<template>
  <div class="blog-list-page">
    <!-- Hero Header -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          The <span class="highlight">Chronicles</span>
        </h1>
        <p class="hero-subtitle">Explore thoughts, stories, and ideas.</p>
        
        <div class="search-bar-wrapper">
          <n-input 
            v-model:value="searchForm.keyword" 
            placeholder="Search articles..." 
            class="hero-search-input custom-input"
            round
            size="large"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon :component="Search" class="search-icon" />
            </template>
          </n-input>
          <button class="search-btn" @click="handleSearch">
            Search
          </button>
        </div>
      </div>

      <div class="hero-footer">
        <div class="footer-left">
          <div class="sort-selector">
            <n-select
              v-model:value="searchForm.sortField"
              :options="sortOptions"
              size="medium"
              class="custom-select"
              @update:value="handleSearch"
            />
          </div>
        </div>

        <div class="filter-tags"></div>

        <div class="footer-right">
          <button class="create-btn" @click="handleCreateClick">
            <n-icon :component="Add" />
            Write
          </button>
        </div>
      </div>
    </div>

    <!-- Content Section -->
    <div class="content-container">
      <n-spin :show="loading">
        <div v-if="blogList.length > 0">
          <div class="blog-grid">
            <div 
              v-for="(blog, index) in blogList" 
              :key="blog.id" 
              class="blog-card-wrapper"
              :style="{ animationDelay: `${index * 0.1}s` }"
              @click="router.push(`/blog/${blog.id}`)"
            >
              <div class="blog-card">
                <div class="card-header">
                  <div class="card-header-content">
                    <span class="category-badge">
                      {{ blog.tags?.[0]?.name || 'Article' }}
                    </span>
                    <span class="read-more-icon">
                      <n-icon :component="ArrowForward" />
                    </span>
                  </div>
                </div>
                
                <div class="card-body">
                  <div class="card-meta-top">
                    <span class="sub-category">{{ blog.tags?.map(tag => tag.name).join(' / ') || 'General' }}</span>
                    <span class="publish-date">{{ DateUtils.isoToDateOnly(blog.createTime) }}</span>
                  </div>
                  
                  <h3 class="card-title">{{ blog.title }}</h3>
                  <p class="card-summary">{{ blog.summary || 'No summary available...' }}</p>
                  
                  <div class="card-footer">
                    <div class="author-profile">
                      <n-avatar 
                        round 
                        size="small" 
                        :src="resolveAvatarUrl(blog.author.avatar)" 
                        :render-icon="renderDefaultAvatar"
                        class="author-avatar"
                      />
                      <span class="author-name">{{ blog.author.nickname }}</span>
                    </div>
                    <span class="like-count">
                      <n-icon :component="HeartOutline" />
                      {{ blog.likeCount || 0 }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="pagination-container">
            <n-pagination
              v-model:page="searchForm.pageNum"
              :item-count="total"
              :page-size="searchForm.pageSize"
              size="large"
              @update:page="handlePageChange"
            />
          </div>
        </div>
        
        <n-empty v-else description="No articles found." class="empty-state">
           <template #icon>
             <n-icon :component="GridOutline" />
           </template>
        </n-empty>
      </n-spin>
    </div>

    <!-- Create Modal -->
    <n-modal v-model:show="showCreateModal">
      <div class="create-modal-content">
        <div class="modal-header">
          <h3>New Article</h3>
        </div>
        <div class="modal-body">
            <n-form :model="createForm" size="large" class="custom-form">
            <n-form-item label="Title" path="title">
                <n-input v-model:value="createForm.title" placeholder="Enter article title" class="custom-input" />
            </n-form-item>
            </n-form>
        </div>
        <div class="modal-actions">
            <button class="cancel-btn" @click="showCreateModal = false">Cancel</button>
            <button class="save-btn" @click="handleCreateBlog">Create</button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<style scoped>
.blog-list-page {
  min-height: 100vh;
  background-color: transparent;
  padding-bottom: 80px;
  position: relative;
  overflow: hidden;
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 0px 20px 40px;
  margin-bottom: 40px;
  text-align: center;
  z-index: 10;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-title {
  font-family: 'Playfair Display', serif;
  font-size: 4rem;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.2;
  letter-spacing: 2px;
  color: var(--text-primary);
}

.highlight {
  font-style: italic;
  color: var(--accent-color);
}

.hero-subtitle {
  font-family: 'Lato', sans-serif;
  font-size: 1.2rem;
  color: var(--text-secondary);
  margin-bottom: 40px;
  letter-spacing: 1px;
}

.search-bar-wrapper {
  display: flex;
  gap: 0;
  max-width: 600px;
  margin: 0 auto 40px;
  border-bottom: 2px solid var(--text-primary);
  padding-bottom: 5px;
  transition: border-color 0.3s;
}

.search-bar-wrapper:focus-within {
  border-color: var(--accent-color);
}

.hero-search-input {
  flex: 1;
}

.custom-input :deep(.n-input__input-el) {
  font-family: 'Lato', sans-serif;
  font-size: 1.1rem;
  color: var(--text-primary);
}

.custom-input :deep(.n-input__border),
.custom-input :deep(.n-input__state-border) {
  display: none;
}

.custom-input {
  background: transparent;
}

.search-btn {
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-family: 'Lato', sans-serif;
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: 2px;
  cursor: pointer;
  padding: 0 20px;
  text-transform: uppercase;
  transition: color 0.3s;
}

.search-btn:hover {
  color: var(--accent-color);
}

/* Content Container */
.content-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 10;
}

.hero-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  border-bottom: 1px solid var(--line-color);
  padding-bottom: 20px;
  animation: fadeIn 0.8s ease-out 0.2s backwards;
}

.footer-left, .footer-right {
  display: flex;
  align-items: center;
}

.custom-select {
  width: 150px;
}

.custom-select :deep(.n-base-selection) {
  background: transparent;
  border: 1px solid var(--line-color);
  border-radius: 0;
}

.custom-select :deep(.n-base-selection__border),
.custom-select :deep(.n-base-selection__state-border) {
  display: none;
}

.custom-select :deep(.n-base-selection-input) {
  font-family: 'Lato', sans-serif;
  color: var(--text-primary);
}

.filter-tags {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex: 1;
}

.filter-tag {
  font-family: 'Lato', sans-serif;
  font-size: 0.9rem;
  letter-spacing: 1px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  padding: 5px 0;
  position: relative;
  text-transform: uppercase;
}

.filter-tag::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 1px;
  background-color: var(--text-primary);
  transition: width 0.3s;
}

.filter-tag:hover {
  color: var(--text-primary);
}

.filter-tag:hover::after,
.filter-tag.active::after {
  width: 100%;
}

.filter-tag.active {
  color: var(--text-primary);
  font-weight: 700;
}

.create-btn {
  background: var(--text-primary);
  color: var(--bg-primary);
  border: none;
  padding: 10px 20px;
  font-family: 'Lato', sans-serif;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.create-btn:hover {
  background: var(--accent-color);
  transform: translateY(-2px);
}

/* Blog Grid */
.blog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 40px;
  margin-top: 40px;
}

.blog-card-wrapper {
  animation: slideInUp 0.6s ease-out backwards;
}

@keyframes slideInUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

.blog-card {
  height: 100%;
  background: var(--bg-primary);
  border: 1px solid var(--line-color);
  display: flex;
  flex-direction: column;
  transition: all 0.4s ease;
  cursor: pointer;
  position: relative;
}

.blog-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: var(--text-primary);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s ease;
}

.blog-card:hover {
  transform: translateY(-10px);
  box-shadow: 15px 15px 0px var(--line-color);
}

.blog-card:hover::before {
  transform: scaleX(1);
  background: var(--accent-color);
}

.card-header {
  padding: 20px 24px 0;
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--line-color);
  padding-bottom: 15px;
}

.category-badge {
  font-family: 'Lato', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: var(--text-primary);
}

.read-more-icon {
  color: var(--text-secondary);
  transition: all 0.3s;
  transform: translateX(-10px);
  opacity: 0;
}

.blog-card:hover .read-more-icon {
  transform: translateX(0);
  opacity: 1;
  color: var(--accent-color);
}

.card-body {
  padding: 20px 24px 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-meta-top {
  display: flex;
  justify-content: space-between;
  font-family: 'Lato', sans-serif;
  font-size: 0.8rem;
  color: var(--text-tertiary);
  margin-bottom: 15px;
  letter-spacing: 1px;
}

.sub-category {
  color: var(--accent-color);
}

.card-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 15px;
  line-height: 1.3;
  color: var(--text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s;
}

.blog-card:hover .card-title {
  color: var(--accent-color);
}

.card-summary {
  font-family: 'Lato', sans-serif;
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 24px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.card-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.author-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  border: 1px solid var(--line-color);
}

.author-name {
  font-family: 'Lato', sans-serif;
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--text-primary);
  text-transform: uppercase;
}

.like-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-tertiary);
  font-family: 'Lato', sans-serif;
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 1px;
}

.pagination-container {
  margin-top: 60px;
  display: flex;
  justify-content: center;
}

.pagination-container :deep(.n-pagination) {
  font-family: 'Lato', sans-serif;
}

.pagination-container :deep(.n-pagination-item) {
  border-radius: 0;
  border: 1px solid var(--line-color);
  background: transparent;
  color: var(--text-primary);
}

.pagination-container :deep(.n-pagination-item--active) {
  background: var(--text-primary);
  color: var(--bg-primary);
  border-color: var(--text-primary);
}

.empty-state {
  margin-top: 100px;
  font-family: 'Playfair Display', serif;
}

/* Modal Styles */
.create-modal-content {
    background: var(--modal-bg);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    padding: 30px;
    width: 90vw;
    max-width: 500px;
    border: 1px solid var(--line-color);
    box-shadow: 0 25px 50px rgba(0,0,0,0.2);
    border-radius: 0;
    box-sizing: border-box;
}

.modal-header {
    margin-bottom: 24px;
    border-bottom: 1px solid var(--line-color);
    padding-bottom: 15px;
}

.modal-header h3 {
    font-family: 'Playfair Display', serif;
    margin: 0;
    font-size: 1.8rem;
    letter-spacing: 2px;
    color: var(--text-primary);
}

.custom-form :deep(.n-form-item-label) {
    font-family: 'Lato', sans-serif;
    font-size: 0.85rem;
    letter-spacing: 1px;
    color: var(--text-secondary);
}

.modal-actions {
    margin-top: 30px;
    display: flex;
    justify-content: flex-end;
    gap: 15px;
    border-top: 1px solid var(--line-color);
    padding-top: 20px;
}

.cancel-btn {
    background: transparent;
    border: 1px solid var(--line-color);
    color: var(--text-primary);
    padding: 10px 20px;
    font-family: 'Lato', sans-serif;
    font-weight: 700;
    letter-spacing: 1px;
    cursor: pointer;
    transition: all 0.3s;
}

.cancel-btn:hover {
    border-color: var(--text-primary);
}

.save-btn {
    background: var(--text-primary);
    color: var(--bg-primary);
    border: none;
    padding: 10px 25px;
    font-family: 'Lato', sans-serif;
    font-weight: 700;
    letter-spacing: 1px;
    cursor: pointer;
    transition: all 0.3s;
}

.save-btn:hover {
    background: var(--accent-color);
    transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 3rem;
  }
  .hero-footer {
    flex-direction: column;
    gap: 20px;
    align-items: stretch;
  }
  .footer-left, .footer-right {
    justify-content: center;
  }
  .filter-tags {
    flex-wrap: wrap;
  }
  .blog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
