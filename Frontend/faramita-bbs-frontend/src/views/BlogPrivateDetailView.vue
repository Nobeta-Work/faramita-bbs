<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Vditor from 'vditor'
import 'vditor/dist/index.css'
import {
  NButton,
  NEmpty,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NModal,
  NSelect,
  NSpin,
  NSwitch,
  NTag,
  NTreeSelect,
  useDialog,
  useMessage,
} from 'naive-ui'
import {
  ArrowBackOutline,
  ArrowUpOutline,
  CheckmarkCircleOutline,
  CloseCircleOutline,
  FolderOpenOutline,
  PricetagOutline,
  SaveOutline,
  TrashOutline,
} from '@vicons/ionicons5'
import { deletePrivateBlog, getPrivateBlog, updatePrivateBlog } from '@/api/blog'
import { getCurrentUserFolderTree } from '@/api/folder'
import { createTag, getTagPage } from '@/api/tag'
import { uploadImage } from '@/api/file'
import { useToc } from '@/composables/useToc'
import { useThemeStore } from '@/stores/theme'
import type { ApiId, BlogPrivateDetailVO, FolderTree, PublishStatus, TagBriefVO } from '@/types'
import { ROOT_FOLDER_ID } from '@/types'
import { decorateMarkdownContent } from '@/utils/markdown'
import { storeToRefs } from 'pinia'

interface SelectOption {
  [key: string]: unknown
  label: string
  value: string
}

interface FolderOption {
  [key: string]: unknown
  label: string
  key: string
  children?: FolderOption[]
}

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)
const {
  activeTocId,
  cleanupToc,
  expandedTocIds,
  extractToc,
  scrollToHeading,
  setupScrollSpy,
  tocItems,
  toggleTocExpand,
  visibleTocItems,
} = useToc()

const blogId = computed(() => String(route.params.id || ''))
const loading = ref(false)
const saving = ref(false)
const dirty = ref(false)
const loadError = ref(false)
const editorRef = ref<HTMLElement | null>(null)
const editorSurfaceRef = ref<HTMLDivElement | null>(null)
const vditor = ref<Vditor | null>(null)
const showTableModal = ref(false)
const hoverRows = ref(0)
const hoverCols = ref(0)
const tagOptions = ref<SelectOption[]>([])
const folderTree = ref<FolderTree | null>(null)
const editorCleanup = ref<(() => void) | null>(null)
const showBackTop = ref(false)
let tocTimer: number | undefined
let contentInitialized = false
let scrollTarget: HTMLElement | Window | null = null

const form = reactive({
  title: '',
  summary: '',
  content: '',
  folderId: String(ROOT_FOLDER_ID),
  isPublished: 0 as PublishStatus,
  tagIds: [] as string[],
})

const tableHoverState = reactive({
  visible: false,
  left: 0,
  top: 0,
  cell: null as HTMLTableCellElement | null,
})

const wordCount = computed(() => form.content.replace(/\s+/g, '').length)
const saveStateText = computed(() => {
  if (saving.value) {
    return 'Saving'
  }
  return dirty.value ? 'Unsaved' : 'Saved'
})
const saveStateType = computed(() => (dirty.value ? 'warning' : 'success'))
const folderOptions = computed<FolderOption[]>(() => [
  {
    label: '根目录',
    key: String(ROOT_FOLDER_ID),
    children: folderTree.value?.children?.map(toFolderOption) ?? [],
  },
])

function toFolderOption(folder: FolderTree): FolderOption {
  return {
    label: String(folder.id) === String(ROOT_FOLDER_ID) ? '根目录' : folder.name,
    key: String(folder.id),
    children: folder.children?.map(toFolderOption),
  }
}

