<script setup lang="ts">
import { ref, onMounted, computed, watch, onUnmounted, nextTick, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import { getBlog, updateBlog, deleteBlog } from '@/api/blog'
import { getProfileByUid } from '@/api/user'
import { downloadAvatar, uploadImage } from '@/api/file'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'
import {
  NCard, NSpace, NSpin,
  NAvatar, NInput, NSwitch, NPopconfirm,
  NIcon, NModal
} from 'naive-ui'
import { ListOutline, Person } from '@vicons/ionicons5'
import { createDiscreteApi } from 'naive-ui'
import type { Blog } from '@/types/blog'
import type { BlogUpdateDTO } from '@/types/api'
import { DateUtils } from '@/types/date'

// Fonts
const fontLink = document.createElement('link')
fontLink.href = 'https://fonts.googleapis.com/css2?family=Lato:wght@300;400;700&family=Playfair+Display:ital,wght@0,400;0,700;1,400&display=swap'
fontLink.rel = 'stylesheet'
document.head.appendChild(fontLink)

const { message } = createDiscreteApi(['message'])

// Vditor instances
const vditor = ref<Vditor | null>(null)
const vditorPreviewRef = ref<HTMLDivElement | null>(null)
const vditorEditRef = ref<HTMLElement | null>(null)

// TOC types
interface TocItem {
  id: string
  text: string
  level: number
  parentId?: string
  hasChildren?: boolean
}

const tocItems = ref<TocItem[]>([])
const activeTocId = ref<string>('')
const expandedTocIds = ref<Set<string>>(new Set())

const toggleTocExpand = (id: string, e: Event) => {
  e.stopPropagation()
  const newSet = new Set(expandedTocIds.value)
  if (newSet.has(id)) {
    newSet.delete(id)
  } else {
    newSet.add(id)
  }
  expandedTocIds.value = newSet
}

const visibleTocItems = computed(() => {
  return tocItems.value.filter(item => {
    if (item.level === 1) return true
    let currentParentId = item.parentId
    while (currentParentId) {
      if (!expandedTocIds.value.has(currentParentId)) return false
      const parent = tocItems.value.find(t => t.id === currentParentId)
      currentParentId = parent?.parentId
    }
    return true
  })
})

watch(activeTocId, (newId) => {
  if (!newId) return
  
  const item = tocItems.value.find(t => t.id === newId)
  if (!item) return
  
  let currentParentId = item.parentId
  let changed = false
  const newSet = new Set(expandedTocIds.value)
  
  while (currentParentId) {
    if (!newSet.has(currentParentId)) {
      newSet.add(currentParentId)
      changed = true
    }
    const parent = tocItems.value.find(t => t.id === currentParentId)
    currentParentId = parent?.parentId
  }
  
  if (changed) {
    expandedTocIds.value = newSet
  }
  
  if (windowWidth.value > 1024) {
    nextTick(() => {
      const activeEl = document.querySelector('.toc-active')
      if (activeEl) {
        activeEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
      }
    })
  }
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)

const blogUid = route.params.bloguid as string
const blog = ref<Blog | null>(null)
const authorAvatar = ref<string | undefined>(undefined)
const loading = ref(false)

// 默认头像图标渲染函数
const renderDefaultAvatar = () => h(NIcon, null, { default: () => h(Person) })
const isEditing = ref(false)
const showTableModal = ref(false)
const tableRows = ref(0)
const tableCols = ref(0)
const hoverRows = ref(0)
const hoverCols = ref(0)
const content = ref('')
const summary = ref('')
const originalBlogData = ref<{
  title: string
  content: string
  summary: string
  isPublished: number
} | null>(null)
const isPublished = ref(1) // 1: published, 0: draft

const windowWidth = ref(window.innerWidth)
const editorHeight = computed(() => (windowWidth.value <= 1024 ? 520 : 760))
const showTocPanel = ref(false)
const sidePanelToggleText = computed(() => (showTocPanel.value ? 'Info' : 'TOC'))

const toggleSidePanel = () => {
  showTocPanel.value = !showTocPanel.value
}

const setPageScrollLock = (locked: boolean) => {
  const overflowValue = locked ? 'hidden' : ''
  document.documentElement.style.overflow = overflowValue
  document.body.style.overflow = overflowValue
}

const handleResize = () => {
  const wasMobile = windowWidth.value <= 1024
  windowWidth.value = window.innerWidth
  const isMobile = windowWidth.value <= 1024
  
  if (wasMobile !== isMobile) {
    if (scrollSpyObserver) {
      scrollSpyObserver.disconnect()
      scrollSpyObserver = null
    }
    if (!isMobile) {
      scrollSpyObserver = setupScrollSpy()
    }
  }
}

// Responsive toolbar items based on screen width
const vditorToolbar = computed(() => {
  const isWide = windowWidth.value > 1400
  const isMedium = windowWidth.value > 1000
  
  const tableItem = {
    name: 'custom-table', // Use a unique name to avoid Vditor's internal 'table' handler
    tip: 'Insert Table',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"><path d="M448 71.9c0-26.5-21.5-48-48-48H48C21.5 23.9 0 45.4 0 71.9v368.1c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V71.9zM128 416H48v-80h80v80zm0-128H48v-80h80v80zm0-128H48V80h80v80zm144 256h-96v-80h96v80zm0-128h-96v-80h96v80zm0-128h-96V80h96v80zm128 256h-80v-80h80v80zm0-128h-80v-80h80v80zm0-128h-80V80h80v80z"/></svg>',
    click: () => {
      showTableModal.value = true
    }
  }

  if (isWide) {
    // Show almost everything on top
    return [
      'emoji', 'headings', 'bold', 'italic', 'strike', 'link', '|',
      'list', 'ordered-list', 'check', 'outdent', 'indent', '|',
      'quote', 'line', 'code', 'inline-code', 'insert-before', 'insert-after', '|',
      'upload', tableItem, '|',
      'undo', 'redo', '|',
      'edit-mode', 'export', 'fullscreen',
      {
        name: 'more',
        toolbar: []
      }
    ]
  } else if (isMedium) {
    // Move some secondary items to more
    return [
      'headings', 'bold', 'italic', 'link', '|',
      'list', 'ordered-list', '|',
      'upload', tableItem, '|',
      'undo', 'redo', '|',
      'edit-mode',
      {
        name: 'more',
        toolbar: [
          'emoji', 'strike', 'check', 'outdent', 'indent', 'quote', 'line', 
          'code', 'inline-code', 'insert-before', 'insert-after', 'export', '|',
          'fullscreen', 'both'
        ]
      }
    ]
  } else {
    // Compact mode for mobile/small screens
    return [
      'headings', 'bold', 'italic', '|',
      'list', '|',
      'upload', '|',
      'undo', 'redo', '|',
      {
        name: 'more',
        toolbar: [
          'emoji', 'strike', 'link', 'ordered-list', 'check', 'outdent', 'indent', 
          'quote', 'line', 'code', 'inline-code', 'insert-before', 'insert-after', 
          tableItem, 'edit-mode', 'export', '|',
          'fullscreen', 'both'
        ]
      }
    ]
  }
})

// Debounce re-init to avoid flickering during resize
let resizeTimer: any = null
let tocSyncTimer: any = null
watch(vditorToolbar, () => {
  if (!isEditing.value || !vditor.value) return
  
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    const currentVal = vditor.value?.getValue() || content.value
    vditor.value?.destroy()
    initVditorEdit(currentVal)
  }, 500)
})

