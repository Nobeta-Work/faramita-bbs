<template>
    <div class="top-nav-container">
        <img src="@/assets/images/logo/Faramita.png" class="faramita-logo">
        <div class="menu-container">
            <div class="main-menu">
                <router-link to="/" class="menu-item">
                    <span><el-icon><HomeFilled /></el-icon>主页</span>
                    <div class="menu-item-border"></div>
                </router-link>
                <router-link to="/1" class="menu-item">
                    <span><el-icon><UserFilled /></el-icon>个人中心</span>
                    <div class="menu-item-border"></div>
                </router-link>
                <router-link to="/1/blog" class="menu-item">
                    <span><el-icon><Management /></el-icon>博客列表</span>
                    <div class="menu-item-border"></div>
                </router-link>
            </div>
            <div class="user-menu">
                <router-link class="menu-item user" to="/login" v-if="!userStore.isAuthenticated">
                    <span><el-icon><Position /></el-icon>登录</span>
                </router-link>
                <router-link class="menu-item user" to="/register" v-if="!userStore.isAuthenticated">
                    <span><el-icon><Plus /></el-icon>注册</span>
                </router-link>
                <router-link class="menu-item user" :to="`/${userStore.userInfo?.id}`" v-if="userStore.isAuthenticated">欢迎您 @{{ userStore.userInfo?.nickname }}</router-link>
                <router-link @click="handleLogout" class="menu-item user" to="/login" v-if="userStore.isAuthenticated">退出</router-link>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useUserStore } from '@/stores/user';


const userStore = useUserStore()

onMounted(async () => {
    if (userStore.token && !userStore.userInfo) {
        await userStore.fetchUserInfo()
    }
})

//  退出登录
const handleLogout = () => {
    userStore.logout()
}
</script>

<style scoped>
.top-nav-container {
    width: 100%;
    height: 6%;
    position: absolute;
    z-index: 10;
    background-color: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    /* padding: 2px; */
}
.faramita-logo {
    height: 90%;
    margin-left: 5%;
    cursor: pointer;
}
.menu-container {
    height: 100%;
    margin-left: 5%;
    /* background-color: white; */
    flex-grow: 1;
    display: flex;
    justify-content: space-between;
}
.main-menu, .user-menu {
    height: 100%;
    display: flex;
    gap: 70px;
}
.user-menu {
    margin-right: 5%;
}
.menu-item {
    display: flex;
    flex-direction: column;
    justify-content: center;
    color: white;
    text-decoration: none;
    font-size: 20px;
    transition: all ease 0.3s;
    position: relative;
}
.menu-item:hover,
.menu-item.router-link-exact-active {
    color: rgb(253, 137, 236);
    text-shadow: 0 0 10px white;
}
.menu-item>span {
    height: 100%;
    align-content: center;
}
.menu-item-border {
    position: absolute;
    top: 0;
    width: 0;
    left: 50%;
    height: 5px;
    background-color: aqua;
    transform: translateX(-50%);
    transition: width ease 0.5s;
}
.menu-item:hover .menu-item-border,
.menu-item.router-link-exact-active .menu-item-border {
    width: 100%;
}
.menu-item.user {
    font-size: 16px;
}
.menu-item.user:hover {
    color: aqua;
}
.menu-item.user :deep(.el-icon) {
    transition: transform 0.3s ease;
}
.menu-item.user:hover :deep(.el-icon),
.menu-item.user.router-link-exact-active :deep(.el-icon) {
    transform: rotate(45deg);
}

</style>