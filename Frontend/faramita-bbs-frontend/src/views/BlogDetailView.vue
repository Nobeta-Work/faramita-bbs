<template>
    <div class="blog-detail-container">
        <!-- 背景层 -->
        <div class="background-layer">

        </div>

        <!-- 内容层 -->
        <div class="content-layer">
            <!-- 博客区 -->
            <div class="blog-container">
                <!-- 博客头部信息 -->
                <div class="blog-header-container">
                    <div class="blog-title-container">
                        <div class="blog-title">
                            标题 
                            <span v-if="!isEditable">{{ blog.title }}</span>
                            <el-input v-else v-model="blog.title" placeholder="请输入标题" 
                            style="width: 200px; margin-left: 8px;" />
                        </div>
                        <div class="author-info">
                            作者 <span @click="router.push(`/${blog.authorId}`)" style="cursor: pointer;">{{ blog.authorName }}</span>
                        </div>
                        <div class="big-big-category">
                            大分类 
                            <span v-if="!isEditable">{{ BlogUtils.bigIdToString(blog.bigCategoryId) }}</span>
                            <select v-else v-model="blog.bigCategoryId" class="transparent-select">
                                <option value="" disabled selected>请选择大分类</option>
                                <option value="1">项目</option>
                                <option value="2">技术栈</option>
                                <option value="3">算法</option>
                                <option value="4">游戏</option>
                                <option value="5">余文</option>
                            </select>
                        </div>
                        <div class="blog-little-category">
                            小分类 
                            <span v-if="!isEditable">{{ blog.littleCategoryName }}</span>
                            <el-input v-else v-model="blog.littleCategoryName" placeholder="请输入小分类" style="width: 150px; margin-left: 8px;" />
                        </div>
                        <div class="blog-time">
                            {{ DateUtils.isoToDateTime(blog.updateTime) }}
                        </div>
                    </div>
                    <div class="summary-container">
                        摘要
                        <span v-if="!isEditable">{{ blog.summary }}</span>
                        <el-input v-else v-model="blog.summary" :rows="2" type="textarea" placeholder="请输入摘要|概述"></el-input>
                    </div>
                </div>
                <!-- 博客内容区域 -->
                <div class="blog-content-container">
                    <!-- 编辑器 -->
                    <div class="editor-toolbar" v-if="isEditable">

                    </div>
                    <div class="blog-detail">
                        <!-- Vditor编辑器 -->
                        <div v-if="isEditable" class="vditor-container">
                            <div ref="vditorRef" class="vditor-editor"></div>
                        </div>
                        <!-- 博文内容显示 -->
                        <div v-else class="blog-content">
                            <div ref="previewRef" class="vditor-preview" />
                        </div>
                    </div>
                    <!-- 博客操作区 -->
                    <div class="blog-actions">
                        <el-switch
                            v-model="blog.isPublished"
                            v-if="isEditable"
                            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
                            active-value="1" :active-action-icon="View"
                            inactive-value="0" :inactive-action-icon="Hide"
                        />
                        <div class="action-btns">
                            <el-button class="edit-btn" type="primary" v-if="isAuthor && !isEditable" @click="editBlog">编辑</el-button>
                            <el-button class="save-btn" type="success" v-if="isEditable" @click="saveBlog">保存</el-button>
                             <el-button class="cancel-btn" v-if="isEditable" @click="cancelEdit">取消</el-button>
                            <el-button class="delete-btn" v-if="isEditable" @click="handleDeleteBlog">删除</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { BlogUtils, DateUtils, type Blog } from '@/types';
import Vditor from 'vditor';
import { nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRoute } from 'vue-router';
import { deleteBlog, getBlog, updateBlog } from '@/api/blog';
import router from '@/router';
import 'vditor/dist/index.css'
import { ElMessage } from 'element-plus';
import { Hide, View } from '@element-plus/icons-vue'
// # 初始实例化对象
const userStore = useUserStore()
const route = useRoute()
const uid = Number(route.params.uid)
const bloguid: string = route.params.bloguid as string