function setFormFromBlog(blog: BlogPrivateDetailVO): void {
  contentInitialized = false
  form.title = blog.title
  form.summary = blog.summary || ''
  form.content = blog.content || ''
  form.folderId = String(blog.folderId ?? ROOT_FOLDER_ID)
  form.isPublished = blog.isPublished
  form.tagIds = blog.tags.map((tag) => String(tag.id))
  tagOptions.value = mergeTagOptions(tagOptions.value, blog.tags.map((tag) => ({ label: tag.name, value: String(tag.id) })))
  nextTick(() => {
    dirty.value = false
    contentInitialized = true
  })
}

function mergeTagOptions(current: SelectOption[], incoming: SelectOption[]): SelectOption[] {
  const map = new Map(current.map((option) => [option.value, option]))
  incoming.forEach((option) => map.set(option.value, option))
  return Array.from(map.values())
}

function createTagOption(label: string): SelectOption {
  const name = label.trim().slice(0, 20)
  return { label: name, value: name }
}

async function loadPage(): Promise<void> {
  loading.value = true
  loadError.value = false
  try {
    const [blog, tree] = await Promise.all([
      getPrivateBlog(blogId.value),
      getCurrentUserFolderTree(),
    ])
    folderTree.value = tree
    setFormFromBlog(blog)
    document.title = `${blog.title} | 编辑博客`
    await nextTick()
    initEditor(form.content)
    await handleTagSearch('')
  } catch (error) {
    loadError.value = true
    message.error('编辑内容加载失败')
  } finally {
    loading.value = false
  }
}

function destroyEditor(): void {
  resetEditorToolbarPosition()
  editorCleanup.value?.()
  editorCleanup.value = null
  if (vditor.value) {
    vditor.value.destroy()
    vditor.value = null
  }
}

function initEditor(markdown: string): void {
  if (!editorRef.value) {
    return
  }

  destroyEditor()
  vditor.value = new Vditor(editorRef.value, {
    height: 'auto',
    mode: 'ir',
    value: markdown,
    theme: isDark.value ? 'dark' : 'classic',
    toolbarConfig: {
      pin: true,
    },
    cache: {
      enable: false,
    },
    toolbar: [
      'headings', 'bold', 'italic', 'strike', 'link', '|',
      'list', 'ordered-list', 'check', 'quote', 'line', 'code', 'inline-code', '|',
      'upload',
      {
        name: 'table-grid',
        tip: 'Insert Table',
        icon: '<svg viewBox="0 0 24 24"><path d="M3 3h18v18H3V3Zm2 2v4h4V5H5Zm6 0v4h8V5h-8ZM5 11v8h4v-8H5Zm6 0v8h8v-8h-8Z"/></svg>',
        click: () => {
          showTableModal.value = true
        },
      },
      '|', 'undo', 'redo', '|', 'fullscreen',
    ],
    preview: {
      theme: {
        current: isDark.value ? 'dark' : 'light',
      },
      hljs: {
        style: isDark.value ? 'dracula' : 'github',
      },
      parse: decorateMarkdownContent,
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
          const url = typeof res === 'string' ? res : res.data
          vditor.value?.insertValue(`![${file.name}](${url})`)
          return ''
        } catch (error) {
          message.error('图片上传失败')
          return 'Upload failed'
        }
      },
    },
    input: (value) => {
      form.content = value
      dirty.value = true
      scheduleTocSync()
    },
    after: () => {
      nextTick(() => {
        bindEditorEnhancements()
        scheduleTocSync()
        syncEditorToolbarPosition()
      })
    },
  })
}

function scheduleTocSync(): void {
  window.clearTimeout(tocTimer)
  tocTimer = window.setTimeout(() => {
    if (!editorRef.value) {
      return
    }

    extractToc(editorRef.value)
    setupScrollSpy(editorRef.value)
    decorateMarkdownContent(editorRef.value)
  }, 180)
}

function syncEditorContent(): void {
  if (vditor.value) {
    form.content = vditor.value.getValue()
    dirty.value = true
    scheduleTocSync()
  }
}