const toggleEdit = () => {
  if (!isEditing.value) {
    // Starting edit: backup current state
    if (blog.value) {
      originalBlogData.value = {
        title: blog.value.title,
        content: content.value,
        summary: summary.value,
        isPublished: isPublished.value
      }
    }
    isEditing.value = true
  } else {
    // Cancelling edit: restore from backup
    if (originalBlogData.value && blog.value) {
      blog.value.title = originalBlogData.value.title
      content.value = originalBlogData.value.content
      summary.value = originalBlogData.value.summary
      isPublished.value = originalBlogData.value.isPublished
    }
    isEditing.value = false
  }
}
  const isAuthor = computed(() => {
  return userStore.userInfo?.id === blog.value?.authorId
})

const authorProfile = ref<any>(null)

// Theme adaptation helper
const vditorPreviewMode = computed(() => isDark.value ? 'dark' : 'light')
const vditorEditorTheme = computed(() => isDark.value ? 'dark' : 'classic')
const vditorContentTheme = computed(() => isDark.value ? 'dark' : 'light')

// Render TOC from Vditor content
const extractToc = () => {
  const container = isEditing.value ? vditorEditRef.value : vditorPreviewRef.value
  if (!container) return

  // In IR mode, headings are inside the contenteditable area
  // In preview mode, they are inside the preview container
  const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const items: TocItem[] = []
  
  const parentStack: TocItem[] = []

  headings.forEach((heading, index) => {
    // Vditor sometimes uses its own ID, if not, we assign one
    let id = heading.id
    if (!id) {
      id = `heading-${index}`
      heading.id = id
    }
    
    // Clean text: remove the '#' character vditor adds as anchor
    let text = (heading as HTMLElement).innerText.trim()
    if (text.endsWith('#')) text = text.slice(0, -1).trim()
    
    const level = parseInt(heading.tagName.substring(1))
    
    // Find parent
    while (parentStack.length > 0 && parentStack[parentStack.length - 1]!.level >= level) {
      parentStack.pop()
    }
    
    const parentId = parentStack.length > 0 ? parentStack[parentStack.length - 1]!.id : undefined
    
    if (parentStack.length > 0) {
      parentStack[parentStack.length - 1]!.hasChildren = true
    }

    const item: TocItem = {
      id,
      text,
      level,
      parentId,
      hasChildren: false
    }
    
    items.push(item)
    parentStack.push(item)
  })
  tocItems.value = items
}

