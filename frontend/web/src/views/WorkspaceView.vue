<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NCheckbox,
  NEmpty,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NModal,
  NPagination,
  NSpace,
  NSpin,
  NTag,
  NTree,
  NTreeSelect,
  useDialog,
  useMessage,
} from 'naive-ui'
import {
  Add,
  CreateOutline,
  DocumentTextOutline,
  FolderOpenOutline,
  MoveOutline,
  PencilOutline,
  TrashOutline,
} from '@vicons/ionicons5'
import { createPrivateBlog } from '@/api/blog'
import {
  createFolder,
  deleteFolder,
  getCurrentUserFolderTree,
  getFolderBlogPage,
  moveBlogsToFolder,
  moveFolder,
  renameFolder,
} from '@/api/folder'
import type { ApiId, BlogPrivateBriefVO, FolderTree } from '@/types'
import { ROOT_FOLDER_ID } from '@/types'
import { DateUtils } from '@/types/date'

interface TreeNode {
  [key: string]: unknown
  label: string
  key: string
  children?: TreeNode[]
}

type FolderModalMode = 'create' | 'rename'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const treeLoading = ref(false)
const blogsLoading = ref(false)
const folderTree = ref<FolderTree | null>(null)
const selectedFolderId = ref(String(ROOT_FOLDER_ID))
const selectedBlogIds = ref<string[]>([])
const blogs = ref<BlogPrivateBriefVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const folderModal = reactive({
  show: false,
  mode: 'create' as FolderModalMode,
  targetId: String(ROOT_FOLDER_ID),
  name: '',
})
const moveFolderModal = reactive({
  show: false,
  folderId: '',
  targetParentId: String(ROOT_FOLDER_ID),
})
const moveBlogsModal = reactive({
  show: false,
  targetId: String(ROOT_FOLDER_ID),
})
const createBlogModal = reactive({
  show: false,
  title: '',
})

const rootFolder = computed<FolderTree>(() => {
  if (folderTree.value && String(folderTree.value.id) === String(ROOT_FOLDER_ID)) {
    return {
      ...folderTree.value,
      name: '根目录',
    }
  }

  return {
    id: ROOT_FOLDER_ID,
    name: '根目录',
    level: 0,
    sortOrder: 0,
    children: folderTree.value ? [folderTree.value] : [],
  }
})

const treeOptions = computed<TreeNode[]>(() => [toTreeNode(rootFolder.value)])
const folderSelectOptions = computed<TreeNode[]>(() => treeOptions.value)
const currentFolder = computed(() => findFolder(rootFolder.value, selectedFolderId.value) ?? rootFolder.value)
const childFolders = computed(() => currentFolder.value.children ?? [])
const breadcrumbs = computed(() => findFolderPath(rootFolder.value, selectedFolderId.value) ?? [rootFolder.value])
const checkedAll = computed({
  get: () => blogs.value.length > 0 && selectedBlogIds.value.length === blogs.value.length,
  set: (checked: boolean) => {
    selectedBlogIds.value = checked ? blogs.value.map((blog) => String(blog.id)) : []
  },
})

function toTreeNode(folder: FolderTree): TreeNode {
  return {
    label: getFolderName(folder),
    key: String(folder.id),
    children: folder.children?.map(toTreeNode),
  }
}

function getFolderName(folder: FolderTree): string {
  return String(folder.id) === String(ROOT_FOLDER_ID) ? '根目录' : folder.name
}

function findFolder(folder: FolderTree, id: string): FolderTree | null {
  if (String(folder.id) === id) {
    return folder
  }

  for (const child of folder.children ?? []) {
    const result = findFolder(child, id)
    if (result) {
      return result
    }
  }

  return null
}

function findFolderPath(folder: FolderTree, id: string, path: FolderTree[] = []): FolderTree[] | null {
  const nextPath = [...path, folder]
  if (String(folder.id) === id) {
    return nextPath
  }

  for (const child of folder.children ?? []) {
    const result = findFolderPath(child, id, nextPath)
    if (result) {
      return result
    }
  }

  return null
}

