<template>
    <div class="blog-list-container">
        <!-- 背景层 -->
        <div class="background-layer"></div>

        <!-- 内容层 -->
        <div class="content-layer">
            <div class="blog-list-wrapper">
                <div class="blog-list-header">
                    <h2>博客列表</h2>
                    <div class="search-box">
                    <el-input
                        v-model="searchForm.keyword"
                        placeholder="搜索博客..."
                        class="search-input"
                        @keyup.enter="handleSearch"
                    >
                        <template #prefix>
                            <el-icon><Search /></el-icon>
                        </template>
                    </el-input>
                    <el-button type="primary" @click="handleSearch">搜索</el-button>
                    <el-button class="add-btn" @click="showCreateDialog = true">+创建博客</el-button>
                </div>
                    <div class="sort-options">
                        <FRSelector
                            v-model="searchForm.orderBy"
                            :options="orderOptions"
                            placeholder="排序方式"
                            style="width: 120px;"
                        />
                        <FRSelector
                            v-model="searchForm.sortOrder"
                            :options="sortOrderOptions"
                            placeholder="排序顺序"
                            style="width: 120px;"
                        />
                    </div>
                </div>

                <div class="blog-list-content">
                    <div 
                        class="blog-list-item" 
                        v-for="blog in blogList" 
                        :key="blog.bloguid"
                        :class="BlogUtils.getCategoryClass(blog.bigCategoryId)"
                        @click="openBlog(blog.bloguid)"
                    >
                        <div class="blog-header">
                            <div class="blog-title">
                                [ {{ blog.title }} ]
                            </div>
                            <div class="blog-category">
                                <span>{{ BlogUtils.bigIdToString(blog.bigCategoryId) }}</span>
                                >
                                <span>{{ blog.littleCategoryName }}</span>
                            </div>
                            <div class="blog-time">
                                {{ DateUtils.isoToDateOnly(blog.updateTime) }}
                            </div>
                        </div>
                        <div class="blog-body">
                            <div class="blog-summary">
                                {{ blog.summary ? blog.summary : '暂无描述' }}
                            </div>
                        </div>
                    </div>
                </div>

                <div class="pagination-container" v-if="total > 0">
                    <el-pagination
                        v-model:current-page="searchForm.page"
                        v-model:page-size="searchForm.pageSize"
                        :total="total"
                        layout="total, prev, pager, next, jumper"
                        @current-change="handleCurrentChange"
                    />
                </div>
            </div>
        </div>

        <el-dialog
            v-model="showCreateDialog"
            title="创建博客"
            width="500px"
            custom-class="create-blog-dialog"
            :before-close="handleCreateDialogClose"
        >
            <el-form :model="createForm" ref="createFormRef" label-width="100px" label-position="right" class="create-blog-form">
                <el-form-item label="大类" prop="bigCategoryId">
                    <div class="dialog-row">
                        <FRSelector
                            v-model="createForm.bigCategoryId"
                            :options="bigCategoryOptions"
                            placeholder="选择大类"
                            style="width: 160px;"
                        />
                    </div>
                </el-form-item>
                <el-form-item label="小类名称" prop="littleCategoryName">
                    <el-input v-model="createForm.littleCategoryName" placeholder="请输入小类名称" clearable />
                </el-form-item>
                <el-form-item label="标题" prop="title">
                    <el-input v-model="createForm.title" placeholder="请输入标题" clearable />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="handleCreateDialogClose" class="cancel-btn">取消</el-button>
                    <el-button type="primary" @click="handleCreateBlog" class="confirm-btn">提交</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getBlogListPage, createBlog } from '@/api/blog'
import { type Blog, type BlogPageQueryDTO, BlogUtils, DateUtils } from '@/types'
import FRSelector from '@/components/FRSelector.vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 路由
const router = useRouter()
const route = useRoute()
const uid = Number(route.params.uid)
const useStore = useUserStore()