// Scroll Spy for TOC
const setupScrollSpy = () => {
  if (windowWidth.value <= 1024) return null

  const container = isEditing.value ? vditorEditRef.value : vditorPreviewRef.value
  if (!container) return null

  const headings = Array.from(container.querySelectorAll('h1, h2, h3, h4, h5, h6'))
  if (headings.length === 0) return null

  const observer = new IntersectionObserver((entries) => {
    const visibleHeadings = entries
      .filter(entry => entry.isIntersecting)
      .sort((a, b) => a.target.getBoundingClientRect().top - b.target.getBoundingClientRect().top)

    if (visibleHeadings.length > 0) {
      const firstHeading = visibleHeadings[0]
      if (firstHeading && firstHeading.target) {
        activeTocId.value = firstHeading.target.id
      }
    }
  }, { 
    rootMargin: '-100px 0px -70% 0px', // Match the scroll-margin-top
    threshold: [0, 1] 
  })

  headings.forEach(h => observer.observe(h))
  return observer
}

let scrollSpyObserver: IntersectionObserver | null = null

const scrollToHeading = (id: string) => {
  // Find element within the current active container to be safe
  const container = isEditing.value ? vditorEditRef.value : vditorPreviewRef.value
  if (!container) return

  const element = container.querySelector(`#${id}`) as HTMLElement | null
  if (element) {
    element.scrollIntoView({
      behavior: 'smooth',
      block: 'start'
    })
    
    activeTocId.value = id
  } else {
    // Fallback to global search if container-specific fails
    const globalElement = document.getElementById(id)
    if (globalElement) {
      globalElement.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
      })
      activeTocId.value = id
    }
  }
}

const insertCustomTable = (r?: number, c?: number) => {
  const rows = r || tableRows.value
  const cols = c || tableCols.value
  
  if (rows <= 0 || cols <= 0) return
  
  let tableMd = '\n'
  // Header row
  tableMd += '| ' + Array(cols).fill(' ').join(' | ') + ' |\n'
  // Separator row
  tableMd += '| ' + Array(cols).fill('---').join(' | ') + ' |\n'
  // Data rows
  for (let i = 0; i < rows - 1; i++) {
    tableMd += '| ' + Array(cols).fill(' ').join(' | ') + ' |\n'
  }
  tableMd += '\n'
  
  vditor.value?.insertValue(tableMd)
  showTableModal.value = false
  // Reset
  hoverRows.value = 0
  hoverCols.value = 0
}

// Initialize Vditor for Preview
const initVditorPreview = async (markdown: string) => {
  if (!vditorPreviewRef.value) return
  
  await Vditor.preview(vditorPreviewRef.value, markdown, {
    mode: vditorPreviewMode.value,
    theme: {
      current: vditorContentTheme.value
    },
    hljs: {
      style: isDark.value ? 'dracula' : 'github'
    },
    anchor: 1,
    after: () => {
      nextTick(() => {
        extractToc()
        if (scrollSpyObserver) scrollSpyObserver.disconnect()
        scrollSpyObserver = setupScrollSpy()
      })
    }
  })
}

// Initialize Vditor for Editing
const initVditorEdit = (markdown: string) => {
  if (!vditorEditRef.value) return
  
  vditor.value = new Vditor(vditorEditRef.value, {
    height: editorHeight.value,
    mode: 'ir', // Instant Rendering
    value: markdown,
    theme: vditorEditorTheme.value,
    preview: {
      theme: {
        current: vditorContentTheme.value
      },
      hljs: {
        style: isDark.value ? 'dracula' : 'github'
      }
    },
    toolbar: vditorToolbar.value,
    toolbarConfig: {
      pin: true,
    },
    cache: {
      enable: false
    },
    upload: {
      accept: 'image/*',
      multiple: false,
      handler: async (files: File[]) => {
        const file = files[0]
        if (!file) {
          return 'No file selected'
        }
        try {
          const res = await uploadImage(file)
          // The request util returns res.data directly, so if the API returns { url: '...' } inside data
          const url = typeof res === 'string' ? res : (res.data || res)
          vditor.value?.insertValue(`![${file.name}](${url})`)
          return ''
        } catch (error) {
          message.error('Upload failed')
          return 'Upload failed'
        }
      }
    },
    input: (value) => {
      content.value = value
      clearTimeout(tocSyncTimer)
      tocSyncTimer = setTimeout(() => {
        nextTick(() => {
          extractToc()
          if (scrollSpyObserver) scrollSpyObserver.disconnect()
          scrollSpyObserver = setupScrollSpy()
        })
      }, 180)
    },
    after: () => {
      nextTick(() => {
        extractToc()
        if (scrollSpyObserver) scrollSpyObserver.disconnect()
        scrollSpyObserver = setupScrollSpy()
      })
    }
  })
}