function bindEditorEnhancements(): void {
  const editor = editorRef.value
  const surface = editorSurfaceRef.value
  if (!editor || !surface) {
    return
  }

  editorCleanup.value?.()

  const keydown = (event: KeyboardEvent) => {
    if (handleTabKey(event)) {
      return
    }
    handlePairKey(event)
  }
  const mousemove = (event: MouseEvent) => {
    updateTableHover(event)
  }
  const mouseleave = () => {
    tableHoverState.visible = false
    tableHoverState.cell = null
  }
  const click = (event: MouseEvent) => {
    if ((event.target as HTMLElement | null)?.closest('.vditor-toolbar')) {
      window.setTimeout(syncEditorToolbarPosition, 0)
    }
  }
  const dblclick = (event: MouseEvent) => {
    const heading = (event.target as HTMLElement | null)?.closest('h1, h2, h3, h4, h5, h6') as HTMLElement | null
    if (heading && editor.contains(heading)) {
      toggleHeadingFold(heading)
    }
  }

  editor.addEventListener('keydown', keydown, true)
  editor.addEventListener('click', click)
  editor.addEventListener('dblclick', dblclick)
  surface.addEventListener('mousemove', mousemove)
  surface.addEventListener('mouseleave', mouseleave)

  editorCleanup.value = () => {
    editor.removeEventListener('keydown', keydown, true)
    editor.removeEventListener('click', click)
    editor.removeEventListener('dblclick', dblclick)
    surface.removeEventListener('mousemove', mousemove)
    surface.removeEventListener('mouseleave', mouseleave)
  }
}

function handleTabKey(event: KeyboardEvent): boolean {
  if (event.key !== 'Tab' || event.isComposing || event.ctrlKey || event.metaKey || event.altKey) {
    return false
  }

  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0 || !editorRef.value?.contains(selection.anchorNode)) {
    return false
  }

  event.preventDefault()
  vditor.value?.insertValue('    ')
  window.setTimeout(syncEditorContent, 0)
  return true
}

function handlePairKey(event: KeyboardEvent): void {
  if (event.isComposing || event.ctrlKey || event.metaKey || event.altKey) {
    return
  }

  const pairs: Record<string, string> = {
    '(': ')',
    '[': ']',
    '{': '}',
    '`': '`',
  }
  const closing = Object.values(pairs)
  const selection = window.getSelection()
  if (!selection || selection.rangeCount === 0 || !editorRef.value?.contains(selection.anchorNode)) {
    return
  }

  const range = selection.getRangeAt(0)
  const open = event.key
  if (pairs[open]) {
    event.preventDefault()
    const selected = range.toString()
    const text = `${open}${selected}${pairs[open]}`
    range.deleteContents()
    const node = document.createTextNode(text)
    range.insertNode(node)
    const nextRange = document.createRange()
    const cursorOffset = selected ? text.length : 1
    nextRange.setStart(node, cursorOffset)
    nextRange.collapse(true)
    selection.removeAllRanges()
    selection.addRange(nextRange)
    window.setTimeout(syncEditorContent, 0)
    return
  }

  if (!closing.includes(event.key) || !range.collapsed || range.startContainer.nodeType !== Node.TEXT_NODE) {
    return
  }

  const text = range.startContainer.textContent || ''
  if (text[range.startOffset] === event.key) {
    event.preventDefault()
    range.setStart(range.startContainer, range.startOffset + 1)
    range.collapse(true)
    selection.removeAllRanges()
    selection.addRange(range)
  }
}

function resolveScrollTarget(): HTMLElement | Window {
  return document.querySelector<HTMLElement>('.main-layout .n-layout-scroll-container')
    ?? document.querySelector<HTMLElement>('.n-layout-scroll-container')
    ?? window
}

function getScrollTop(target = resolveScrollTarget()): number {
  return target instanceof Window ? window.scrollY : target.scrollTop
}

function updateBackTopVisibility(): void {
  showBackTop.value = getScrollTop() > 360
}