// Blog实体类
const blog: Blog = reactive({
    bloguid: '',
    title: '',
    content: '',
    summary: '',
    authorId: 0,
    authorName: '',
    categoryId: 0,
    bigCategoryId: 0,
    littleCategoryName: '',
    isPublished: 0,
    createTime: '',
    updateTime: '',
})
// # 获取Blog实例
const fetchBlog = async () => {
    if (!uid || !bloguid) return
    
    try {
        const response = await getBlog(uid, bloguid)
        Object.assign(blog, response)

        await nextTick()    // 使用nextTick确保视图更新后输出日志

        console.log('>查询blog成功 => blog:', blog)

        isAuthor.value = validatePermisson()
    } catch (error) {
        console.error('>查询blog失败:', error)
    }
}
onMounted(() => {
    fetchBlog()
})
// ? 功能方法
const validatePermisson = () => {
    return userStore.$state.userInfo?.id === blog.authorId
}
// # Vditor相关
const vditorRef = ref<HTMLDivElement>()
const vditorInstance = ref<Vditor>()
const previewRef = ref<HTMLDivElement>()
const isEditable = ref(false)
const isAuthor = ref(validatePermisson())

// 初始化Vditor
const initVditor = async () => {
    if (!vditorRef.value) return

    await nextTick()

    vditorInstance.value = new Vditor(vditorRef.value, {
        height: 700,
        mode: 'ir', // 即时渲染
        theme: 'dark',
        icon: 'material',
        typewriterMode: true,
        placeholder: '开始编写博客内容',
        cache: {
            enable: false
        },
        toolbarConfig: {
            pin: true,
            hide: false
        },
        // 确保弹出层正确定位
        // popup: {
        //     enable: true,
        //     esc: true,
        //     follow: true,
        //     position: 'absolute'
        // },
        // 添加emoji配置
        // emoji: {
        //     enable: true,
        //     emojiPath: 'https://cdn.jsdelivr.net/npm/vditor@3.11.2/dist/images/emoji',
        //     inlineCode: true
        // },
        upload: {
            accept: 'image/*',
            handler: (files: File[]) => {
                // TODO文件上传api处理
                files.forEach(file => {
                    console.log('文件上传:',file.name)
                    
                })
                return Promise.resolve([]) as unknown as string | Promise<string> | Promise<null> | null
            }
        },
        toolbar: [
            // 'emoji',
            'headings',
            'bold',
            'italic',
            'strike',
            'link',
            '|',
            'list',
            'ordered-list',
            'check',
            'outdent',
            'indent',
            '|',
            'quote',
            'line',
            'code',
            'inline-code',
            'insert-before',
            'insert-after',
            '|',
            'table',
            '|',
            'undo',
            'redo',
            '|',
            'fullscreen',
            'edit-mode',
            {
                name: 'more',
                toolbar: [
                    'both',
                    'code-theme',
                    'content-theme',
                    'export',
                    'outline',
                    'preview',
                    'devtools',
                ],
            }
        ],
        after: () => {
            // 编辑器初始化完成后设置内容
            if (blog.content) {
                vditorInstance.value?.setValue(blog.content)
            }
        }
    })
}
// 渲染Markdown预览
const renderPreview = () => {
    if (previewRef.value && blog.content) {
        Vditor.preview(previewRef.value, blog.content, {
            mode: 'dark', // 使用暗色模式
            anchor: 1,
            markdown: {
                autoSpace: true,
                // fixTermLink: true,
                toc: true,
            },
            hljs: {
                enable: true,
                style: 'github-dark', // 使用github暗色代码高亮
                lineNumber: true
            },
            speech: {
                enable: false
            },
            math: {
                engine: 'KaTeX',
                inlineDigit: true,
                macros: {}
            }
            // 移除自定义主题配置，使用内置主题
        })
    }
}

// 在获取博客内容后渲染预览
watch(() => blog.content, (newContent) => {
    if (!isEditable.value && newContent) {
        nextTick(() => {
            renderPreview()
        })
    }
}, { immediate: true })

// 在编辑模式切换时重新渲染预览
watch(isEditable, (newValue) => {
    if (!newValue && blog.content) {
        nextTick(() => {
            renderPreview()
        })
    }
})
// 销毁Vditor实例
const destroyVditor = () => {
    if (vditorInstance.value) {
        vditorInstance.value.destroy()
        vditorInstance.value = undefined
    }
}

// 编辑博客
let originalBlogData: {
    title: string,
    bigCategoryId: number,
    littleCategoryName: string,
    content: string,
    isPublished: number,
} | null = null
const editBlog = () => {
    isEditable.value = true

    // 保存原始数据，以便取消时恢复
    originalBlogData = {
        title: blog.title,
        bigCategoryId: blog.bigCategoryId,
        littleCategoryName: blog.littleCategoryName,
        content: blog.content,
        isPublished: blog.isPublished
    }

    nextTick(() => {
        initVditor()
    })
}