const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return ''
  return DateUtils.isoToDateTime(dateStr)
}

const fetchAuthorProfile = async (uid: number) => {
  try {
    const res = await getProfileByUid(uid)
    authorProfile.value = res.user
    if (res.user.avatar) {
      const blob = await downloadAvatar(res.user.avatar)
      authorAvatar.value = URL.createObjectURL(blob)
    }
  } catch (error) {
    console.error('Failed to fetch author info', error)
  }
}

const fetchBlog = async () => {
  loading.value = true
  try {
    const res = await getBlog(blogUid)
    blog.value = res
    content.value = res.content
    summary.value = res.summary
    isPublished.value = res.isPublished
    document.title = res.title + ' - Faramita BBS'

    // Fetch author info
    if (res.authorId) {
      fetchAuthorProfile(res.authorId)
    }

    // Initialize Vditor Preview
    nextTick(() => {
      initVditorPreview(res.content)
    })
  } catch (error) {
    console.error(error)
    message.error('Failed to load blog')
  } finally {
    loading.value = false
  }
}

// Watchers for Mode and Theme
watch(isEditing, (newVal) => {
  setPageScrollLock(newVal)
  nextTick(() => {
    showTocPanel.value = false
    if (newVal) {
      if (scrollSpyObserver) {
        scrollSpyObserver.disconnect()
        scrollSpyObserver = null
      }
      // Switch to editing
      initVditorEdit(content.value)
    } else {
      // Switch to preview
      initVditorPreview(content.value)
    }
  })
}, { immediate: true })

watch(isDark, () => {
  // Re-initialize to apply theme
  if (isEditing.value) {
    initVditorEdit(content.value)
  } else {
    initVditorPreview(content.value)
  }
})

const handleSave = async () => {
  if (!blog.value) return

  const updateData: BlogUpdateDTO = {
    title: blog.value.title,
    content: content.value,
    summary: summary.value,
    littleCategoryName: blog.value.littleCategoryName,
    bigCategoryId: blog.value.bigCategoryId,
    isPublished: isPublished.value
  }

  try {
    await updateBlog(blog.value.bloguid, updateData)
    message.success('Saved successfully')
    isEditing.value = false
    // Update local state
    blog.value.content = content.value
    blog.value.summary = summary.value
    blog.value.isPublished = isPublished.value
  } catch (error) {
    message.error('Save failed')
  }
}

const handleDelete = async () => {
  if (!blog.value) return
  try {
    await deleteBlog(blog.value.bloguid)
    message.success('Deleted successfully')
    router.push('/blog')
  } catch (error) {
    message.error('Delete failed')
  }
}

onMounted(() => {
  window.scrollTo(0, 0)
  window.addEventListener('resize', handleResize)
  fetchBlog()
})

onUnmounted(() => {
  setPageScrollLock(false)
  window.removeEventListener('resize', handleResize)
  clearTimeout(tocSyncTimer)
  clearTimeout(resizeTimer)
  if (scrollSpyObserver) {
    scrollSpyObserver.disconnect()
  }
  if (vditor.value) {
    vditor.value.destroy()
  }
})
</script>