function getToolbarTopOffset(): number {
  return window.matchMedia('(max-width: 720px)').matches ? 70 : 76
}

function resetEditorToolbarPosition(): void {
  const toolbar = editorRef.value?.querySelector<HTMLElement>('.vditor-toolbar')
  toolbar?.classList.remove('vditor-toolbar--page-fixed')
  toolbar?.style.removeProperty('top')
  toolbar?.style.removeProperty('left')
  toolbar?.style.removeProperty('width')
  editorSurfaceRef.value?.classList.remove('editor-card--toolbar-fixed')
  editorSurfaceRef.value?.style.removeProperty('--editor-toolbar-height')
}

function syncEditorToolbarPosition(): void {
  const surface = editorSurfaceRef.value
  const editor = editorRef.value
  const toolbar = editor?.querySelector<HTMLElement>('.vditor-toolbar')
  if (!surface || !editor || !toolbar) {
    resetEditorToolbarPosition()
    return
  }
  if (editor.querySelector('.vditor--fullscreen')) {
    resetEditorToolbarPosition()
    return
  }

  const top = getToolbarTopOffset()
  const editorRect = editor.getBoundingClientRect()
  const surfaceRect = surface.getBoundingClientRect()
  const toolbarHeight = toolbar.offsetHeight || 36
  const shouldFix = editorRect.top <= top && surfaceRect.bottom > top + toolbarHeight + 24

  if (!shouldFix) {
    resetEditorToolbarPosition()
    return
  }

  toolbar.classList.add('vditor-toolbar--page-fixed')
  surface.classList.add('editor-card--toolbar-fixed')
  surface.style.setProperty('--editor-toolbar-height', `${toolbarHeight}px`)
  toolbar.style.top = `${top}px`
  toolbar.style.left = `${editorRect.left}px`
  toolbar.style.width = `${editorRect.width}px`
}

function updateScrollEffects(): void {
  updateBackTopVisibility()
  syncEditorToolbarPosition()
}

function scrollToTop(): void {
  const target = scrollTarget ?? resolveScrollTarget()
  target.scrollTo({ top: 0, behavior: 'smooth' })
}

function updateTableHover(event: MouseEvent): void {
  const surface = editorSurfaceRef.value
  if (!surface) {
    return
  }

  const target = event.target as HTMLElement | null
  if (target?.closest('.table-action-popover')) {
    return
  }

  const cell = target?.closest('td, th') as HTMLTableCellElement | null
  if (!cell || !surface.contains(cell)) {
    tableHoverState.visible = false
    tableHoverState.cell = null
    return
  }

  const cellRect = cell.getBoundingClientRect()
  const surfaceRect = surface.getBoundingClientRect()
  tableHoverState.visible = true
  tableHoverState.cell = cell
  tableHoverState.left = Math.min(surfaceRect.width - 120, Math.max(12, cellRect.right - surfaceRect.left - 92))
  tableHoverState.top = Math.max(12, cellRect.top - surfaceRect.top - 42)
}

function insertTableColumn(): void {
  const cell = tableHoverState.cell
  const row = cell?.parentElement as HTMLTableRowElement | null
  const table = cell?.closest('table')
  if (!cell || !row || !table) {
    return
  }

  const index = Array.from(row.children).indexOf(cell)
  Array.from(table.rows).forEach((tableRow, rowIndex) => {
    const targetCell = tableRow.cells[index]
    targetCell?.insertAdjacentHTML('afterend', rowIndex === 0 ? '<th> </th>' : '<td> </td>')
  })
  syncEditorContent()
}

function insertTableRow(): void {
  const cell = tableHoverState.cell
  const row = cell?.parentElement as HTMLTableRowElement | null
  if (!cell || !row) {
    return
  }

  const rowHTML = Array.from({ length: row.children.length })
    .map(() => (cell.tagName === 'TH' ? '<th> </th>' : '<td> </td>'))
    .join('')
  row.insertAdjacentHTML('afterend', `<tr>${rowHTML}</tr>`)
  syncEditorContent()
}

