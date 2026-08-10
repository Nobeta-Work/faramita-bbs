import type { PageQuery } from './api'

export interface TagBriefVO {
    id: number | string
    name: string
}

export interface TagPageQuery extends PageQuery {
    keyword?: string
}

export interface TagSaveDTO {
    name: string
    description?: string
}
