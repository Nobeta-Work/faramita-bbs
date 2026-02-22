import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { useUserStore } from "../stores/user";
import MainLayout from '@/layouts/MainLayout.vue'

// 路由配置数组
const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        component: MainLayout,
        children: [
            {
                path: '',
                name: 'Index',
                component: () => import('@/views/IndexView.vue'),
                meta: {
                    requiresAuth: false,
                    title: 'Faramita BBS | 彼岸',
                }
            },
            {
                path: 'blog',
                name: 'BlogList',
                component: () => import('@/views/BlogListView.vue'),
                meta: {
                    requiresAuth: false,
                    title: '博客列表 | Faramita BBS',
                }
            },
            {
                path: 'blog/:bloguid',
                name: 'BlogDetail',
                component: () => import('@/views/BlogDetailView.vue'),
                meta: {
                    requiresAuth: false,
                    title: '博客详情 | Faramita BBS',
                }
            },
            {
                path: ':uid',
                name: 'UserProfile',
                component: () => import('@/views/UserProfile.vue'),
                meta: {
                    requiresAuth: false,
                    title: '个人主页 | Faramita BBS',
                }
            }
        ]
    },
    // 登录页面
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/LoginView.vue'),
        meta: {
            requiresAuth: false,
            title: '登录',
        },
    },
    // 注册页面
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/RegisterView.vue'),
        meta: {
            requiresAuth: false,
            title: '注册',
        },
    },
    // 404页面
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/NotFoundView.vue'),
        meta: {
            requiresAuth: false,
            title: '页面不存在',
        },
    },
]

// 创建路由实例
const router = createRouter({
    history: createWebHistory('/bbs'),
    routes,
})

// 路由守卫
router.beforeEach(async (to, _, next) => {
    const userStore = useUserStore()
    const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title as string
    }

    // 如果有 token 但没有用户信息，说明是刷新页面，需要重新获取用户信息
    if (userStore.token && !userStore.userInfo) {
        try {
            await userStore.fetchUserInfo()
        } catch (error) {
            console.error('Failed to fetch user info:', error)
        }
    }

    if (requiresAuth && !userStore.isAuthenticated) {
        next('/login')
    } else {
        next()
    }
})

export default router
