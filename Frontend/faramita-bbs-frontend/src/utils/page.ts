import type { PageResult } from '@/types'

export function normalizePageResult<T>(page: PageResult<T>): PageResult<T> {
    return {
        ...page,
        total: Number(page.total),
        pageNum: Number(page.pageNum),
        pageSize: Number(page.pageSize),
        pages: Number(page.pages),
    }
}
