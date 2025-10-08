<template>
    <div class="profile-container">
        <!-- 背景层 -->
        <div class="background-layer">

        </div>

        <!-- 内容层 -->
        <div class="content-layer">
            <div class="user-profile-container">
                <div class="avatar-container">
                    <div class="avatar-square" />
                    <div class="avatar-square" />
                    <div class="avatar-circle" />
                    <el-upload
                        class="avatar-uploader"
                        v-if="validateUser()"
                        :show-file-list="false"
                        :before-upload="beforeAvatarUpload"
                        :http-request="handleAvatarUpload"
                        accept="images/*"
                    >
                        <el-avatar :src="avatarUrl" :size="200" style="cursor: pointer;"/>
                    </el-upload>
                    <el-avatar v-else :src="avatarUrl" :size="200"/>
                </div>
                <div class="user-info-container">
                    <div class="show-nickname">>> Here is <span>{{ user.nickname }}</span> <<</div>
                    <el-descriptions title="个人资料" column="2">
                        <template #extra v-if="validateUser()">
                            <span @click="openUpdateUserProfileDialog">修改个人资料</span>
                        </template>
                        <el-descriptions-item label="昵称:">{{ user.nickname }}</el-descriptions-item>
                        <el-descriptions-item label="uid:">{{ user.id }}</el-descriptions-item>
                        <el-descriptions-item label="性别:">{{ explainSex(user.sex) }}</el-descriptions-item>
                        <el-descriptions-item label="种族:">{{ user.race }}</el-descriptions-item>
                        <el-descriptions-item label="个性签名">
                            <template #default>
                                <div class="signature-content">
                                    {{ user.signature }}
                                </div>
                            </template>
                        </el-descriptions-item>
                    </el-descriptions>
                </div>
            </div>
            <div class="blog-list-container">
                <button class="more-btn" @click="router.push(`${uid}/blog`)">+更多</button>
                <div class="blog-list-item" v-for="blog in blogList" :key="blog.bloguid"
                :class="BlogUtils.getCategoryClass(blog.bigCategoryId)">
                    <div class="blog-header" @click="openBlog(blog.bloguid)">
                        <div class="blog-title">
                           [ {{ blog.title }} ]
                        </div>
                        <div class="blog-category">
                            <span :class="BlogUtils.getCategoryClass(blog.bigCategoryId)">{{ BlogUtils.bigIdToString(blog.bigCategoryId) }}</span>
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
            <div>
                <el-dialog v-model="showUpdateUserProfileDialog" title="修改个人资料" width="500px"
                :before-close="handleDialogClose" custom-class="profile-dialog" class="update-profile-dialog">
                    <el-form ref="profileFormRef" :model="profileForm" :rules="profileFormRules" label-width="80px" label-position="right" class="update-profile-form">
                        <el-form-item label="昵称" prop="nickname">
                            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" clearable/>
                        </el-form-item>
                        <el-form-item label="密码" prop="password">
                            <el-input v-model="profileForm.password" type="password" show-password="true" placeholder="请输入密码" clearable/>
                        </el-form-item>
                        <el-form-item label="确认密码" prop="password2" v-show="profileForm.password">
                            <el-input v-model="profileForm.password2" type="password" show-password="true" placeholder="请输入密码" clearable/>
                        </el-form-item>
                        <el-form-item label="性别" prop="sex">
                            <el-radio-group v-model="profileForm.sex">
                                <el-radio :label="0">女</el-radio>
                                <el-radio :label="1">男</el-radio>
                                <el-radio :label="2">神秘</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item label="种族" prop="race">
                            <el-input
                                v-model="profileForm.race"
                                placeholder="请输入种族"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="个性签名" prop="signature">
                            <el-input
                                v-model="profileForm.signature"
                                type="textarea"
                                :rows="3"
                                placeholder="请输入个性签名"
                                maxlength="300"
                                show-word-limit
                            />
                        </el-form-item>
                    </el-form>
                    <template #footer>
                        <span class="dialog-footer">
                            <el-button @click="handleDialogClose" class="cancel-btn">取消</el-button>
                            <el-button type="primary" @click="handleProfileSubmit" class="confirm-btn">确认修改</el-button>
                        </span>
                    </template>
                </el-dialog>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { downloadAvatar } from '@/api/file';