<template>
  <div class="blog-detail-page" :class="{ 'editing-no-scroll': isEditing }">
    <n-spin :show="loading">
      <div v-if="blog" class="blog-container" :class="{ 'editing-layout': isEditing }">
        <!-- Left Sidebar: Blog Info / TOC -->
        <div class="left-sidebar">
          <div class="fixed-sidebar-wrapper">
            <aside class="side-col side-stack">
              <div class="side-panel-wrap">
                <template v-if="!isEditing">
                  <div class="author-wrapper" style="margin-bottom: 20px;">
                    <n-card class="author-card" :bordered="false">
                      <div class="author-info">
                        <div class="author-avatar-wrapper">
                          <n-avatar 
                            round 
                            :size="80" 
                            :src="authorAvatar" 
                            :render-icon="renderDefaultAvatar"
                            class="author-avatar-lg" 
                          />
                        </div>
                        <div class="author-details">
                            <h3 class="author-name-lg">{{ blog.authorName }}</h3>
                            <!-- TODO 待开发功能：作者卡片详情 -->
                            <!-- <p class="author-bio">{{ authorProfile?.remark || 'No bio available.' }}</p> -->
                        </div>
                        <button class="visit-btn" @click="router.push(`/${blog.authorId}`)">
                          View Profile
                        </button>
                      </div>
                    </n-card>
                  </div>

                  <div class="toc-wrapper">
                    <div class="toc-container">
                      <div class="toc-header">
                        <n-icon class="toc-icon">
                          <ListOutline />
                        </n-icon>
                        <span class="toc-title">Contents</span>
                      </div>
                      <div class="toc-list">
                        <div
                          v-for="item in visibleTocItems"
                          :key="item.id"
                          class="toc-item"
                          :class="[`toc-level-${item.level}`, { 'toc-active': activeTocId === item.id }]"
                          @click="scrollToHeading(item.id)"
                        >
                          <span class="toc-text">{{ item.text }}</span>
                          <span 
                            v-if="item.hasChildren" 
                            class="toc-expand-icon" 
                            @click="toggleTocExpand(item.id, $event)"
                          >
                            {{ expandedTocIds.has(item.id) ? '-' : '+' }}
                          </span>
                        </div>
                        <div v-if="tocItems.length === 0" class="toc-empty">No contents</div>
                      </div>
                    </div>
                  </div>
                </template>

                <template v-else>
                  <transition name="panel-slide" mode="out-in">
                    <div v-if="showTocPanel" class="toc-wrapper" key="toc">
                      <div class="toc-container">
                        <div class="toc-header">
                          <n-icon class="toc-icon">
                            <ListOutline />
                          </n-icon>
                          <span class="toc-title">Contents</span>
                        </div>
                        <div class="toc-list">
                          <div
                            v-for="item in visibleTocItems"
                            :key="item.id"
                            class="toc-item"
                            :class="[`toc-level-${item.level}`, { 'toc-active': activeTocId === item.id }]"
                            @click="scrollToHeading(item.id)"
                          >
                            <span class="toc-text">{{ item.text }}</span>
                            <span 
                              v-if="item.hasChildren" 
                              class="toc-expand-icon" 
                              @click="toggleTocExpand(item.id, $event)"
                            >
                              {{ expandedTocIds.has(item.id) ? '-' : '+' }}
                            </span>
                          </div>
                          <div v-if="tocItems.length === 0" class="toc-empty">No contents</div>
                        </div>
                      </div>
                    </div>

                    <n-card v-else class="edit-side-card blog-info-card" :bordered="false" key="info">
                      <div class="edit-side-header">Article Info</div>
                      <n-space vertical :size="12">
                        <div class="field-label">Title</div>
                        <n-input
                          v-model:value="blog.title"
                          type="text"
                          placeholder="Enter title"
                          class="edit-title-input custom-input"
                          size="large"
                        />

                        <div class="field-label">Summary</div>
                        <n-input
                          v-model:value="summary"
                          type="textarea"
                          placeholder="Enter summary"
                          :autosize="{ minRows: 3, maxRows: 6 }"
                          class="custom-input"
                        />

                        <div class="field-label">Status</div>
                        <div class="publish-row">
                          <span>{{ isPublished === 1 ? 'Public' : 'Private' }}</span>
                          <n-switch v-model:value="isPublished" :checked-value="1" :unchecked-value="0" />
                        </div>

                        <div class="action-group">
                          <button class="save-btn" @click="handleSave">Save</button>
                          <div class="action-subrow">
                            <button class="cancel-btn" @click="toggleEdit">Cancel</button>
                            <n-popconfirm @positive-click="handleDelete">
                              <template #trigger>
                                <button class="delete-btn">Delete</button>
                              </template>
                              Are you sure you want to delete this article?
                            </n-popconfirm>
                          </div>
                        </div>
                      </n-space>
                    </n-card>
                  </transition>
                </template>
              </div>
            </aside>
          </div>
        </div>

        <button v-if="isEditing" class="edge-toggle-tag" type="button" @click="toggleSidePanel">
          {{ sidePanelToggleText }}
        </button>

        <!-- Center: Content -->
        <main class="main-col">
          <n-card v-if="!isEditing" class="blog-card" :bordered="false">
            <div class="blog-header">
              <h1 class="blog-title">{{ blog.title }}</h1>

              <div class="blog-display-meta">
                 <div class="blog-meta-row">
                    <n-space align="center" size="small">
                      <span class="category-badge">{{ blog.littleCategoryName }}</span>
                      <span class="meta-divider">|</span>
                      <span class="date">Published {{ formatDate(blog.createTime) }}</span>
                      <template v-if="blog.updateTime && blog.updateTime !== blog.createTime">
                          <span class="meta-divider">|</span>
                          <span class="date">Updated {{ formatDate(blog.updateTime) }}</span>
                      </template>
                    </n-space>
                 </div>

                 <div class="blog-summary-section" v-if="blog.summary">
                    <p class="blog-summary-text">{{ blog.summary }}</p>
                 </div>
              </div>

              <div class="blog-actions">
                <n-space align="center" justify="end">
                  <template v-if="isAuthor">
                    <button class="edit-btn" @click="toggleEdit">Edit</button>
                  </template>
                </n-space>
              </div>
            </div>

            <div class="blog-content">
              <!-- Vditor Containers -->
              <div ref="vditorPreviewRef" class="vditor-preview-container"></div>
            </div>
          </n-card>

          <n-card v-else class="editor-only-card" :bordered="false">
            <div class="blog-content editing-mode">
              <div ref="vditorEditRef" class="vditor-edit-container"></div>
            </div>
          </n-card>
        </main>

        <!-- Table Size Selector Modal (Checkerboard Style) -->
        <n-modal
          v-model:show="showTableModal"
          preset="card"
          title="Insert Table"
          style="width: 320px"
          :bordered="false"
          class="table-grid-modal"
        >
          <div class="table-grid-container">
            <div class="table-grid-info">
              {{ hoverRows > 0 ? `${hoverRows} x ${hoverCols}` : 'Select size' }}
            </div>
            <div 
              class="table-grid-cells"
              @mouseleave="hoverRows = 0; hoverCols = 0"
            >
              <div v-for="r in 10" :key="`row-${r}`" class="grid-row">
                <div 
                  v-for="c in 10" 
                  :key="`cell-${r}-${c}`" 
                  class="grid-cell"
                  :class="{ 'active': r <= hoverRows && c <= hoverCols }"
                  @mouseenter="hoverRows = r; hoverCols = c"
                  @click="insertCustomTable(r, c)"
                ></div>
              </div>
            </div>
          </div>
        </n-modal>
      </div>
    </n-spin>
  </div>
