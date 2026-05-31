import type { PageResult, TagBriefVO, TagPageQuery, TagSaveDTO } from '@/types'
import request from '@/utils/request'

export function getTagPage(params: TagPageQuery): Promise<PageResult<TagBriefVO>> {
    return request<PageResult<TagBriefVO>>({
        url: '/tags',
        method: 'get',
        params,
    })
}

export function createTag(data: TagSaveDTO): Promise<TagBriefVO> {
    return request<TagBriefVO>({
        url: '/tags',
        method: 'post',
        data,
    })
}
