import type { ApiId, PageQuery } from './api'

export const ROOT_FOLDER_ID = 0

export interface FolderTree {
    id: ApiId
    name: string
    level: number
    children: FolderTree[]
    sortOrder: number
}

export interface FolderSaveDTO {
    parentId: ApiId
    name: string
}

export interface FolderRenameDTO {
    name: string
}

export interface FolderMoveDTO {
    targetParentId: ApiId
}

export interface FolderBlogsMoveDTO {
    blogIds: ApiId[]
    targetId: ApiId
}

export interface FolderBlogsPageQuery extends PageQuery {}
