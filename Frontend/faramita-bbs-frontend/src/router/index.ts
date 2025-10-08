import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { useUserStore } from "../stores/user";

// 路由配置数组
const routes: Array<RouteRecordRaw> = [
    // 基础路由
    {
        path: '/',
        name: 'Index',
        // component: () => import('@/views/IndexView.vue'),
        redirect: '1',
        meta: {
            requiresAuth: false,
            title: '~彼岸论坛~',
        }
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
    // 用户个人中心
    {
        path: '/:uid',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue'),
        meta: {
            requiresAuth: false,
            title: '用户详情',
        },
    },
    // 用户博文列表
    {
        path: '/:uid/blog',
        name: 'BlogList',
        component: () => import('@/views/BlogListView.vue'),
        meta: {
            requiresAuth: false,
            title: '博文列表',
        },
    },
    // 博文详情
    {
        path: '/:uid/blog/:bloguid',
        name: 'BlogDetail',
        component: () => import('@/views/BlogDetailView.vue'),
        meta: {
            requiresAuth: false,
            title: '博文详情',
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
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title as string
    }

    if (requiresAuth && !userStore.isAuthenticated) {
        next('/login')
    } else {
        next()
    }

    if (from) {
        
    }
})

export default router