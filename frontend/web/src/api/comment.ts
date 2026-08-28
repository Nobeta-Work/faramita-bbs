import type {
  ApiId,
  CommentPageQuery,
  CommentSaveDTO,
  CommentVO,
  PageResult,
} from '@/types'
import request from '@/utils/request'
import { normalizePageResult } from '@/utils/page'

export async function getCommentPage(
  blogId: ApiId,
  query: CommentPageQuery,
): Promise<PageResult<CommentVO>> {
  const page = await request<PageResult<CommentVO>>({
    url: `/blogs/comments/${blogId}`,
    method: 'get',
    params: query,
  })
  return normalizePageResult(page)
}

export function createComment(data: CommentSaveDTO): Promise<ApiId> {
  return request<ApiId>({
    url: '/blogs/comments',
    method: 'post',
    data,
  })
}

export function deleteComment(id: ApiId): Promise<void> {
  return request<void>({
    url: `/blogs/comments/${id}`,
    method: 'delete',
  })
}