// 搜索表单
const searchForm: BlogPageQueryDTO = reactive({
    page: 1,
    pageSize: 10,
    bigCategoryId: 0,
    keyword: '',
    orderBy: 'update_time',
    sortOrder: 'desc',
    litteCategoryName: '',
    categoryId: '',
    authorId: 0
})
const orderOptions = [
    {
        value: 'update_time',
        label: '更新字段'
    }, {
        value: 'create_time',
        label: '创建字段'
    }, {
        value: 'title',
        label: '标题'
    },
]
const sortOrderOptions = [
    {
        value: 'desc',
        label: '降序'
    },
    {
        value: 'asc',
        label: '升序'
    },
]
// 博客列表数据
const blogList = ref<Blog[]>([])
const total = ref(0)

// 创建博客对话框
const showCreateDialog = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
    bigCategoryId: 0,
    littleCategoryName: '',
    title: ''
})

const bigCategoryOptions = [
    { value: 1, label: BlogUtils.bigIdToString(1) as string },
    { value: 2, label: BlogUtils.bigIdToString(2) as string },
    { value: 3, label: BlogUtils.bigIdToString(3) as string },
    { value: 4, label: BlogUtils.bigIdToString(4) as string },
    { value: 5, label: BlogUtils.bigIdToString(5) as string },
]

// 获取博客列表
const fetchBlogList = async () => {
    try {
        const response = await getBlogListPage(uid, searchForm)
        console.log('分页响应:', response)
        blogList.value = Array.isArray(response.list) ? response.list : []
        total.value = typeof response.total === 'number' ? response.total : 0
        console.log('渲染 blogList:', blogList.value)
    } catch (error) {
        console.error('获取博客列表失败:', error)
    }
}

// 搜索处理
const handleSearch = () => {
    fetchBlogList()
}

// 分页处理
// const handleSizeChange = (val: number) => {
//     searchForm.pageSize = val
//     fetchBlogList()
// }

const handleCurrentChange = (val: number) => {
    searchForm.page = val
    fetchBlogList()
}

// 跳转到博客详情页
const openBlog = (bloguid: string) => {
    router.push(`/${uid}/blog/${bloguid}`)
}

// 关闭创建博客对话框
function handleCreateDialogClose() {
    showCreateDialog.value = false
    if (createFormRef.value) createFormRef.value.resetFields()
    createForm.bigCategoryId = 0,
    createForm.littleCategoryName = ''
    createForm.title = ''
}

// 创建博客处理
async function handleCreateBlog() {
    if (!createForm.bigCategoryId || !createForm.littleCategoryName || !createForm.title) {
        ElMessage.error('请填写完整信息')
        return
    }
    if (!useStore.$state.userInfo?.id) {
        ElMessage.error('身份无效或过期，请登录')
        return
    }
    try {
        // 假定 authorName 可从本地获取或后端自动填充
        const res = await createBlog(useStore.$state.userInfo?.id as number, {
            title: createForm.title,
            bigCategoryId: Number(createForm.bigCategoryId),
            littleCategoryName: createForm.littleCategoryName,
            authorName: useStore.$state.userInfo?.nickname as string
        })
        ElMessage.success('博客创建成功')
        showCreateDialog.value = false
        router.push(`/blog/${res}`)
    } catch (err) {
        console.error('>博客创建失败', err)
    }
}

// 组件挂载时获取数据
onMounted(() => {
    fetchBlogList()
})
</script>

<style scoped>
.blog-list-container {
    height: 100%;
    width: 100%;
    position: relative;
    overflow: hidden;
}

.background-layer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url('@/assets/images/bg/bg03.jpg');
    background-size: cover;
    background-position: center;
    filter: blur(1px);
    z-index: 0;
}

.content-layer {
    position: relative;
    z-index: 1;
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
    padding: 20px;
    box-sizing: border-box;
    margin-top: 2.5%;
}


.search-container {
    width: 100%;
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.search-box {
    display: flex;
    gap: 10px;
    width: 60%;
    min-width: 300px;
}

.search-input {
    flex: 1;
}

.blog-list-wrapper {
    flex: 1;
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 15px;
    padding: 20px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}

.blog-list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid rgb(53, 238, 204);
}