// 保存博客
const saveBlog = async () => {
    if (vditorInstance.value) {
        blog.content = vditorInstance.value.getValue()
        // 调用API保存博客
        try {
            // 构造响应结构
            const blogUpdateDTO = {
                title: blog.title,
                content: blog.content,
                summary: blog.summary,
                bigCategoryId: blog.bigCategoryId,
                littleCategoryName: blog.littleCategoryName,
                isPublished: blog.isPublished
            }

            console.log('>保存博客<')
            await updateBlog(uid, bloguid, blogUpdateDTO)
            ElMessage.success('保存成功')
            // 保存成功后退出编辑模式
            isEditable.value = false
            destroyVditor()
        } catch (error) {
            console.error('>保存博客失败：', error)
        }
    }
}

// 取消编辑
const cancelEdit = () => {
    isEditable.value = false

    // 恢复原始数据
    if (originalBlogData) {
        blog.title = originalBlogData.title
        blog.bigCategoryId = originalBlogData.bigCategoryId
        blog.littleCategoryName = originalBlogData.littleCategoryName
        blog.content = originalBlogData.content
    }

    destroyVditor()
}

// 删除博客
const handleDeleteBlog = async () => {
    // 删除博客
    console.log('>删除博客')
    try {
        await deleteBlog(uid, bloguid)
        ElMessage.success('删除博客成功')

        router.push(`/${uid}/blog`)
    } catch (error) {
        console.error('>删除博客失败:', error)
    }
}

// 组件卸载
onUnmounted(() => {
    destroyVditor()
})

</script>

<style scoped>
.blog-detail-container {
    height: 100vh;
    background-image: url('@/assets/images/bg/bg02.png');
    background-size: cover;
    background-position: center;
    position: relative;
    overflow: auto;
}

/* 背景层 */
.background-layer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: auto;
    z-index: 0;
    background-color: rgba(0, 0, 0, 0.1);
}

/* 内容层 */
.content-layer {
    margin-top: 2%;
    position: relative;
    z-index: 5;
    width: 100%;
    min-height: 50%;
    display: flex;
    justify-content: center;
    padding: 1% 0;
}

/* 博客容器 */
.blog-container {
    width: 70%;
    max-width: 1200px;
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 16px;
    padding: 30px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(138, 43, 226, 0.3);
}

/* 博客头部容器 */
.blog-header-container {
    border-bottom: 2px solid rgba(43, 226, 211, 0.5);
    padding-bottom: 10px;
    margin-bottom: 10px;
    position: relative;
}

.blog-title-container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 15px;
    align-items: center;
}

.blog-title-container > div {
    background-color: rgba(138, 43, 226, 0.1);
    border-radius: 8px;
    padding: 10px 15px;
    border-left: 3px solid #3a2be2;
    transition: all 0.3s ease;
}

.blog-title-container > div:hover {
    background-color: rgba(138, 43, 226, 0.2);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(138, 43, 226, 0.3);
}

.blog-title-container span {
    color: #e6e6fa;
    font-weight: 600;
    margin-left: 8px;
}

.blog-title {
    color: #ffd700;
    font-size: 18px;
    font-weight: 600;
}

.author-info {
    color: #87ceeb;
    font-size: 16px;
}

.big-big-category {
    color: #98fb98;
    font-size: 16px;
}

.blog-little-category {
    color: #ffb6c1;
    font-size: 16px;
    margin-left: auto;
}

.blog-time {
    position: absolute;
    right: 0;
    bottom: 0;
    color: #dda0dd;
    font-size: 14px;
    background-color: transparent !important;
    border: none !important; /* 去掉边框 */
    font-style: italic;
    padding: 0 !important; /* 去掉内边距 */
    margin: 0 !important; /* 去掉外边距 */
}

/* 博客内容容器 */
.blog-content-container {
    margin-bottom: 30px;
}

/* Vditor编辑器容器 */
.vditor-container {
    border: 1px solid #333;
    border-radius: 8px;
    margin-bottom: 20px;
    position: relative;
    z-index: 1000;
    overflow: visible;
}

.vditor-editor {
    width: 100%;
    min-height: 400px;
    color: #e8e8e8 !important; /* 设置编辑器字体颜色为浅色 */
}

.vditor-preview {
    border-radius: 8px;
    padding: 20px;
    color: #e8e8e8 !important; /* 设置预览区域字体颜色为浅色 */
}

/* 确保Vditor内部元素的字体颜色 */
:deep(.vditor-reset) {
    color: #e8e8e8 !important;
}
/* 编辑器背景 */
:deep(.vditor-ir) {
    color: #e8e8e8 !important;
}

