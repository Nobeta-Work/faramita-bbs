import type { UserRole } from '@/types'

export const ROUTE_NAMES = {
    index: 'Index',
    blogList: 'BlogList',
    blogPublicDetail: 'BlogPublicDetail',
    blogLegacyDetail: 'BlogDetail',
    userProfile: 'UserProfile',
    workspace: 'Workspace',
    workspaceBlog: 'WorkspaceBlog',
    login: 'Login',
    register: 'Register',
    notFound: 'NotFound',
} as const

export const ROUTE_PATHS = {
    index: '/',
    blogList: '/blog',
    blogPublicDetail: '/blog/:id',
    blogLegacyDetail: '/blog/:bloguid',
    userProfile: '/:uid',
    workspace: '/workspace',
    workspaceBlog: '/workspace/blogs/:id',
    login: '/login',
    register: '/register',
} as const

export const PHASE_ONE_PLANNED_ROUTES = [
    {
        name: ROUTE_NAMES.workspace,
        path: ROUTE_PATHS.workspace,
        requiresAuth: true,
        roles: ['ROLE_USER'] satisfies UserRole[],
    },
    {
        name: ROUTE_NAMES.workspaceBlog,
        path: ROUTE_PATHS.workspaceBlog,
        requiresAuth: true,
        roles: ['ROLE_USER'] satisfies UserRole[],
    },
] as const

declare module 'vue-router' {
    interface RouteMeta {
        requiresAuth?: boolean
        roles?: UserRole[]
        title?: string
    }
}