import { getProfileByUid, updateAvatar, updateProfile } from '@/api/user';
import { type Blog, type ProfileRespose, type User, DateUtils, BlogUtils } from '@/types';
import { nextTick, onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus';
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const uid = Number(route.params.uid)

const user: User = reactive({
    id: 0,
    nickname: '',
    username: '',
    password: '',
    avatar: '',
    sex: 0,
    race: '',
    signature: '',
    createTime: '',
    updateTime: ''
})
const blogList: Array<Blog> = reactive([])
const profile: ProfileRespose = reactive({
    user,
    blogList
})
// # avatarAPI => 根据avatar: string调用api获得头像数据
const avatarUrl = ref('')
const fetchAvatar = async () => {
    if (!user.avatar) {
        avatarUrl.value = ''
        return
    }

    try {
        // 调用downloadAvatar API
        const blob = await downloadAvatar(user.avatar)

        // 将blob数据转换为URL
        const url = URL.createObjectURL(blob)

        // 更新avatarUrl
        avatarUrl.value = url
    } catch (error) {
        console.error('>获取头像失败:', error)
        avatarUrl.value = ''
    }
}
onUnmounted(() => {
    // 释放临时URL
    if (avatarUrl.value) {
        URL.revokeObjectURL(avatarUrl.value)
    }
})
// # 发起请求，获得个人资料页数据
const fetchProfile = async () => {
    try {
        // 调用查询资料API
        const response = await getProfileByUid(uid)
        
        Object.assign(profile.user, response.user)

        blogList.splice(0, blogList.length, ...response.blogList)

        await nextTick()    // 使用nextTick确保视图更新后输出日志

        console.log('>查询个人资料成功 => user:', user)
        console.log('>查询个人资料成功 => blogList:', blogList)

        // 获取头像
        await fetchAvatar()
    } catch (error) {
        console.log('>查询个人资料失败',uid)
    }
}
// 在访问页面，页面加载时调用
onMounted(() => {
    fetchProfile()
})

// # 修改头像

// 上传校验
const beforeAvatarUpload = (file: any) => {
    // 身份校验
    if (!validateUser()) {
        return false
    }

    // 文件类型校验
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
        ElMessage.error('请选择文件图片!')
        return false
    }

    // 文件大小校验
    const isLt10M = file.size / 1024 / 1024 < 10
    if (!isLt10M) {
        ElMessage.error('头像大小不超过10MB')
        return false
    }
}

// 处理头像上传
const handleAvatarUpload = async (options: UploadRequestOptions) => {
    const { file } = options

    try {

        // 启用预览
        const confirmUpload = await previewAvatar(file)
        if (!confirmUpload) {
            return
        }

        // 本地预览URL
        const localPreviewURL = URL.createObjectURL(file)

        // 调用头像上传API
        const response = await updateAvatar(uid, file)

        // 更新本地头像显示
        avatarUrl.value = localPreviewURL

        // 更新用户信息中的avatar
        user.avatar = response as unknown as string

        ElMessage.success('头像更新成功')

        // 清理临时URL
        onUnmounted(() => {
            if (avatarUrl.value) {
                URL.revokeObjectURL(avatarUrl.value)
            }
        })
    } catch (error) {
        console.error('>头像上传失败:', error)
    }
}