:deep(.vditor-wysiwyg) {
    color: #e8e8e8 !important;
}
/* 预览区域背景 */
.vditor-preview {
    background-color: rgba(0, 0, 0, 0.3) !important;
    border-radius: 8px;
    padding: 20px;
    color: #e8e8e8 !important;
}
:deep(.vditor-preview__action) {
    color: #e8e8e8 !important;
}



/* 博客操作区 */
.blog-actions {
    display: flex;
    justify-content: flex-end;
    gap: 15px;
    padding-top: 20px;
    border-top: 1px solid rgba(138, 43, 226, 0.3);
}

.action-btns {
    display: flex;
    gap: 10px;
}

/* 按钮样式 */
:deep(.el-button) {
    background: linear-gradient(135deg, #8a2be2, #9370db);
    border: none;
    color: white;
    font-weight: 500;
    padding: 10px 20px;
    border-radius: 6px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(138, 43, 226, 0.3);
}

:deep(.el-button:hover) {
    background: linear-gradient(135deg, #9370db, #8a2be2);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(138, 43, 226, 0.5);
}

:deep(.el-button:active) {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(138, 43, 226, 0.3);
}

:deep(.el-button.edit-btn) {
    background: linear-gradient(135deg, #4d9de0, #3498db);
}

:deep(.el-button.edit-btn:hover) {
    background: linear-gradient(135deg, #3498db, #4d9de0);
}

:deep(.el-button.save-btn) {
    background: linear-gradient(135deg, #2ecc71, #27ae60);
}

:deep(.el-button.save-btn:hover) {
    background: linear-gradient(135deg, #27ae60, #2ecc71);
}

:deep(.el-button.delete-btn) {
    background: linear-gradient(135deg, #e74c3c, #c0392b);
}

:deep(.el-button.delete-btn:hover) {
    background: linear-gradient(135deg, #c0392b, #e74c3c);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .blog-container {
        width: 90%;
        padding: 20px;
    }
    
    .blog-title-container {
        grid-template-columns: 1fr;
        gap: 10px;
    }
    
    .blog-actions {
        justify-content: center;
    }
    
    .action-btns {
        flex-direction: column;
        width: 100%;
    }
    
    :deep(.el-button) {
        width: 100%;
    }
}

/* 动画效果 */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.blog-container {
    animation: fadeIn 0.6s ease-out;
}

.blog-title-container > div {
    animation: fadeIn 0.8s ease-out;
}

.blog-title-container > div:nth-child(1) { animation-delay: 0.1s; }
.blog-title-container > div:nth-child(2) { animation-delay: 0.2s; }
.blog-title-container > div:nth-child(3) { animation-delay: 0.3s; }
.blog-title-container > div:nth-child(4) { animation-delay: 0.4s; }
.blog-title-container > div:nth-child(5) { animation-delay: 0.5s; }

/* 编辑模式下表单元素样式 */
:deep(.el-input) {
    background-color: transparent !important;
}
:deep(.el-input__wrapper) {
    background-color: transparent !important;
    box-shadow: none !important;
    border: none !important;
    padding: 0 !important;
}
:deep(.el-input__inner) {
    background-color: transparent !important;
    border: none !important;
    color: #e8e8e8 !important;
    border-radius: 0;
    padding-left: 0 !important;
    box-shadow: none !important;
}
:deep(.el-input__inner:focus) {
    border: none !important;
    box-shadow: none !important;
}


/* 下拉菜单动画效果 */
@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 针对自定义类的样式 */
:deep(.transparent-select) {
    background-color: transparent !important;
    color: #e8e8e8 !important;
    border: 1px solid rgba(138, 43, 226, 0.5) !important;
    outline: none;
    width: 150px;
    margin-left: 8px;
    padding: 8px 12px;
    appearance: none;
    -webkit-appearance: none;
    -moz-appearance: none;
    background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23e8e8e8' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
    background-repeat: no-repeat;
    background-position: right 12px center;
    background-size: 16px;
    border-radius: 6px;
    background-color: rgba(30, 30, 30, 0.7) !important;
    transition: all 0.3s ease;
    cursor: pointer;
    animation: slideDown 0.3s ease-out;
}

:deep(.transparent-select:hover) {
    border-color: rgba(138, 43, 226, 0.8) !important;
    box-shadow: 0 0 8px rgba(138, 43, 226, 0.5);
    transform: translateY(-2px);
}

:deep(.transparent-select:focus) {
    outline: none;
    border-color: #8a2be2 !important;
    box-shadow: 0 0 12px rgba(138, 43, 226, 0.8);
}

:deep(.transparent-select option) {
    background-color: rgba(30, 30, 30, 0.95);
    color: #e8e8e8;
    padding: 10px;
    transition: all 0.2s ease;
}

:deep(.transparent-select option:hover) {
    background-color: rgba(138, 43, 226, 0.3);
}

:deep(.transparent-select option:checked) {
    background-color: rgba(138, 43, 226, 0.5);
    color: #fff;
}

/* 兼容旧的 el-select 样式 */
:deep(.transparent-select .el-input__wrapper) {
    background-color: transparent !important;
    box-shadow: none !important;
    border: none !important;
    padding: 0 !important;
}
:deep(.transparent-select .el-input) {
    background-color: transparent !important;
}
:deep(.transparent-select .el-input__inner) {
    background-color: transparent !important;
    border: none !important;
    color: #e8e8e8 !important;
    border-radius: 0;
    padding-left: 0 !important;
    box-shadow: none !important;
}

/* 美化摘要容器 */
.summary-container {
    color: rgb(255, 0, 212);
    font-size: large;
    margin-top: 10px;
    margin-bottom: 5px;
    padding: 15px;
    background: linear-gradient(135deg, rgba(138, 43, 226, 0.1), rgba(147, 112, 219, 0.1));
    border-radius: 12px;
    border: 1px solid rgba(138, 43, 226, 0.2);
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(138, 43, 226, 0.1);
    animation: fadeIn 0.8s ease-out 0.6s both; /* 添加动画效果，延迟出现 */
}

.summary-container:hover {
    background: linear-gradient(135deg, rgba(138, 43, 226, 0.15), rgba(147, 112, 219, 0.15));
    border-color: rgba(138, 43, 226, 0.3);
    box-shadow: 0 6px 16px rgba(138, 43, 226, 0.2);
    transform: translateY(-2px);
}
.summary-container >span {
    color: white;
    font-size: medium;
    margin-left: 1%;
}

/* 美化摘要文本框 */
.summary-container :deep(.el-input) {
    width: 100%;
}

.summary-container :deep(.el-input__wrapper) {
    background-color: rgba(0, 0, 0, 0.3) !important;
    box-shadow: none !important;
    border: 1px solid rgba(138, 43, 226, 0.3) !important;
    border-radius: 8px !important;
    padding: 8px 12px !important;
    transition: all 0.3s ease;
}

.summary-container :deep(.el-input__wrapper:hover) {
    border-color: rgba(138, 43, 226, 0.5) !important;
    box-shadow: 0 0 10px rgba(138, 43, 226, 0.3);
}

.summary-container :deep(.el-input__wrapper.is-focus) {
    border-color: #8a2be2 !important;
    box-shadow: 0 0 12px rgba(138, 43, 226, 0.4);
}

.summary-container :deep(.el-input__inner) {
    background-color: transparent !important;
    border: none !important;
    color: #e8e8e8 !important;
    border-radius: 6px !important;
    padding: 8px 0 !important;
    font-size: 15px;
    line-height: 1.6;
    resize: vertical; /* 允许垂直调整大小 */
    min-height: 80px;
    max-height: 200px;
}

.summary-container :deep(.el-input__inner::placeholder) {
    color: rgba(232, 232, 232, 0.5) !important;
    font-style: italic;
}

  /* 非编辑模式下，为博客内容容器的子内容区域设置最大高度和滚动条 */
  .blog-content-container:not(.edit-mode) .vditor-preview,
  .blog-content-container:not(.edit-mode) .blog-actual-content {
    min-height: 500px;
    max-height: 1500px;
    overflow-y: auto;
    padding-right: 8px; /* 为滚动条留出空间 */
  }
  
  /* 优化滚动条样式 */
  .blog-content-container:not(.edit-mode) .vditor-preview::-webkit-scrollbar,
  .blog-content-container:not(.edit-mode) .blog-actual-content::-webkit-scrollbar {
    width: 8px;
  }
  
  .blog-content-container:not(.edit-mode) .vditor-preview::-webkit-scrollbar-track,
  .blog-content-container:not(.edit-mode) .blog-actual-content::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  .blog-content-container:not(.edit-mode) .vditor-preview::-webkit-scrollbar-thumb,
  .blog-content-container:not(.edit-mode) .blog-actual-content::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;
  }
  
  .blog-content-container:not(.edit-mode) .vditor-preview::-webkit-scrollbar-thumb:hover,
  .blog-content-container:not(.edit-mode) .blog-actual-content::-webkit-scrollbar-thumb:hover {
    background: #555;
  }
</style>