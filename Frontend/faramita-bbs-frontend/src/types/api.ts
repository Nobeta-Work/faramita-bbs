import type { Blog } from './blog'
import type { User } from './user'

export type ApiId = number | string
export type SortOrder = 'asc' | 'desc'

export const API_SUCCESS_CODE = 200
export const LEGACY_API_SUCCESS_CODE = 1

export interface ApiResponse<T = unknown> {
    code: number
    msg?: string
    message?: string
    data: T
}

export interface PageQuery {
    pageNum: number
    pageSize: number
    sortField?: string
    sortOrder?: SortOrder
}

export interface PageResult<T> {
    total: number
    pageNum: number
    pageSize: number
    pages: number
    records: T[]
}

export interface PageQueryDTO<T = unknown> {
    page: number
    pageSize: number
    query: T
}

export interface ProfileResponse {
    user: User
    blogList: Blog[]
}

export interface LegacyPageResult<T> {
    total: number
    list: T[]
}

export function toLegacyPageResult<T>(page: PageResult<T>): LegacyPageResult<T> {
    return {
        total: page.total,
        list: page.records,
    }
}

// v0.2 page contract kept for current pages until Phase 2 rewires views.
export interface BlogPageQueryDTO {
    page: number
    pageSize: number
    bigCategoryId: number
    keyword: string
    orderBy: string
    sortOrder: string
    litteCategoryName: string
    categoryId: string
    authorId: number
}

export interface BlogPageQueryVO {
    total: number
    list: Blog[]
}

export interface BlogCreateDTO {
    title: string
    bigCategoryId: number
    littleCategoryName: string
    authorName: string
}

export interface BlogCreateVO {
    bloguid: string
}

export interface BlogUpdateDTO {
    title: string
    content: string
    summary: string
    littleCategoryName: string
    bigCategoryId: number
    isPublished: number
}