function deleteTableColumn(): void {
  const cell = tableHoverState.cell
  const row = cell?.parentElement as HTMLTableRowElement | null
  const table = cell?.closest('table')
  if (!cell || !row || !table) {
    return
  }

  const index = Array.from(row.children).indexOf(cell)
  Array.from(table.rows).forEach((tableRow) => {
    tableRow.cells[index]?.remove()
  })
  syncEditorContent()
}

function deleteTableRow(): void {
  const row = tableHoverState.cell?.parentElement as HTMLTableRowElement | null
  row?.remove()
  syncEditorContent()
}

function toggleHeadingFold(heading: HTMLElement): void {
  const level = Number(heading.tagName.substring(1))
  const hidden = heading.dataset.folded !== 'true'
  heading.dataset.folded = String(hidden)
  heading.classList.toggle('heading-folded', hidden)

  let sibling = heading.nextElementSibling as HTMLElement | null
  while (sibling) {
    if (/^H[1-6]$/.test(sibling.tagName) && Number(sibling.tagName.substring(1)) <= level) {
      break
    }

    sibling.style.display = hidden ? 'none' : ''
    sibling = sibling.nextElementSibling as HTMLElement | null
  }
}

function insertCustomTable(rows: number, cols: number): void {
  let markdown = '\n'
  markdown += `| ${Array(cols).fill(' ').join(' | ')} |\n`
  markdown += `| ${Array(cols).fill('---').join(' | ')} |\n`
  for (let index = 1; index < rows; index += 1) {
    markdown += `| ${Array(cols).fill(' ').join(' | ')} |\n`
  }
  markdown += '\n'
  vditor.value?.insertValue(markdown)
  showTableModal.value = false
  hoverRows.value = 0
  hoverCols.value = 0
  window.setTimeout(syncEditorContent, 0)
}

async function handleTagSearch(keyword: string): Promise<void> {
  try {
    const result = await getTagPage({
      pageNum: 1,
      pageSize: 8,
      keyword: keyword || undefined,
    })
    tagOptions.value = mergeTagOptions(
      tagOptions.value,
      result.records.map((tag) => ({ label: tag.name, value: String(tag.id) })),
    )
  } catch (error) {
    console.error('Failed to search tags', error)
  }
}

async function resolveTagIds(): Promise<ApiId[]> {
  const optionByValue = new Map(tagOptions.value.map((option) => [option.value, option]))
  const resolved: ApiId[] = []

  for (const value of form.tagIds) {
    const option = optionByValue.get(value)
    if (option) {
      resolved.push(value)
      continue
    }

    const name = value.trim()
    if (!name) {
      continue
    }

    const tag: TagBriefVO = await createTag({ name })
    tagOptions.value = mergeTagOptions(tagOptions.value, [{ label: tag.name, value: String(tag.id) }])
    resolved.push(tag.id)
  }

  return resolved
}

