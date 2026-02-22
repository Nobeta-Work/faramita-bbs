// # API相关类型定义
import type { User, Blog } from '@/types'

// API响应基础结构
export interface ApiResponse<T = any> {
    code: number;
    message: string;
    data: T;
}
export interface PageQueryDTO< T = any> {
    page: number;
    pageSize: number;
    query: T;
}

// 获取个人资料响应
export interface ProfileResponse {
    user: User;
    blogList: Array<Blog>;
}

// 分页查询参数类
export interface BlogPageQueryDTO {
    page: number;
    pageSize: number;
    bigCategoryId: number;
    keyword: string;
    orderBy: string;
    sortOrder: string;
    litteCategoryName: string;
    categoryId: string;
    authorId: number;
}
// 分页查询响应类
export interface BlogPageQueryVO {
    total: number,
    list: Array<Blog>
}

// 博文创建参数类
export interface BlogCreateDTO {
    title: string
    bigCategoryId: number,
    littleCategoryName: string,
    authorName: string
}

// 博文创建响应类
export interface BlogCreateVO {
    bloguid: string
}

// 博文更新请求类
export interface BlogUpdateDTO {
    title: string
    content: string
    summary: string
    littleCategoryName: string
    bigCategoryId: number
    isPublished: number
}
