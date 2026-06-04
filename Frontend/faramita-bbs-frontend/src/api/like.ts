import type { ApiId } from '@/types'
import request from '@/utils/request'

export function toggleBlogLike(id: ApiId): Promise<number> {
    return request<number>({
        url: `/like/blogs/${id}`,
        method: 'post',
    })
}