.blog-list-header h2 {
    color: rgb(183, 121, 241);
    margin: 0;
    font-size: 24px;
}

.sort-options {
    display: flex;
    gap: 10px;
}

.sort-select {
    width: 120px;
}

.blog-list-content {
    flex: 1;
    overflow-y: auto;
    margin-bottom: 20px;
}

.blog-list-item {
    width: 100%;
    min-height: 100px;
    margin-top: 1%;
    border-top: 3px solid rgb(249, 149, 245);
    border-radius: 10px;
    background-color: rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.blog-list-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.blog-list-item.category-project {
    border-top: 3px solid rgb(248, 116, 0);
}

.blog-list-item.category-tech {
    border-top: 3px solid rgb(0, 161, 248);
}

.blog-list-item.category-algo {
    border-top: 3px solid rgb(0, 248, 83);
}

.blog-list-item.category-game {
    border-top: 3px solid rgb(248, 244, 0);
}

.blog-list-item.category-other {
    border-top: 3px solid rgb(248, 0, 194);
}

.blog-header {
    width: 100%;
    height: 30px;
    display: flex;
    border-bottom: 1px solid rgb(132, 255, 255);
    align-items: center;
}

.blog-title {
    margin-left: 3%;
    color: rgb(253, 202, 0);
    font-size: 17px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.blog-category {
    margin-left: auto;
    color: white;
    margin-right: 5%;
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 14px;
}

.blog-category > span {
    color: rgb(38, 181, 209);
}

.blog-category > span.category-project {
    color: rgb(255, 183, 0);
}

.blog-category > span.category-tech {
    color: rgb(0, 132, 255);
}

.blog-category > span.category-algo {
    color: rgb(2, 186, 35);
}

.blog-category > span.category-game {
    color: rgb(216, 201, 32);
}

.blog-category > span.category-other {
    color: rgb(155, 112, 255);
}

.blog-category > span:last-child {
    color: rgb(1, 215, 208);
}

.blog-time {
    color: rgba(2, 198, 198, 0.466);
    font-size: 14px;
    margin-right: 1%;
}

.blog-body {
    width: 95%;
    flex: 1;
    display: flex;
    align-items: center;
    padding: 10px 0;
    margin-left: 2%;
}

.blog-summary {
    display: flex;
    align-items: center;
    color: #ccc;
    font-size: 14px;
}

.pagination-container {
    display: flex;
    justify-content: center;
    padding: 20px 0;
}

/* Element Plus 样式覆盖 */
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
    border-color: #8a2be2;
    background-color: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2);
}

:deep(.el-input__inner) {
    color: #ffffff;
}

:deep(.el-input__inner::placeholder) {
    color: rgba(255, 255, 255, 0.6);
}

:deep(.el-select .el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    box-shadow: none;
}

:deep(.el-select .el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.4);
    background-color: rgba(255, 255, 255, 0.15);
}

:deep(.el-select .el-input__wrapper.is-focus) {
    border-color: #8a2be2;
    background-color: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2);
}