</template>

<style scoped>
/* ==================== 页面基础 ==================== */
.blog-detail-page {
  min-height: 100vh;
  padding-top: 40px;
  background: var(--bg-primary);
  position: relative;
  overflow-x: hidden;
}

.blog-detail-page.editing-no-scroll {
  height: calc(100vh - 40px);
  overflow: hidden;
}

.blog-detail-page.editing-no-scroll .blog-container {
  height: calc(100vh - 56px);
}

.blog-detail-page.editing-no-scroll .main-col,
.blog-detail-page.editing-no-scroll .editor-only-card,
.blog-detail-page.editing-no-scroll .blog-content.editing-mode {
  height: 100%;
}

/* New layout using flexbox */
.blog-container {
  display: flex;
  gap: 40px;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 40px;
  position: relative;
  z-index: 1;
}

.left-sidebar {
  width: 280px;
  min-width: 280px;
  max-width: 280px;
  flex-shrink: 0;
}

.fixed-sidebar-wrapper {
  position: fixed;
  top: 100px;
  width: 280px;
  height: calc(100vh - 120px);
  z-index: 10;
}

.side-col {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.side-col::-webkit-scrollbar {
  display: none;
}

.side-stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.main-col {
  width: auto;
  flex-grow: 1;
  max-width: none;
  min-width: 0;
  margin: 0;
}

.blog-container.editing-layout .main-col {
  width: calc(100% - 320px);
}

.side-panel-wrap {
  position: relative;
  width: 100%;
  min-width: 0;
}

.edge-toggle-tag {
  position: fixed;
  left: 0;
  top: 50%;
  transform: translate(-68%, -50%);
  height: 112px;
  width: 42px;
  border-radius: 0;
  border: 1px solid var(--text-primary);
  border-left: none;
  background: var(--text-primary);
  color: var(--bg-primary);
  font-family: 'Lato', sans-serif;
  font-size: 0.86rem;
  font-weight: 700;
  letter-spacing: 1px;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 40;
}

.edge-toggle-tag:hover {
  transform: translate(0, -50%);
  background: var(--accent-color);
  border-color: var(--accent-color);
}

.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: all 0.3s ease;
}

.panel-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.panel-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* Mobile Adaptation */
@media (max-width: 1024px) {
  .blog-container {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
    padding: 0 16px 20px;
  }

  .left-sidebar {
    display: block;
    width: 100%;
    max-width: 100%;
    min-width: unset;
    order: 1;
  }

  .fixed-sidebar-wrapper {
    position: static;
    width: 100%;
    height: auto;
  }

  .edge-toggle-tag {
    top: auto;
    bottom: 100px;
    transform: translateX(-66%);
    height: 94px;
    width: 38px;
    font-size: 0.8rem;
  }

  .edge-toggle-tag:hover {
    transform: translateX(0);
  }

  .side-col {
    max-height: none;
    overflow: visible;
  }

  .main-col {
    width: 100%;
    order: 2;
    margin: 0;
  }

  .blog-container.editing-layout .main-col {
    width: 100%;
  }
}

/* ==================== 目录卡片 ==================== */
.toc-wrapper {
  width: 100%;
  min-width: 0;
}

.toc-container {
  width: 100%;
  box-sizing: border-box;
  padding: 24px;
  background: var(--bg-primary);
  border: 1px solid var(--line-color);
}

