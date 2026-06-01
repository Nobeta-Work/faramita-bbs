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
