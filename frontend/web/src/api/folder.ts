import type {
    ApiId,
    BlogPrivateBriefVO,
    FolderBlogsMoveDTO,
    FolderBlogsPageQuery,
    FolderMoveDTO,
    FolderRenameDTO,
    FolderSaveDTO,
    FolderTree,
    PageResult,
} from '@/types'
import { normalizePageResult } from '@/utils/page'
import request from '@/utils/request'

export function createFolder(data: FolderSaveDTO): Promise<void> {
    return request<void>({
        url: '/folders',
        method: 'post',
        data,
    })
}

export function renameFolder(id: ApiId, data: FolderRenameDTO): Promise<void> {
    return request<void>({
        url: `/folders/${id}`,
        method: 'put',
        data,
    })
}

export function moveFolder(id: ApiId, data: FolderMoveDTO): Promise<void> {
    return request<void>({
        url: `/folders/${id}/move`,
        method: 'put',
        data,
    })
}

export function deleteFolder(id: ApiId): Promise<void> {
    return request<void>({
        url: `/folders/${id}`,
        method: 'delete',
    })
}

export function getCurrentUserFolderTree(): Promise<FolderTree> {
    return request<FolderTree>({
        url: '/folders/me',
        method: 'get',
    })
}

export async function getFolderBlogPage(
    id: ApiId,
    params: FolderBlogsPageQuery,
): Promise<PageResult<BlogPrivateBriefVO>> {
    const page = await request<PageResult<BlogPrivateBriefVO>>({
        url: `/folders/${id}/blogs`,
        method: 'get',
        params,
    })

    return normalizePageResult(page)
}

export function moveBlogsToFolder(data: FolderBlogsMoveDTO): Promise<void> {
    return request<void>({
        url: '/folders/blogs/move',
        method: 'put',
        data,
    })
}
