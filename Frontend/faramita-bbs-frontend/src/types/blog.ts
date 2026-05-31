import type { ApiId, PageQuery } from './api'
import type { TagBriefVO } from './tag'
import type { UserBriefVO } from './user'

export type PublishStatus = 0 | 1

export interface BlogBaseVO {
    id: ApiId
    title: string
    summary: string | null
    isPublished: PublishStatus
    likeCount: number
    createTime: string
    updateTime: string
    author: UserBriefVO
    tags: TagBriefVO[]
}

export interface BlogPublicBriefVO extends BlogBaseVO {}

export interface BlogPublicDetailVO extends BlogPublicBriefVO {
    content: string | null
    isLiked: boolean
}

export interface BlogPrivateBriefVO extends BlogBaseVO {
    folderId: ApiId
}

export interface BlogPrivateDetailVO extends BlogPrivateBriefVO {
    content: string | null
}

export interface BlogPageQuery extends PageQuery {
    keyword?: string
    authorId?: ApiId
    tagIds?: ApiId[]
}

export interface BlogSaveDTO {
    title: string
    folderId?: ApiId
}

export interface BlogEditDTO {
    folderId: ApiId
    isPublished: PublishStatus
    title: string
    summary?: string
    content?: string
    tagIds?: ApiId[]
}

// Legacy blog shape kept for current pages until Phase 2 rewires views.
export interface Blog{
    bloguid: string;
    title: string;
    content: string;
    summary: string;
    authorId: number;
    authorName: string;
    categoryId: number;
    bigCategoryId: number;
    littleCategoryName: string;
    isPublished: number;
    createTime: string;
    updateTime: string;
}

export class BlogUtils {
    static bigIdToString(bigId: string | number) {
        if (bigId == '1') {
            return '项目'
        } else if (bigId == '2') {
            return '技术栈'
        } else if (bigId == '3') {
            return '算法'
        } else if (bigId == '4') {
            return '游戏'
        } else if (bigId == '5') {
            return '余文'
        }
    }
    static getCategoryClass(bigCategoryId: number): string {
        const categoryMap: Record<number, string> = {
            1: 'category-project',
            2: 'category-tech', 
            3: 'category-algo',
            4: 'category-game',
            5: 'category-other'
        };
        return categoryMap[bigCategoryId] || '';
    }
}