// 头像预览功能
const previewAvatar = (file : File): Promise<boolean> => {
    return new Promise((resolve) => {
        const reader = new FileReader()

        reader.onload = (e) => {
            const result = e.target?.result as string

            // 预览确认弹窗
            ElMessageBox.confirm('确认使用此图片作为头像吗？', '头像预览', {
                confirmButtonText: '确认',
                cancelButtonText: '取消',
                type: 'info',
                dangerouslyUseHTMLString: true,
                customClass: 'avatar-preview-dialog',
                confirmButtonClass: 'confirm-btn',
                cancelButtonClass: 'cancel-btn',
                message: `
                <div style="text-align: center;">
                    <img src="${result}" style="width: 200px; height: 200px; border-radius: 50%; object-fit:cover;" />
                    <p style="margin-top: 10px; color: white;">预览效果</p>
                </div>
                `
            }).then(() => {
                resolve(true)
            }).catch(() => {
                resolve(false)
            })
        }

        reader.onerror = () => {
            ElMessage.error('图片读取失败')
            resolve(false)
        }

        reader.readAsDataURL(file)
    })
}

// # 修改个人资料

// 相关变量
const showUpdateUserProfileDialog = ref(false)
const profileFormRef = ref<FormInstance>()
const profileForm = reactive({
    nickname: '',
    password: '',
    password2: '',
    sex: 0,
    race: '',
    signature: ''
})

// 表单验证规则
const profileFormRules: FormRules = {
    nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 1, max: 20, message: '昵称长度应在1到20个字符之间', trigger: 'blur' }
    ],
    password: [
        { required: false, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 30, message: '密码长度应在6到30个字符之间', trigger: 'blur' }
    ],
    password2: [
        { 
            required: false,
            validator: (rule, value, callback) => {
                if (!profileForm.password) {
                    callback()
                }
                if (!value) {
                    callback(new Error('请再次输入密码'))
                } else if (value !== profileForm.password) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
                if (rule) {
                    
                }
            }, 
            trigger: ['blur', 'change'] 
        }
    ],
    sex: [
        { required: true, message: '请选择性别', trigger: 'change' }
    ],
    race: [
        { required: true, message: '请输入种族', trigger: 'blur' },
        { min: 1, max: 20, message: '种族长度应在1到20个字符之间', trigger: 'blur' }
    ],
    signature: [
        { max: 100, message: '个性签名长度不能超过100个字符', trigger: 'blur' }
    ],
}
// 对话框开关方法
const openUpdateUserProfileDialog = () => {
    // 填充表单数据
    profileForm.nickname = user.nickname
    profileForm.sex = user.sex
    profileForm.race = user.race
    profileForm.signature = user.signature
    profileForm.password = ''
    profileForm.password2 = ''
    // 显示对话框
    showUpdateUserProfileDialog.value = true
}
const handleDialogClose = () => {
    showUpdateUserProfileDialog.value = false
    // 重置表单
    if (profileFormRef.value) {
        profileFormRef.value.resetFields()
    }
}

// * 提交个人资料修改
const handleProfileSubmit = async () => {
    if (!profileFormRef.value) return

    try {
        // 验证表单
        await profileFormRef.value.validate()

        // 准备请求数据
        const updateData: any = {
            id: user.id,
            nickname: profileForm.nickname,
            avatar: user.avatar,
            sex: profileForm.sex,
            race: profileForm.race,
            signature: profileForm.signature
        }

        // 密码修改特化
        if (profileForm.password) {
            updateData.password = profileForm.password
        }

        // 调用更新个人资料api
        console.log('>发起更新请求:', updateData)
        await updateProfile(uid, updateData)

        // 更新本地数据
        Object.assign(user, updateData)

        ElMessage.success('个人资料更新成功！')

        // 关闭对话框
        showUpdateUserProfileDialog.value = false

        // 重新获取资料同步数据
        await fetchProfile()
    } catch (error) {
        if (error instanceof Error) {
            console.error('>修改个人资料失败:', error)
        }
    }
}
// # blog相关跳转
// 跳转到特定blog
const openBlog = (bloguid: string) => {
    router.push(`/${uid}/blog/${bloguid}`)
}
// ? 功能方法
// 根据sex返回string性别
const explainSex = (sex: number) => {
    if (sex === 0) {
        return '女'
    } else if (sex === 1) {
        return '男'
    } else {
        return '神秘'
    }
}
// 校验访问用户与被访问用户是否一致
const validateUser = () => {
    const id = userStore.$state.userInfo?.id
    console.log('>校验用户=>','被访用户:',uid,',访问用户:',id)
    return id === uid
}