:deep(.el-button) {
    background: linear-gradient(90deg, #8a2be2, #9370db);
    border: none;
    color: white;
    font-weight: 500;
    padding: 12px 20px;
    border-radius: 8px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(138, 43, 226, 0.3);
}

:deep(.el-button:hover) {
    background: linear-gradient(90deg, #9370db, #8a2be2);
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(138, 43, 226, 0.4);
}

:deep(.el-button:active) {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(138, 43, 226, 0.3);
}

:deep(.el-pagination) {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
}

:deep(.el-pagination .el-pager li) {
    background-color: rgba(255, 255, 255, 0.1);
    color: #ffffff;
    border-radius: 4px;
    margin: 0 2px;
}

:deep(.el-pagination .el-pager li.active) {
    background-color: #8a2be2;
    color: white;
}

:deep(.el-pagination .el-pager li:hover) {
    color: #9370db;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
    background-color: rgba(255, 255, 255, 0.1);
    color: #ffffff;
    border-radius: 4px;
}

:deep(.el-pagination .btn-prev:hover),
:deep(.el-pagination .btn-next:hover) {
    color: #9370db;
}

/* 强力穿透 el-dialog 样式，保证玻璃磨砂和自定义效果 */
:deep(.el-dialog) {
    background: rgba(255,255,255,0.08) !important;
    backdrop-filter: blur(18px) saturate(180%) !important;
    -webkit-backdrop-filter: blur(18px) saturate(180%) !important;
    border-radius: 18px !important;
    box-shadow: 0 8px 32px rgba(138,43,226,0.18) !important;
    border: 1px solid rgba(255,255,255,0.18) !important;
}
:deep(.el-dialog__title) {
    color: #fd52e7 !important;
    font-size: 20px !important;
    font-weight: 600 !important;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3) !important;
}
:deep(.el-dialog__body) {
    background: transparent !important;
    padding: 30px !important;
}
:deep(.el-dialog__footer) {
    background: transparent !important;
    border-radius: 0 0 18px 18px !important;
    padding: 20px !important;
    margin: 0 !important;
}

.create-blog-dialog {
    background: rgba(255,255,255,0.08);
    backdrop-filter: blur(18px) saturate(180%);
    -webkit-backdrop-filter: blur(18px) saturate(180%);
    border-radius: 18px;
    box-shadow: 0 8px 32px rgba(138,43,226,0.18);
    border: 1px solid rgba(255,255,255,0.18);
}
.create-blog-dialog .el-dialog__title {
    color: #fd52e7;
    font-size: 20px;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}
.create-blog-dialog .el-dialog__body {
    background: transparent;
    padding: 30px;
}
.create-blog-dialog .el-dialog__footer {
    background: linear-gradient(90deg, rgba(138, 43, 226, 0.08), rgba(147, 112, 219, 0.08));
    border-radius: 0 0 18px 18px;
    padding: 20px;
    margin: 0;
}
.create-blog-form .el-form-item__label {
    color: #fff !important;
    font-size: 20px !important;
    font-weight: 600 !important;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
    letter-spacing: 1px;
}
.create-blog-form .el-input__wrapper {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    box-shadow: none;
    transition: all 0.3s ease;
}
.create-blog-form .el-input__wrapper:hover {
    border-color: rgba(255, 255, 255, 0.4);
    background-color: rgba(255, 255, 255, 0.15);
}
.create-blog-form .el-input__wrapper.is-focus {
    border-color: #8a2be2;
    background-color: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2);
}
.create-blog-form .el-input__inner {
    color: #ffffff;
}
.create-blog-form .el-input__inner::placeholder {
    color: rgba(255, 255, 255, 0.6);
}
.create-blog-dialog .cancel-btn {
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.2));
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: #ffffff;
    font-weight: 500;
    padding: 10px 20px;
    border-radius: 8px;
    transition: all 0.3s ease;
}
.create-blog-dialog .cancel-btn:hover {
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.3));
    border-color: rgba(255, 255, 255, 0.5);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 255, 255, 0.2);
}
.create-blog-dialog .confirm-btn {
    background: linear-gradient(90deg, #8a2be2, #9370db);
    border: none;
    color: white;
    font-weight: 500;
    padding: 10px 20px;
    border-radius: 8px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(138, 43, 226, 0.3);
}
.create-blog-dialog .confirm-btn:hover {
    background: linear-gradient(90deg, #9370db, #8a2be2);
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(138, 43, 226, 0.4);
}
.create-blog-dialog .confirm-btn:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(138, 43, 226, 0.3);
}
.dialog-row {
    display: flex;
    align-items: center;
    width: 100%;
}
.dialog-row :deep(.fr-selector) {
    width: 160px !important;
    min-width: 120px;
    margin-left: 0;
}

/* 穿透 el-form-item label 样式，确保字体变大且为白色 */
:deep(.el-form-item__label) {
    color: #fff !important;
    font-size: 20px !important;
    font-weight: 600 !important;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
    letter-spacing: 1px;
}
</style>