async function handleSave(): Promise<void> {
  if (!form.title.trim()) {
    message.warning('请输入标题')
    return
  }

  saving.value = true
  try {
    const tagIds = await resolveTagIds()
    await updatePrivateBlog(blogId.value, {
      title: form.title.trim(),
      summary: form.summary,
      content: vditor.value?.getValue() ?? form.content,
      folderId: form.folderId,
      isPublished: form.isPublished,
      tagIds,
    })
    dirty.value = false
    message.success('已保存')
  } catch (error) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

function handleDelete(): void {
  dialog.warning({
    title: '删除博客',
    content: '删除后无法从前端恢复，确认继续？',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePrivateBlog(blogId.value)
        message.success('已删除')
        router.push('/workspace')
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

watch(isDark, () => {
  const current = vditor.value?.getValue() ?? form.content
  nextTick(() => initEditor(current))
})

watch(form, () => {
  if (contentInitialized) {
    dirty.value = true
  }
}, { deep: true })

onMounted(() => {
  loadPage()
  nextTick(() => {
    scrollTarget = resolveScrollTarget()
    updateScrollEffects()
    scrollTarget.addEventListener('scroll', updateScrollEffects, { passive: true })
    window.addEventListener('resize', updateScrollEffects)
  })
})

onUnmounted(() => {
  window.clearTimeout(tocTimer)
  scrollTarget?.removeEventListener('scroll', updateScrollEffects)
  window.removeEventListener('resize', updateScrollEffects)
  scrollTarget = null
  cleanupToc()
  destroyEditor()
})
</script>

<template>
  <div class="private-detail-page">
    <n-spin :show="loading">
      <div v-if="loadError" class="load-error">
        <n-empty description="无法打开这篇博客" />
        <n-button @click="router.push('/workspace')">返回工作台</n-button>
      </div>

      <div v-else class="editor-shell">
        <header class="editor-header">
          <button class="text-link" type="button" @click="router.push('/workspace')">
            <n-icon :component="ArrowBackOutline" />
            Workspace
          </button>

          <div class="state-group">
            <n-tag :type="saveStateType" round>
              <template #icon>
                <n-icon :component="dirty ? CloseCircleOutline : CheckmarkCircleOutline" />
              </template>
              {{ saveStateText }}
            </n-tag>
            <n-button type="primary" :loading="saving" @click="handleSave">
              <template #icon><n-icon :component="SaveOutline" /></template>
              Save
            </n-button>
            <n-button secondary type="error" @click="handleDelete">
              <template #icon><n-icon :component="TrashOutline" /></template>
            </n-button>
          </div>
        </header>

        <section class="meta-panel">
          <n-form label-placement="top" class="meta-form">
            <n-form-item label="Title">
              <n-input v-model:value="form.title" size="large" :maxlength="20" show-count class="custom-input" />
            </n-form-item>
            <n-form-item label="Summary">
              <n-input
                v-model:value="form.summary"
                type="textarea"
                :maxlength="200"
                show-count
                :autosize="{ minRows: 2, maxRows: 4 }"
                class="custom-input"
              />
            </n-form-item>
            <n-form-item label="Status">
              <div class="switch-row">
                <span>{{ form.isPublished === 1 ? 'Public' : 'Private' }}</span>
                <n-switch v-model:value="form.isPublished" :checked-value="1" :unchecked-value="0" />
              </div>
            </n-form-item>
            <n-form-item label="Folder">
              <n-tree-select
                v-model:value="form.folderId"
                :options="folderOptions"
                default-expand-all
                class="custom-select"
              >
                <template #arrow>
                  <n-icon :component="FolderOpenOutline" />
                </template>
              </n-tree-select>
            </n-form-item>
            <n-form-item label="Tags">
              <n-select
                v-model:value="form.tagIds"
                :options="tagOptions"
                multiple
                filterable
                tag
                clearable
                class="custom-select"
                :on-create="createTagOption"
                @search="handleTagSearch"
              >
                <template #arrow>
                  <n-icon :component="PricetagOutline" />
                </template>
              </n-select>
            </n-form-item>
          </n-form>
        </section>

        <div class="workbench">
          <aside class="toc-panel">
            <div class="toc-title">Contents</div>
            <div v-if="tocItems.length" class="toc-list">
              <button
                v-for="item in visibleTocItems"
                :key="item.id"
                type="button"
                class="toc-item"
                :class="[`toc-level-${item.level}`, { 'toc-active': activeTocId === item.id }]"
                @click="scrollToHeading(editorRef, item.id)"
              >
                <span>{{ item.text }}</span>
                <span
                  v-if="item.hasChildren"
                  class="toc-toggle"
                  @click="toggleTocExpand(item.id, $event)"
                >
                  {{ expandedTocIds.has(item.id) ? '-' : '+' }}
                </span>
              </button>
            </div>
            <n-empty v-else size="small" description="No contents" />
          </aside>

          <main ref="editorSurfaceRef" class="editor-card">
            <div ref="editorRef" class="vditor-edit-container"></div>

            <div class="word-count">
              {{ wordCount }} chars
            </div>

            <div
              v-if="tableHoverState.visible"
              class="table-action-popover"
              :style="{ left: `${tableHoverState.left}px`, top: `${tableHoverState.top}px` }"
            >
              <button type="button" @mousedown.prevent @click="insertTableColumn">+ Col</button>
              <button type="button" @mousedown.prevent @click="insertTableRow">+ Row</button>
              <button type="button" @mousedown.prevent @click="deleteTableColumn">- Col</button>
              <button type="button" @mousedown.prevent @click="deleteTableRow">- Row</button>
            </div>
          </main>
        </div>
      </div>
    </n-spin>

    <button
      v-show="showBackTop"
      class="back-top-button"
      type="button"
      title="回到顶部"
      aria-label="回到顶部"
      @click="scrollToTop"
    >
      <n-icon :component="ArrowUpOutline" />
    </button>

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
        <div class="table-grid-cells" @mouseleave="hoverRows = 0; hoverCols = 0">
          <div v-for="row in 10" :key="row" class="grid-row">
            <button
              v-for="col in 10"
              :key="col"
              type="button"
              class="grid-cell"
              :class="{ active: row <= hoverRows && col <= hoverCols }"
              @mouseenter="hoverRows = row; hoverCols = col"
              @click="insertCustomTable(row, col)"
            />
          </div>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<style scoped>
.private-detail-page {
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
  padding: 28px 20px 96px;
}

.editor-shell {
  width: min(1440px, 100%);
  margin: 0 auto;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.text-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: 'Lato', sans-serif;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.text-link:hover {
  color: var(--accent-color);
}

.state-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.meta-panel,
.editor-card,
.toc-panel {
  border: 1px solid var(--line-color);
  background: var(--bg-primary);
}

.meta-panel {
  padding: 22px 24px 4px;
  margin-bottom: 22px;
}

.meta-form {
  display: grid;
  grid-template-columns: minmax(280px, 2fr) minmax(260px, 1.2fr) minmax(140px, 0.6fr) minmax(210px, 1fr) minmax(240px, 1fr);
  gap: 16px;
}

.meta-form :deep(.n-form-item-label) {
  font-family: 'Lato', sans-serif;
  color: var(--text-tertiary);
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.custom-input,
.custom-select {
  background: transparent;
}

.switch-row {
  min-height: 34px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-primary);
  font-family: 'Lato', sans-serif;
}

.workbench {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 22px;
  align-items: start;
}

.toc-panel {
  position: sticky;
  top: 92px;
  padding: 22px;
  max-height: calc(100vh - 120px);
  overflow: auto;
}

.toc-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
  font-weight: 700;
  padding-bottom: 14px;
  margin-bottom: 14px;
  border-bottom: 1px solid var(--line-color);
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.toc-item {
  width: 100%;
  border: 0;
  background: transparent;
  color: var(--text-secondary);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 0;
  cursor: pointer;
  text-align: left;
  font-family: 'Lato', sans-serif;
}

.toc-item:hover,
.toc-active {
  color: var(--text-primary);
  transform: translateX(4px);
}

.toc-active {
  font-weight: 700;
}

.toc-toggle {
  color: var(--accent-color);
  flex: 0 0 auto;
}

.toc-level-2 { padding-left: 14px; }
.toc-level-3 { padding-left: 28px; }
.toc-level-4 { padding-left: 42px; }
.toc-level-5 { padding-left: 56px; }
.toc-level-6 { padding-left: 70px; }

.editor-card {
  min-width: 0;
  position: relative;
  padding: 18px;
}

.vditor-edit-container {
  min-height: 720px;
}

.back-top-button {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 120;
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--line-color);
  background: var(--modal-bg);
  color: var(--text-primary);
  cursor: pointer;
  backdrop-filter: blur(14px);
  box-shadow: 0 18px 38px rgba(0, 0, 0, 0.16);
  transition: transform 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.back-top-button:hover {
  transform: translateY(-3px);
  border-color: var(--accent-color);
  color: var(--accent-color);
}

.word-count {
  position: absolute;
  right: 28px;
  bottom: 28px;
  z-index: 80;
  padding: 7px 12px;
  border: 1px solid var(--line-color);
  background: var(--modal-bg);
  color: var(--text-secondary);
  backdrop-filter: blur(12px);
  font-family: 'Lato', sans-serif;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 1px;
}

.table-action-popover {
  position: absolute;
  z-index: 90;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
  padding: 6px;
  border: 1px solid var(--line-color);
  background: var(--modal-bg);
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.16);
}

.table-action-popover button {
  border: 1px solid var(--line-color);
  background: var(--bg-primary);
  color: var(--text-primary);
  padding: 5px 8px;
  cursor: pointer;
  font-family: 'Lato', sans-serif;
  font-size: 0.72rem;
  font-weight: 700;
}

.table-action-popover button:hover {
  border-color: var(--accent-color);
  color: var(--accent-color);
}

:deep(.vditor) {
  border: 0 !important;
  background: transparent !important;
  min-height: 720px !important;
}

:deep(.vditor-toolbar) {
  border-bottom: 1px solid var(--line-color) !important;
  background: var(--bg-primary) !important;
}

:deep(.vditor-toolbar--pin) {
  position: sticky !important;
  top: 76px !important;
  z-index: 220 !important;
  background: var(--modal-bg) !important;
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}

:deep(.vditor-toolbar--page-fixed) {
  position: fixed !important;
  box-sizing: border-box;
}

.editor-card--toolbar-fixed :deep(.vditor-content) {
  margin-top: var(--editor-toolbar-height, 36px);
}

:deep(.vditor-content) {
  background: transparent !important;
  min-height: 680px !important;
  height: auto !important;
  overflow: visible !important;
}

:deep(.vditor-ir),
:deep(.vditor-sv),
:deep(.vditor-wysiwyg) {
  min-height: 680px !important;
  height: auto !important;
  overflow: visible !important;
}

:deep(.vditor-reset) {
  color: var(--text-primary) !important;
  font-family: 'Lato', sans-serif !important;
  font-size: 1.02rem !important;
  line-height: 1.68 !important;
  min-height: 640px !important;
}

:deep(.heading-folded)::after {
  content: ' ...';
  color: var(--accent-color);
}

.load-error {
  min-height: 420px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  align-items: center;
  justify-content: center;
}

.table-grid-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.table-grid-info {
  margin-bottom: 15px;
  color: var(--text-secondary);
  font-family: 'Lato', sans-serif;
  font-size: 0.9rem;
  font-weight: 700;
}

.table-grid-cells {
  border: 1px solid var(--line-color);
  padding: 2px;
}

.grid-row {
  display: flex;
}

.grid-cell {
  width: 22px;
  height: 22px;
  border: 1px solid var(--line-color);
  margin: 1px;
  background: transparent;
  cursor: pointer;
}

.grid-cell.active {
  background: var(--text-primary);
  border-color: var(--text-primary);
}

@media (max-width: 1180px) {
  .meta-form,
  .workbench {
    grid-template-columns: 1fr;
  }

  .toc-panel {
    position: static;
    max-height: none;
  }
}

@media (max-width: 720px) {
  .editor-header,
  .state-group {
    align-items: stretch;
    flex-direction: column;
  }

  .meta-panel,
  .editor-card,
  .toc-panel {
    padding: 16px;
  }

  :deep(.vditor-toolbar--pin) {
    top: 70px !important;
  }
}
</style>