function isDescendant(parentId: string, candidateId: string): boolean {
  const parent = findFolder(rootFolder.value, parentId)
  if (!parent) {
    return false
  }
  return Boolean(parent.children?.some((child) => String(child.id) === candidateId || isDescendant(String(child.id), candidateId)))
}

async function loadTree(): Promise<void> {
  treeLoading.value = true
  try {
    folderTree.value = await getCurrentUserFolderTree()
  } catch (error) {
    message.error('目录树加载失败')
  } finally {
    treeLoading.value = false
  }
}

async function loadBlogs(): Promise<void> {
  blogsLoading.value = true
  selectedBlogIds.value = []
  try {
    const page = await getFolderBlogPage(selectedFolderId.value, {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortField: 'updateTime',
      sortOrder: 'desc',
    })
    blogs.value = page.records
    total.value = page.total
  } catch (error) {
    message.error('博客列表加载失败')
  } finally {
    blogsLoading.value = false
  }
}

function selectFolder(id: ApiId): void {
  selectedFolderId.value = String(id)
}

function openCreateFolder(parentId = selectedFolderId.value): void {
  folderModal.mode = 'create'
  folderModal.targetId = parentId
  folderModal.name = ''
  folderModal.show = true
}

function openRenameFolder(folder: FolderTree): void {
  if (String(folder.id) === String(ROOT_FOLDER_ID)) {
    return
  }
  folderModal.mode = 'rename'
  folderModal.targetId = String(folder.id)
  folderModal.name = folder.name
  folderModal.show = true
}

async function submitFolderModal(): Promise<void> {
  const name = folderModal.name.trim()
  if (!name) {
    message.warning('请输入目录名')
    return
  }

  try {
    if (folderModal.mode === 'create') {
      await createFolder({ parentId: folderModal.targetId, name })
      message.success('目录已创建')
    } else {
      await renameFolder(folderModal.targetId, { name })
      message.success('目录已重命名')
    }
    folderModal.show = false
    await loadTree()
  } catch (error) {
    message.error(folderModal.mode === 'create' ? '创建目录失败' : '重命名失败')
  }
}

function confirmDeleteFolder(folder: FolderTree): void {
  if (String(folder.id) === String(ROOT_FOLDER_ID)) {
    return
  }

  dialog.warning({
    title: '删除目录',
    content: '该目录及其子目录会被删除，目录内博客将由后端移动到根目录。确认继续？',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteFolder(folder.id)
        message.success('目录已删除')
        if (String(folder.id) === selectedFolderId.value) {
          selectedFolderId.value = String(ROOT_FOLDER_ID)
        }
        await loadTree()
        await loadBlogs()
      } catch (error) {
        message.error('删除目录失败')
      }
    },
  })
}

function openMoveFolder(folder: FolderTree): void {
  if (String(folder.id) === String(ROOT_FOLDER_ID)) {
    return
  }
  moveFolderModal.folderId = String(folder.id)
  moveFolderModal.targetParentId = String(ROOT_FOLDER_ID)
  moveFolderModal.show = true
}

async function submitMoveFolder(): Promise<void> {
  if (moveFolderModal.folderId === moveFolderModal.targetParentId || isDescendant(moveFolderModal.folderId, moveFolderModal.targetParentId)) {
    message.warning('不能移动到自身或子目录下')
    return
  }

  try {
    await moveFolder(moveFolderModal.folderId, { targetParentId: moveFolderModal.targetParentId })
    message.success('目录已移动')
    moveFolderModal.show = false
    await loadTree()
  } catch (error) {
    message.error('移动目录失败')
  }
}

async function submitCreateBlog(): Promise<void> {
  const title = createBlogModal.title.trim()
  if (!title) {
    message.warning('请输入标题')
    return
  }

  try {
    const id = await createPrivateBlog({
      title,
      folderId: selectedFolderId.value,
    })
    createBlogModal.show = false
    createBlogModal.title = ''
    router.push(`/workspace/blogs/${id}`)
  } catch (error) {
    message.error('创建博客失败')
  }
}

function openMoveBlogs(): void {
  if (selectedBlogIds.value.length === 0) {
    message.warning('请先选择博客')
    return
  }
  moveBlogsModal.targetId = selectedFolderId.value
  moveBlogsModal.show = true
}

