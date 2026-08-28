import type { ApiId, PageQuery } from './api'
import type { UserBriefVO } from './user'

export type CommentStatus = -2 | -1 | 0 | 1

export interface CommentVO {
  id: ApiId
  blogId: ApiId
  parentId: ApiId
  rootId: ApiId
  content: string | null
  likeCount: number
  status: CommentStatus
  author: UserBriefVO
  replyTo: UserBriefVO | null
  replyCount: number
  replies: CommentVO[]
  createTime: string
  updateTime: string
}

export interface CommentPageQuery extends PageQuery {}

export interface CommentSaveDTO {
  blogId: ApiId
  parentId?: ApiId
  content: string
}