</script>

<style scoped>
.profile-container {
    height: 100%;
    width: 100%;
    background-image: url('@/assets/images/bg/dust.jpg');
    position: relative;
}
/* #region 背景层 */
.background-layer{
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
    background-color: rgba(0, 0, 0, 0.4);
}
/*  #endregion */
/* #region 内容层 */
.content-layer {
    margin-top: 3%;
    position: relative;
    z-index: 5;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.user-profile-container {
    width: 50%;
    display: flex;
    margin-top: 2%;
    overflow: visible;
}

.avatar-container {
    margin-left: 10%;
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: visible;
}
.avatar-container .active {
    cursor: pointer;
}

.avatar-square {
    position: absolute;
    z-index: 100;
    overflow: visible;

    width: 180px;
    height: 180px;
    border: 3px solid white;
    animation: rotateBorder 10s linear infinite;
}
.avatar-square:nth-child(2) {
    animation: rotateBorder 15s linear infinite;
}
.avatar-circle {
    position: absolute;
    z-index: 100;
    overflow: visible;

    border-radius: 50%;
    width: 210px;
    height: 210px;
    /* border: 1px solid black; */
    /* border-top: 3px dashed white; */
    border-bottom: 3px solid white;
    animation: rotateBorder 5s linear infinite;
}
@keyframes rotateBorder {
    0% {
        transform: rotate(0deg);
        border-color: #4d9de0;
        box-shadow: 0 0 10px #4d9de0;
    }
    25% {
        border-color: #e74c3c;
        box-shadow: 0 0 15px #e74c3c;
    }
    50% {
        border-color: #2ecc71;
        box-shadow: 0 0 20px #2ecc71;
    }
    75% {
        border-color: #f39c12;
        box-shadow: 0 0 15px #f39c12;
    }
    100% {
        transform: rotate(360deg);
        border-color: #4d9de0;
        box-shadow: 0 0 10px #4d9de0;
    }
}

.el-avatar {
    z-index: 1001;
    position: relative;
    
}

.user-info-container {
    margin-left: auto;
    margin-right: 10%;
    width: 50%;
    display: flex;
    flex-direction: column;
}

.show-nickname {
    color: rgb(249, 180, 215);
    font-size: 30px;
    font-family:'Gill Sans', 'Gill Sans MT', Calibri, 'Trebuchet MS', sans-serif;
    margin-left: 10%;
    margin-bottom: 3%;
}
.show-nickname>span {
    font-family: 'Times New Roman', Times, serif;
    background: linear-gradient(to right, rgb(255, 225, 0), rgb(0, 195, 255));
    background-size: 150% 150%;
    background-clip: text;
    -webkit-background-clip: text;
    color: transparent;
    animation: gradientText 3s ease infinite;
}
@keyframes gradientText {
    0% {
        background-position: 0% 50%;
    }
    50% {
        background-position: 100% 50%;
    }
    100% {
        background-position: 0% 50%;
    }
}

.user-info-container :deep(.el-descriptions) {
    border-left: 3px solid rgb(124, 102, 235);
    border-right: 3px solid rgb(52, 131, 221);
    width: 100%;
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 15px;
    padding-top: 2%;
}
.user-info-container :deep(.el-descriptions__body) {
    background-color: transparent;
    margin-left: 5%;
}
.user-info-container :deep(.el-descriptions__title) {
    color: rgb(183, 121, 241);
    margin-left: 10%;
    margin-bottom: 10px;
    font-size: large;
}
.user-info-container :deep(.el-descriptions__header) {
    border-bottom: 2px solid rgb(53, 238, 204);
}
.user-info-container :deep(.el-descriptions__extra) {
    color: rgba(77, 207, 216, 0.511);
    margin-right: 5%;
    font-size: small;
    cursor: pointer;
}
.user-info-container :deep(.el-descriptions__extra:hover) {
    color: rgb(48, 223, 235);
    text-decoration: underline;
}
.user-info-container :deep(.el-descriptions__label) {
    color: rgb(199, 164, 231);
    font-size: larger;
}
.user-info-container :deep(.el-descriptions__content) {
    color: #eee;
    font-size: larger;
}

.signature-content {
    color: #ccc;
    font-size: smaller;
}


.blog-list-container {
    width: 50%;
    height: auto;
    overflow: visible;
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
    align-items: center;
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
    border-bottom: 1px solid black;
    align-items: center;
    cursor: pointer;
}
.blog-title {
    margin-left: 3%;
    color: rgb(253, 202, 0);
    font-size: 17px;
}
.blog-category {
    margin-left: auto;
    color: white;
    margin-right: 5%;
}
.blog-category>span {
    color: rgb(38, 181, 209);
}
.blog-category>span.category-project {
    color: rgb(255, 183, 0);
}
.blog-category>span.category-tech {
    color: rgb(0, 132, 255);
}
.blog-category>span.category-algo {
    color: rgb(2, 186, 35);
}
.blog-category>span.category-game {
    color: rgb(216, 201, 32);
}
.blog-category>span.category-other {
    color: rgb(155, 112, 255);
}
.blog-category>span:last-child {
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
}
.blog-summary {
    display: flex;
    align-items: center;
    color: #ccc;
}

:deep(.update-profile-dialog) {
    background-color: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(4px);
}
:deep(.update-profile-dialog .el-dialog__title) {
    color: rgb(248, 124, 232);
    border-bottom: 3px solid pink;
}
:deep(.update-profile-form .el-form-item__label) {
    color: pink;
    font-size: 15px;
}

/* 修改个人资料对话框样式 - 暗色系紫色系 */
:deep(.update-profile-dialog) {
    background-color: rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(138, 43, 226, 0.3);
}


:deep(.update-profile-dialog .el-dialog__title) {
    color: #fd52e7;
    font-size: 20px;
    font-weight: 600;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

:deep(.update-profile-dialog .el-dialog__body) {
    background-color: transparent;
    padding: 30px;
}

:deep(.update-profile-dialog .el-dialog__footer .el-dialog__body) {
    background: linear-gradient(90deg, rgba(138, 43, 226, 0.1), rgba(147, 112, 219, 0.1));
    border-top: 1px solid rgba(138, 43, 226, 0.2);
    border-radius: 0 0 16px 16px;
    padding: 20px;
    margin: 0;
}

/* 表单样式 */
:deep(.update-profile-form .el-form-item__label) {
    color: #ffffff;
    font-size: 16px;
    font-weight: 500;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

:deep(.update-profile-form .el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    box-shadow: none;
    transition: all 0.3s ease;
}

:deep(.update-profile-form .el-input__wrapper:hover) {
    border-color: rgba(255, 255, 255, 0.4);
    background-color: rgba(255, 255, 255, 0.15);
}

:deep(.update-profile-form .el-input__wrapper.is-focus) {
    border-color: #8a2be2;
    background-color: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2);
}

:deep(.update-profile-form .el-input__inner) {
    color: #ffffff;
}

:deep(.update-profile-form .el-input__inner::placeholder) {
    color: rgba(255, 255, 255, 0.6);
}

/* 性别选择器样式 */
:deep(.update-profile-form .el-radio-group) {
    display: flex;
    width: 100%;
    gap: 10px;
}

:deep(.update-profile-form .el-radio) {
    margin-right: 0;
    padding: 6px 12px;
    border-radius: 6px;
    transition: all 0.2s ease;
}

:deep(.update-profile-form .el-radio__label) {
    color: #739efb;
    font-size: 14px;
    font-weight: 500;
    padding-left: 6px;
}

:deep(.update-profile-form .el-radio:hover .el-radio__label) {
    color: #f748d1;
}

:deep(.update-profile-form .el-radio.is-checked .el-radio__label) {
    color: #fd52e7;
}

:deep(.update-profile-form .el-radio__inner) {
    border-color: rgba(138, 43, 226, 0.5);
    transition: all 0.2s ease;
}

:deep(.update-profile-form .el-radio.is-checked .el-radio__inner) {
    background-color: #8a2be2;
    border-color: #8a2be2;
}

:deep(.update-profile-form .el-radio__inner:hover) {
    border-color: #9370db;
}

/* 按钮样式 */
:deep(.update-profile-dialog .cancel-btn) {
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.2));
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: #ffffff;
    font-weight: 500;
    padding: 10px 20px;
    border-radius: 8px;
    transition: all 0.3s ease;
}