async function submitMoveBlogs(): Promise<void> {
  try {
    await moveBlogsToFolder({
      blogIds: selectedBlogIds.value,
      targetId: moveBlogsModal.targetId,
    })
    message.success('博客已移动')
    moveBlogsModal.show = false
    await loadBlogs()
  } catch (error) {
    message.error('移动博客失败')
  }
}

function toggleBlogChecked(id: ApiId, checked: boolean): void {
  const blogId = String(id)
  selectedBlogIds.value = checked
    ? Array.from(new Set([...selectedBlogIds.value, blogId]))
    : selectedBlogIds.value.filter((item) => item !== blogId)
}

watch(selectedFolderId, () => {
  pageNum.value = 1
  loadBlogs()
})

watch(pageNum, () => {
  loadBlogs()
})

onMounted(async () => {
  await loadTree()
  await loadBlogs()
})
</script>

<template>
  <div class="workspace-page">
    <div class="workspace-shell">
      <aside class="folder-pane">
        <div class="pane-header">
          <div>
            <span class="eyebrow">Workspace</span>
            <h1>工作台</h1>
          </div>
          <n-button circle secondary @click="openCreateFolder()">
            <template #icon><n-icon :component="Add" /></template>
          </n-button>
        </div>

        <n-spin :show="treeLoading">
          <n-tree
            block-line
            block-node
            selectable
            default-expand-all
            :data="treeOptions"
            :selected-keys="[selectedFolderId]"
            @update:selected-keys="(keys) => selectFolder(keys[0] || ROOT_FOLDER_ID)"
          />
        </n-spin>
      </aside>

      <main class="content-pane">
        <section class="content-header">
          <div class="breadcrumbs">
            <button
              v-for="(folder, index) in breadcrumbs"
              :key="folder.id"
              type="button"
              @click="selectFolder(folder.id)"
            >
              {{ getFolderName(folder) }}
              <span v-if="index < breadcrumbs.length - 1">/</span>
            </button>
          </div>

          <n-space wrap>
            <n-button secondary @click="openCreateFolder()">
              <template #icon><n-icon :component="FolderOpenOutline" /></template>
              新建目录
            </n-button>
            <n-button type="primary" @click="createBlogModal.show = true">
              <template #icon><n-icon :component="CreateOutline" /></template>
              新建博客
            </n-button>
          </n-space>
        </section>

        <section class="library-section">
          <div class="library-head">
            <div class="section-title">
              <span>Contents</span>
              <small>{{ childFolders.length }} folders / {{ total }} blogs</small>
            </div>
            <n-space>
              <n-button secondary :disabled="selectedBlogIds.length === 0" @click="openMoveBlogs">
                <template #icon><n-icon :component="MoveOutline" /></template>
                移动
              </n-button>
            </n-space>
          </div>

          <n-spin :show="blogsLoading">
            <div class="library-content">
              <div v-if="childFolders.length" class="library-zone folder-zone">
                <div class="folder-grid">
                  <article
                    v-for="folder in childFolders"
                    :key="folder.id"
                    class="folder-card"
                    @click="selectFolder(folder.id)"
                  >
                    <div class="folder-main">
                      <n-icon class="folder-icon" :component="FolderOpenOutline" />
                      <div>
                        <h3>{{ getFolderName(folder) }}</h3>
                        <p>{{ folder.children?.length || 0 }} subfolders</p>
                      </div>
                    </div>
                    <div class="folder-actions" @click.stop>
                      <n-button quaternary circle size="small" @click="openCreateFolder(String(folder.id))">
                        <template #icon><n-icon :component="Add" /></template>
                      </n-button>
                      <n-button quaternary circle size="small" @click="openRenameFolder(folder)">
                        <template #icon><n-icon :component="PencilOutline" /></template>
                      </n-button>
                      <n-button quaternary circle size="small" @click="openMoveFolder(folder)">
                        <template #icon><n-icon :component="MoveOutline" /></template>
                      </n-button>
                      <n-button quaternary circle size="small" type="error" @click="confirmDeleteFolder(folder)">
                        <template #icon><n-icon :component="TrashOutline" /></template>
                      </n-button>
                    </div>
                  </article>
                </div>
              </div>

              <div v-if="blogs.length" class="library-zone blog-zone">
                <div class="blog-table">
                  <div class="blog-table-head">
                    <n-checkbox v-model:checked="checkedAll" />
                    <span>Title</span>
                    <span>Status</span>
                    <span>Updated</span>
                    <span>Likes</span>
                  </div>
                  <div
                    v-for="blog in blogs"
                    :key="blog.id"
                    class="blog-row"
                    @click="router.push(`/workspace/blogs/${blog.id}`)"
                  >
                    <n-checkbox
                      :checked="selectedBlogIds.includes(String(blog.id))"
                      @click.stop
                      @update:checked="(checked) => toggleBlogChecked(blog.id, checked)"
                    />
                    <div class="blog-cell-main">
                      <n-icon :component="DocumentTextOutline" />
                      <div>
                        <h3>{{ blog.title }}</h3>
                        <p>{{ blog.summary || 'No summary' }}</p>
                      </div>
                    </div>
                    <n-tag :type="blog.isPublished === 1 ? 'success' : 'default'" size="small" round>
                      {{ blog.isPublished === 1 ? 'Public' : 'Private' }}
                    </n-tag>
                    <span>{{ DateUtils.isoToDateOnly(blog.updateTime || blog.createTime) }}</span>
                    <span>{{ blog.likeCount || 0 }}</span>
                  </div>

                  <div class="pagination-wrap">
                    <n-pagination
                      v-model:page="pageNum"
                      :page-size="pageSize"
                      :item-count="total"
                    />
                  </div>
                </div>
              </div>

              <n-empty
                v-if="!childFolders.length && !blogs.length"
                class="empty-library"
                description="当前目录暂无内容"
              />
            </div>
          </n-spin>
        </section>
      </main>
    </div>

    <n-modal v-model:show="folderModal.show">
      <div class="workspace-modal">
        <h2>{{ folderModal.mode === 'create' ? '新建目录' : '重命名目录' }}</h2>
        <n-form label-placement="top">
          <n-form-item label="Name">
            <n-input v-model:value="folderModal.name" @keyup.enter="submitFolderModal" />
          </n-form-item>
        </n-form>
        <div class="modal-actions">
          <n-button @click="folderModal.show = false">取消</n-button>
          <n-button type="primary" @click="submitFolderModal">确认</n-button>
        </div>
      </div>
    </n-modal>

    <n-modal v-model:show="moveFolderModal.show">
      <div class="workspace-modal">
        <h2>移动目录</h2>
        <n-form label-placement="top">
          <n-form-item label="Target">
            <n-tree-select
              v-model:value="moveFolderModal.targetParentId"
              :options="folderSelectOptions"
              :to="false"
              default-expand-all
            />
          </n-form-item>
        </n-form>
        <div class="modal-actions">
          <n-button @click="moveFolderModal.show = false">取消</n-button>
          <n-button type="primary" @click="submitMoveFolder">移动</n-button>
        </div>
      </div>
    </n-modal>

    <n-modal v-model:show="moveBlogsModal.show">
      <div class="workspace-modal">
        <h2>移动博客</h2>
        <n-form label-placement="top">
          <n-form-item label="Target">
            <n-tree-select
              v-model:value="moveBlogsModal.targetId"
              :options="folderSelectOptions"
              :to="false"
              default-expand-all
            />
          </n-form-item>
        </n-form>
        <div class="modal-actions">
          <n-button @click="moveBlogsModal.show = false">取消</n-button>
          <n-button type="primary" @click="submitMoveBlogs">移动</n-button>
        </div>
      </div>
    </n-modal>

    <n-modal v-model:show="createBlogModal.show">
      <div class="workspace-modal">
        <h2>新建博客</h2>
        <n-form label-placement="top">
          <n-form-item label="Title">
            <n-input v-model:value="createBlogModal.title" :maxlength="20" show-count @keyup.enter="submitCreateBlog" />
          </n-form-item>
        </n-form>
        <div class="modal-actions">
          <n-button @click="createBlogModal.show = false">取消</n-button>
          <n-button type="primary" @click="submitCreateBlog">创建</n-button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<style scoped>
