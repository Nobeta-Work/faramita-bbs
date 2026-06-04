import type {
    ApiId,
    BlogEditDTO,
    BlogPageQuery,
    BlogPrivateDetailVO,
    BlogPublicBriefVO,
    BlogPublicDetailVO,
    BlogSaveDTO,
    PageResult,
} from '@/types'
import { normalizePageResult } from '@/utils/page'
import request from '@/utils/request'

export async function getPublicBlogPage(query: BlogPageQuery): Promise<PageResult<BlogPublicBriefVO>> {
    const page = await request<PageResult<BlogPublicBriefVO>>({
        url: '/blogs/page',
        method: 'get',
        params: query,
    })

    return normalizePageResult(page)
}

export function createPrivateBlog(data: BlogSaveDTO): Promise<ApiId> {
    return request<ApiId>({
        url: '/blogs/me',
        method: 'post',
        data,
    })
}

export function getPublicBlog(id: ApiId): Promise<BlogPublicDetailVO> {
    return request<BlogPublicDetailVO>({
        url: `/blogs/${id}`,
        method: 'get',
    })
}

export function getPrivateBlog(id: ApiId): Promise<BlogPrivateDetailVO> {
    return request<BlogPrivateDetailVO>({
        url: `/blogs/me/${id}`,
        method: 'get',
    })
}

export function updatePrivateBlog(id: ApiId, data: BlogEditDTO): Promise<void> {
    return request<void>({
        url: `/blogs/me/${id}`,
        method: 'put',
        data,
    })
}

export function deletePrivateBlog(id: ApiId): Promise<void> {
    return request<void>({
        url: `/blogs/me/${id}`,
        method: 'delete',
    })
}
