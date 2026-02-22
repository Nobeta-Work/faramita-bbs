<script setup lang="ts">
import { h, ref, computed, watch, onMounted } from 'vue'
import { RouterLink, useRouter, useRoute } from 'vue-router'
import { NLayoutHeader, NMenu, NButton, NAvatar, NDropdown, NSpace, NIcon, NSwitch, NDrawer, NDrawerContent } from 'naive-ui'
import { PersonCircleOutline, LogOutOutline, Moon, Sunny, MenuOutline, Person } from '@vicons/ionicons5'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { downloadAvatar } from '@/api/file'

const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const showMobileMenu = ref(false)
const userAvatarUrl = ref<string | undefined>(undefined)

// 默认头像图标渲染函数
const renderDefaultAvatar = () => h(NIcon, null, { default: () => h(Person) })

const activeKey = computed(() => {
  if (route.path === '/') return 'home'
  if (route.path.startsWith('/blog')) {
    // If it's a specific blog detail, maybe we still want to highlight 'blog'?
    // Usually yes.
    return 'blog'
  }
  return null
})

const fetchUserAvatar = async () => {
    if (userStore.userInfo?.avatar) {
        try {
            const blob = await downloadAvatar(userStore.userInfo.avatar)
            userAvatarUrl.value = URL.createObjectURL(blob)
        } catch (error) {
            console.error('加载头像失败', error)
        }
    } else {
        userAvatarUrl.value = undefined
    }
}

watch(() => userStore.userInfo?.avatar, () => {
    fetchUserAvatar()
})

onMounted(() => {
    if (userStore.isAuthenticated) {
        fetchUserAvatar()
    }
})

const menuOptions = [
  {
    label: () => h(RouterLink, { to: '/' }, { default: () => '首页' }),
    key: 'home'
  },
  {
    label: () => h(RouterLink, { to: '/blog' }, { default: () => '博客' }),
    key: 'blog'
  }
]

const userOptions = [
  {
    label: '个人中心',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(PersonCircleOutline) })
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(NIcon, null, { default: () => h(LogOutOutline) })
  }
]

function handleUserSelect(key: string) {
  if (key === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (key === 'profile') {
    if (userStore.userInfo?.id) { // changed uid to id based on User interface
      router.push(`/${userStore.userInfo.id}`)
    }
  }
}
</script>

<template>
  <n-layout-header bordered class="nav-header glass">
    <div class="nav-content">
      <div class="left-section">
        <div class="logo">
          <router-link to="/">
            <span class="logo-text">Faramita BBS</span>
          </router-link>
        </div>
      </div>

      <!-- Desktop Menu -->
      <div class="menu desktop-only">
        <n-menu mode="horizontal" :options="menuOptions" :value="activeKey" />
      </div>

      <div class="actions">
        <n-space align="center">
          <n-switch :value="isDark" @update:value="themeStore.toggleTheme">
            <template #checked-icon>
              <n-icon :component="Moon" />
            </template>
            <template #unchecked-icon>
              <n-icon :component="Sunny" />
            </template>
          </n-switch>
          
          <div v-if="userStore.isAuthenticated" class="desktop-only">
             <n-dropdown trigger="hover" :options="userOptions" @select="handleUserSelect">
               <n-avatar 
                 round 
                 size="small" 
                 :src="userAvatarUrl" 
                 :render-icon="renderDefaultAvatar"
               />
             </n-dropdown>
          </div>
          <div v-else class="desktop-only">
            <n-button text tag="a" href="/login" @click.prevent="router.push('/login')">
              登录
            </n-button>
          </div>

          <!-- Mobile Toggle -->
          <div class="mobile-only">
            <n-button text style="font-size: 24px" @click="showMobileMenu = true">
              <n-icon :component="MenuOutline" />
            </n-button>
          </div>
        </n-space>
      </div>
    </div>

    <!-- Mobile Drawer -->
    <n-drawer v-model:show="showMobileMenu" :width="240" placement="right">
      <n-drawer-content title="菜单">
        <n-menu :options="menuOptions" />
        <div class="mobile-user-actions">
           <div v-if="userStore.isAuthenticated">
             <div class="mobile-user-info" @click="handleUserSelect('profile')">
                <n-avatar 
                  round 
                  size="small" 
                  :src="userAvatarUrl" 
                  :render-icon="renderDefaultAvatar"
                />
                <span>{{ userStore.userInfo?.nickname }}</span>
             </div>
             <n-button block secondary type="error" @click="handleUserSelect('logout')" style="margin-top: 10px">
               退出登录
             </n-button>
           </div>
           <div v-else>
             <n-button block type="primary" @click="router.push('/login'); showMobileMenu = false">
               登录
             </n-button>
           </div>
        </div>
      </n-drawer-content>
    </n-drawer>
  </n-layout-header>
</template>

<style scoped>
.nav-header {
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 0 24px;
}

.nav-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 95%;
  width: 1200px;
  margin: 0 auto;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: bold;
  background: linear-gradient(120deg, #bd34fe 30%, #41d1ff);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-decoration: none;
}
a {
  text-decoration: none;
}

.mobile-only {
  display: none;
}

@media (max-width: 768px) {
  .desktop-only {
    display: none;
  }
  .mobile-only {
    display: block;
  }
}

.mobile-user-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(128,128,128,0.2);
}

.mobile-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 10px 0;
}
</style>