.workspace-page {
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
  padding: 36px 20px 80px;
}

.workspace-shell {
  width: min(1440px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 28px;
  align-items: start;
}

.folder-pane,
.content-pane section,
.workspace-modal {
  background: var(--bg-primary);
  border: 1px solid var(--line-color);
}

.folder-pane {
  position: sticky;
  top: 92px;
  padding: 24px;
  max-height: calc(100vh - 120px);
  overflow: auto;
}

.pane-header,
.content-header,
.library-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.eyebrow {
  color: var(--accent-color);
  font-family: 'Lato', sans-serif;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 3px;
  text-transform: uppercase;
}

.pane-header h1 {
  margin: 4px 0 0;
  font-family: 'Playfair Display', serif;
  font-size: 2.2rem;
  font-weight: 400;
  color: var(--text-primary);
}

.content-pane {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 0;
}

.content-pane section {
  padding: 24px;
}

.content-header {
  margin-bottom: 22px;
}

.library-section {
  overflow: hidden;
  padding: 0 !important;
}

.breadcrumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.breadcrumbs button {
  border: 0;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-family: 'Lato', sans-serif;
  font-weight: 700;
}

.breadcrumbs button:hover {
  color: var(--accent-color);
}

.section-title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 0;
  font-family: 'Playfair Display', serif;
  font-size: 1.45rem;
  color: var(--text-primary);
}

