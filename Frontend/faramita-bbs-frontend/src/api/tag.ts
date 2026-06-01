import type { PageResult, TagBriefVO, TagPageQuery, TagSaveDTO } from '@/types'
import { normalizePageResult } from '@/utils/page'
import request from '@/utils/request'

export async function getTagPage(params: TagPageQuery): Promise<PageResult<TagBriefVO>> {
    const page = await request<PageResult<TagBriefVO>>({
        url: '/tags',
        method: 'get',
        params,
    })

    return normalizePageResult(page)
}

export function createTag(data: TagSaveDTO): Promise<TagBriefVO> {
    return request<TagBriefVO>({
        url: '/tags',
        method: 'post',
        data,
    })
}