/* Custom TOC styles */
.toc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toc-item {
  padding: 4px 0;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
  font-family: 'Lato', sans-serif;
  font-size: 0.9rem;
  line-height: 1.5;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  word-break: break-word;
}

.toc-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.toc-expand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  margin-left: 8px;
  font-size: 1.2rem;
  color: var(--text-tertiary);
  transition: color 0.3s;
  flex-shrink: 0;
}

.toc-expand-icon:hover {
  color: var(--accent-color);
}

.toc-item:hover {
  color: var(--text-primary);
  transform: translateX(4px);
}

.toc-active {
  color: var(--text-primary) !important;
  font-weight: 700;
}

.toc-active::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background: var(--accent-color);
  border-radius: 50%;
}

html {
  scroll-behavior: smooth;
}

/* Level-based indentation */
.toc-level-1 {
  font-weight: 700;
  font-size: 1rem;
  margin-top: 12px;
  color: var(--text-primary);
}

.toc-level-1:first-child {
  margin-top: 0;
}

.toc-level-2 { padding-left: 16px; }
.toc-level-3 { padding-left: 32px; }
.toc-level-4 { padding-left: 48px; }
.toc-level-5 { padding-left: 64px; }
.toc-level-6 { padding-left: 80px; }

.toc-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--line-color);
}

.toc-icon {
  font-size: 1.2rem;
  color: var(--text-primary);
}

.toc-title {
  font-family: 'Playfair Display', serif;
  font-weight: 700;
  font-size: 1.2rem;
  color: var(--text-primary);
  letter-spacing: 1px;
}

/* ==================== 博客主卡片 ==================== */
.blog-card {
  border-radius: 0;
  min-height: 80vh;
  padding: 40px 50px;
  background: var(--bg-primary);
  border: 1px solid var(--line-color);
  z-index: 2;
}

.edit-side-card {
  width: 100%;
  box-sizing: border-box;
  border-radius: 0;
  background: var(--bg-primary);
  border: 1px solid var(--line-color);
  padding: 24px;
}

.edit-side-header {
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 20px;
  letter-spacing: 1px;
  color: var(--text-primary);
  border-bottom: 1px solid var(--line-color);
  padding-bottom: 15px;
}

.field-label {
  font-family: 'Lato', sans-serif;
  font-size: 0.85rem;
  color: var(--text-secondary);
  font-weight: 700;
  margin-bottom: 5px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.custom-input :deep(.n-input__input-el),
.custom-input :deep(.n-input__textarea-el) {
  font-family: 'Lato', sans-serif;
  font-size: 1rem;
  color: var(--text-primary);
}

.custom-input :deep(.n-input__border),
.custom-input :deep(.n-input__state-border) {
  border: 1px solid var(--line-color);
  border-radius: 0;
}

.custom-input {
  background: transparent;
}

.publish-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-family: 'Lato', sans-serif;
  font-size: 0.9rem;
  padding: 10px 0;
  color: var(--text-primary);
}

