<script setup lang="ts">
import { h, ref, computed } from 'vue'
import { RouterLink, useRouter, useRoute } from 'vue-router'
import { NLayoutHeader, NMenu, NButton, NAvatar, NDropdown, NSpace, NIcon, NSwitch, NDrawer, NDrawerContent } from 'naive-ui'
import {
  BookOutline,
  BriefcaseOutline,
  HomeOutline,
  LogOutOutline,
  MenuOutline,
  Moon,
  Person,
  PersonCircleOutline,
  ShieldCheckmarkOutline,
  Sunny,
} from '@vicons/ionicons5'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { resolveAvatarUrl } from '@/utils/avatar'

const themeStore = useThemeStore()
const { isDark } = storeToRefs(themeStore)
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const showMobileMenu = ref(false)
const userAvatarUrl = computed(() => resolveAvatarUrl(userStore.userInfo?.avatar))

// 默认头像图标渲染函数
const renderDefaultAvatar = () => h(NIcon, null, { default: () => h(Person) })

const activeKey = computed(() => {
  if (route.path === '/') return 'home'
  if (route.path.startsWith('/blog')) {
    return 'blog'
  }
  if (route.path.startsWith('/workspace')) return 'workspace'
  if (route.path.startsWith('/admin')) return 'admin'
  return null
})

const menuOptions = computed(() => {
  const options = [
    {
      label: () => h(RouterLink, { to: '/' }, { default: () => '首页' }),
      key: 'home',
      icon: () => h(NIcon, null, { default: () => h(HomeOutline) }),
    },
    {
      label: () => h(RouterLink, { to: '/blog' }, { default: () => '博客' }),
      key: 'blog',
      icon: () => h(NIcon, null, { default: () => h(BookOutline) }),
    },
    {
      label: () => h(RouterLink, { to: '/workspace' }, { default: () => '工作台' }),
      key: 'workspace',
      icon: () => h(NIcon, null, { default: () => h(BriefcaseOutline) }),
    },
  ]

  if (userStore.hasAnyRole(['ROLE_ADMIN'])) {
    options.push({
      label: () => h(RouterLink, { to: '/admin' }, { default: () => '管理' }),
      key: 'admin',
      icon: () => h(NIcon, null, { default: () => h(ShieldCheckmarkOutline) }),
    })
  }

  return options
})

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
    if (userStore.userInfo?.id) {
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
            <span class="logo-text">Para BBS</span>
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
          <div class="mobile-only mobile-menu-trigger">
            <n-button
              text
              aria-label="打开导航菜单"
              class="mobile-menu-button"
              @click="showMobileMenu = true"
            >
              <n-icon :component="MenuOutline" />
            </n-button>
          </div>
        </n-space>
      </div>
    </div>

    <!-- Mobile Drawer -->
    <n-drawer v-model:show="showMobileMenu" :width="280" placement="right">
      <n-drawer-content title="菜单">
        <n-menu :options="menuOptions" :value="activeKey" @update:value="showMobileMenu = false" />
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
  font-family: 'ZCOOL KuaiLe', sans-serif;
}

.logo-text {
  font-weight: bold;
  background: linear-gradient(120deg, #ff66c4 30%, #41d1ff);
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

@media (max-width: 900px) {
  .desktop-only {
    display: none;
  }
  .mobile-only {
    display: flex;
  }

  .nav-header {
    padding: 0 12px;
  }

  .nav-content {
    width: 100%;
    max-width: none;
    gap: 8px;
  }

  .left-section {
    min-width: 0;
    flex: 1 1 auto;
  }

  .logo {
    min-width: 0;
    overflow: hidden;
  }

  .logo-text {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 1.55rem;
  }

  .actions {
    flex: 0 0 auto;
    min-width: 0;
  }

  .actions :deep(.n-space) {
    gap: 6px !important;
  }

  .mobile-menu-trigger {
    align-items: center;
  }

  .mobile-menu-button {
    width: 38px;
    height: 38px;
    padding: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }
}

.mobile-menu-button {
  font-size: 24px;
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
.nav-header {
  height: 70px;
  padding: 0 18px;
  border-bottom: 1px dashed var(--line-color) !important;
  background: var(--modal-bg) !important;
}

.nav-content {
  max-width: 1120px;
  font-family: 'Agbalumo', 'ZCOOL KuaiLe', sans-serif;
}

.logo-text {
  font-family: 'Agbalumo', 'ZCOOL KuaiLe', sans-serif;
  font-size: 2rem;
  font-weight: 400;
}

:deep(.n-menu-item-content-header) {
  font-size: 1.08rem;
}

:deep(.n-menu-item-content__tab) {
  font-size: 1.08rem;
}

:deep(.n-button__content) {
  font-size: 1.02rem;
}

:deep(.n-menu-item-content) {
  border-radius: 999px;
}

:deep(.n-menu-item-content--selected) {
  color: var(--accent-color) !important;
  background: var(--card-hover);
}

:deep(.n-switch) {
  --n-rail-color: var(--bg-secondary) !important;
  --n-rail-color-active: var(--accent-color) !important;
}
</style>