:deep(.update-profile-dialog .cancel-btn:hover) {
    background: linear-gradient(90deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.3));
    border-color: rgba(255, 255, 255, 0.5);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 255, 255, 0.2);
}

:deep(.update-profile-dialog .confirm-btn) {
    background: linear-gradient(90deg, #8a2be2, #9370db);
    border: none;
    color: white;
    font-weight: 500;
    padding: 10px 20px;
    border-radius: 8px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(138, 43, 226, 0.3);
}

:deep(.update-profile-dialog .confirm-btn:hover) {
    background: linear-gradient(90deg, #9370db, #8a2be2);
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(138, 43, 226, 0.4);
}

:deep(.update-profile-dialog .confirm-btn:active) {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(138, 43, 226, 0.3);
}

/* 个性签名文本区域样式 */
:deep(.update-profile-form .el-textarea__inner) {
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    color: #ffffff;
    font-size: 14px;
    resize: vertical;
    transition: all 0.3s ease;
}

:deep(.update-profile-form .el-textarea__inner:hover) {
    border-color: rgba(255, 255, 255, 0.4);
    background-color: rgba(255, 255, 255, 0.15);
}

:deep(.update-profile-form .el-textarea__inner:focus) {
    border-color: #8a2be2;
    background-color: rgba(255, 255, 255, 0.2);
    box-shadow: 0 0 0 2px rgba(138, 43, 226, 0.2);
    outline: none;
}

:deep(.update-profile-form .el-textarea__inner::placeholder) {
    color: rgba(255, 255, 255, 0.6);
}

/* 字数统计样式 */
:deep(.update-profile-form .el-input__count) {
    background-color: rgba(138, 43, 226, 0.3);
    color: #ffffff;
    border-radius: 4px;
    padding: 2px 6px;
    font-size: 12px;
}

:deep(.update-profile-form .el-input__count .el-input__count-inner) {
    color: #fd52e7;
    font-weight: 500;
}

.more-btn {
    background-color: transparent;
    border: none;
    color:#f092e8;
    font-size: 15px;
    margin: 0;
    padding: 0;
    cursor: pointer;
}

/*  #endregion */
</style>

<style>
.avatar-preview-dialog {
    background-color: rgba(0, 0, 0, 0.5);
}
.avatar-preview-dialog .el-message-box__title {
    color: rgb(250, 131, 232);
    font-weight: 500;
    
}
.confirm-btn {
    background: linear-gradient(45deg, #b4228d, #1d86c3);
    color: rgb(0, 0, 0);
    font-size: 15px;
    border-radius: 12px;
    transition: all 0.3 ease;
    padding: 12px 30px;
    box-shadow: 0 4px 20px rgba(193, 0, 211, 0.587);
    border: none;
}
.confirm-btn:hover {
    background: linear-gradient(45deg, #f009b2, #01a2ff);
    /* transform: translateY(-2px); */
    color: black;
    font-weight: bold;
}
.cancel-btn {
    background: linear-gradient(45deg, #fdbeb7, #4c56b4);
    border: none;
    color: black;
    border-radius: 12px;
    padding: 12px 30px;
    font-weight: 600;
    transition: all 0.3s ease;
    box-shadow: 0 4px 20px rgba(202, 168, 164, 0.4);
}

.cancel-btn:hover {
    background: linear-gradient(45deg, #ee857a, #828deb);
    /* transform: translateY(-2px); */
    box-shadow: 0 8px 25px rgba(231, 76, 60, 0.6);
    color: black;
}
</style>