.action-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.action-subrow {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.save-btn, .cancel-btn, .delete-btn {
  font-family: 'Lato', sans-serif;
  font-weight: 700;
  letter-spacing: 1px;
  padding: 12px 0;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  text-transform: uppercase;
  font-size: 0.85rem;
}

.save-btn {
  background: var(--text-primary);
  color: var(--bg-primary);
}

.save-btn:hover {
  background: var(--accent-color);
}

.cancel-btn {
  background: transparent;
  border: 1px solid var(--line-color);
  color: var(--text-primary);
}

.cancel-btn:hover {
  border-color: var(--text-primary);
}

.delete-btn {
  background: transparent;
  border: 1px solid #ef4444;
  color: #ef4444;
}

.delete-btn:hover {
  background: #ef4444;
  color: white;
}

.editor-only-card {
  border-radius: 0;
  min-height: calc(100vh - 120px);
  padding: 20px;
  background: var(--bg-primary);
  border: 1px solid var(--line-color);
}

.author-wrapper {
  width: 100%;
}

.author-card {
  border-radius: 0;
  text-align: center;
  background: var(--bg-primary);
  border: 1px solid var(--line-color);
  padding: 30px 20px;
}

.author-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.author-avatar-lg {
  border: 1px solid var(--line-color);
}

.author-name-lg {
  font-family: 'Playfair Display', serif;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 10px;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.author-bio {
  font-family: 'Lato', sans-serif;
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 0 0 20px;
  line-height: 1.6;
  font-style: italic;
}

.author-stats {
  width: 100%;
  padding: 15px 0;
  border-top: 1px solid var(--line-color);
  border-bottom: 1px solid var(--line-color);
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-val {
  font-family: 'Playfair Display', serif;
  font-weight: 700;
  font-size: 1.8rem;
  color: var(--text-primary);
}

.stat-label {
  font-family: 'Lato', sans-serif;
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 5px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.visit-btn {
  background: transparent;
  border: 1px solid var(--text-primary);
  color: var(--text-primary);
  font-family: 'Lato', sans-serif;
  font-weight: 700;
  letter-spacing: 1px;
  padding: 10px 0;
  width: 100%;
  cursor: pointer;
  transition: all 0.3s;
  text-transform: uppercase;
  font-size: 0.85rem;
}

.visit-btn:hover {
  background: var(--text-primary);
  color: var(--bg-primary);
}

/* ==================== 博客内容区域 ==================== */
.blog-header {
  margin-bottom: 40px;
  text-align: center;
}

.blog-title {
  font-family: 'Playfair Display', serif;
  font-size: 3rem;
  margin-bottom: 20px;
  line-height: 1.2;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.blog-display-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.blog-meta-row {
  font-family: 'Lato', sans-serif;
  font-size: 0.9rem;
  color: var(--text-secondary);
  letter-spacing: 1px;
}

.category-badge {
  font-weight: 700;
  color: var(--text-primary);
  text-transform: uppercase;
}

.meta-divider {
  color: var(--line-color);
}

.blog-summary-section {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 0;
  border-top: 1px solid var(--line-color);
  border-bottom: 1px solid var(--line-color);
}

.blog-summary-text {
  margin: 0;
  font-family: 'Lato', sans-serif;
  font-size: 1.1rem;
  color: var(--text-secondary);
  line-height: 1.8;
  font-style: italic;
}

.edit-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-family: 'Lato', sans-serif;
  font-weight: 700;
  letter-spacing: 1px;
  cursor: pointer;
  transition: color 0.3s;
  text-transform: uppercase;
  font-size: 0.85rem;
  text-decoration: underline;
}

.edit-btn:hover {
  color: var(--text-primary);
}

/* Vditor Styles Overrides */
.vditor-preview-container, .vditor-edit-container {
  width: 100%;
  min-height: 420px;
}

.editing-mode .vditor-edit-container {
  min-height: calc(100vh - 220px);
}

:deep(.vditor) {
  border: none !important;
  background-color: transparent !important;
}

:deep(.vditor-toolbar) {
  border-bottom: 1px solid var(--line-color) !important;
  background-color: var(--bg-primary) !important;
  position: sticky;
  top: 0;
  z-index: 100;
}

:deep(.vditor-content) {
  background-color: transparent !important;
}

:deep(.vditor-reset) {
  font-family: 'Lato', sans-serif !important;
  color: var(--text-primary) !important;
  padding: 0 !important;
  font-size: 1.1rem !important;
  line-height: 1.8 !important;
}

:deep(.vditor-reset h1),
:deep(.vditor-reset h2),
:deep(.vditor-reset h3),
:deep(.vditor-reset h4),
:deep(.vditor-reset h5),
:deep(.vditor-reset h6) {
  font-family: 'Playfair Display', serif !important;
  margin-top: 2em !important;
  margin-bottom: 1em !important;
  font-weight: 700 !important;
}

:deep(.vditor-reset a) {
  color: var(--accent-color) !important;
  text-decoration: none !important;
  border-bottom: 1px solid var(--accent-color) !important;
}

:deep(.vditor-reset blockquote) {
  border-left: 4px solid var(--text-primary) !important;
  color: var(--text-secondary) !important;
  background: transparent !important;
  font-style: italic !important;
  padding: 10px 20px !important;
  margin: 20px 0 !important;
}

/* Fix for TOC anchors - ensure headings have margin for fixed header */
:deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
  scroll-margin-top: 120px;
}

@media (max-width: 1024px) {
  .blog-card,
  .editor-only-card {
    padding: 20px;
  }

  .blog-title {
    font-size: 2rem;
  }

  .editing-mode .vditor-edit-container {
    min-height: 58vh;
  }
}

.toc-empty {
  text-align: center;
  color: var(--text-tertiary);
  padding: 20px 0;
  font-family: 'Lato', sans-serif;
  font-size: 0.9rem;
  font-style: italic;
}

/* Table Grid Selector Styles */
.table-grid-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.table-grid-info {
  margin-bottom: 15px;
  font-family: 'Lato', sans-serif;
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-weight: 700;
}

.table-grid-cells {
  border: 1px solid var(--line-color);
  padding: 2px;
  background: var(--bg-primary);
  display: inline-block;
}

.grid-row {
  display: flex;
}

.grid-cell {
  width: 22px;
  height: 22px;
  border: 1px solid var(--line-color);
  margin: 1px;
  cursor: pointer;
  transition: all 0.1s;
}

.grid-cell:hover {
  transform: scale(1.1);
  z-index: 2;
}

.grid-cell.active {
  background-color: var(--text-primary);
  border-color: var(--text-primary);
}
</style>