.section-title small {
  color: var(--text-tertiary);
  font-style: italic;
}

.library-head {
  padding: 24px;
  border-bottom: 1px solid var(--line-color);
}

.library-content {
  min-height: 220px;
}

.library-zone {
  padding: 24px;
}

.folder-zone {
  background: var(--card-hover);
}

.blog-zone {
  background: var(--bg-primary);
}

.folder-zone + .blog-zone {
  border-top: 1px solid var(--line-color);
}

.folder-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.folder-card {
  border: 1px solid var(--line-color);
  background: var(--bg-primary);
  padding: 18px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.folder-card:hover {
  border-color: var(--accent-color);
  transform: translateY(-4px);
  box-shadow: 10px 10px 0 var(--line-color);
}

.folder-main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.folder-icon {
  color: var(--accent-color);
  font-size: 1.8rem;
}

.folder-card h3,
.blog-cell-main h3 {
  margin: 0;
  color: var(--text-primary);
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
}

.folder-card p,
.blog-cell-main p {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: 0.86rem;
}

.folder-actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 16px;
}

.blog-table {
  background: transparent;
}

.blog-table-head,
.blog-row {
  display: grid;
  grid-template-columns: 44px minmax(260px, 1fr) 120px 140px 80px;
  gap: 16px;
  align-items: center;
  padding: 14px 18px;
}

.blog-table-head {
  color: var(--text-tertiary);
  border-bottom: 1px solid var(--line-color);
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.blog-row {
  cursor: pointer;
  border-bottom: 1px solid var(--line-color);
  transition: background 0.2s ease;
}

.blog-row:last-child {
  border-bottom: 0;
}

.blog-row:hover {
  background: var(--card-hover);
}

.blog-cell-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.blog-cell-main h3,
.blog-cell-main p {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 20px;
  border-top: 1px solid var(--line-color);
}

.empty-library {
  padding: 72px 24px;
}

.workspace-modal {
  width: min(92vw, 460px);
  padding: 26px;
  box-shadow: 0 24px 54px rgba(0, 0, 0, 0.18);
}

.workspace-modal h2 {
  margin: 0 0 22px;
  font-family: 'Playfair Display', serif;
  font-size: 1.55rem;
  color: var(--text-primary);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1080px) {
  .workspace-shell {
    grid-template-columns: 1fr;
  }

  .folder-pane {
    position: static;
    max-height: none;
  }

  .blog-table-head {
    display: none;
  }

  .blog-row {
    grid-template-columns: 34px minmax(0, 1fr);
  }

  .blog-row > span,
  .blog-row > .n-tag {
    grid-column: 2;
  }
}

@media (max-width: 680px) {
  .content-header,
  .library-head {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
