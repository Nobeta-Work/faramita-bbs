import type {
    ApiId,
    Blog,
    BlogCreateDTO,
    BlogEditDTO,
    BlogPageQuery,
    BlogPageQueryDTO,
    BlogPageQueryVO,
    BlogPrivateDetailVO,
    BlogPublicBriefVO,
    BlogPublicDetailVO,
    BlogSaveDTO,
    BlogUpdateDTO,
    PageResult,
} from '@/types'
import request from '@/utils/request'

const LEGACY_SORT_FIELD_MAP: Record<string, string> = {
    create_time: 'createTime',
    update_time: 'updateTime',
}

function normalizeId(id: ApiId | null | undefined): string {
    return id === null || id === undefined ? '' : String(id)
}

function normalizeNumber(id: ApiId | null | undefined): number {
    return id === null || id === undefined ? 0 : Number(id)
}

function toLegacyBlog(blog: BlogPublicBriefVO | BlogPublicDetailVO | BlogPrivateDetailVO): Blog {
    const tags = blog.tags ?? []

    return {
        bloguid: normalizeId(blog.id),
        title: blog.title,
        content: 'content' in blog ? blog.content ?? '' : '',
        summary: blog.summary ?? '',
        authorId: normalizeNumber(blog.author?.id),
        authorName: blog.author?.nickname ?? '',
        categoryId: 'folderId' in blog ? normalizeNumber(blog.folderId) : 0,
        bigCategoryId: 0,
        littleCategoryName: tags.map((tag) => tag.name).join(', '),
        isPublished: blog.isPublished,
        createTime: blog.createTime,
        updateTime: blog.updateTime,
    }
}

function toBlogPageQuery(data: BlogPageQueryDTO): BlogPageQuery {
    return {
        pageNum: data.page,
        pageSize: data.pageSize,
        keyword: data.keyword || undefined,
        authorId: data.authorId || undefined,
        sortField: LEGACY_SORT_FIELD_MAP[data.orderBy] ?? data.orderBy,
        sortOrder: data.sortOrder === 'asc' ? 'asc' : 'desc',
    }
}

export function getPublicBlogPage(query: BlogPageQuery): Promise<PageResult<BlogPublicBriefVO>> {
    return request<PageResult<BlogPublicBriefVO>>({
        url: '/blogs/page',
        method: 'get',
        params: query,
    })
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

// Legacy exports kept for current pages until Phase 2 rewires views.
export async function getBlogListPage(data: BlogPageQueryDTO): Promise<BlogPageQueryVO> {
    const page = await getPublicBlogPage(toBlogPageQuery(data))

    return {
        total: page.total,
        list: page.records.map(toLegacyBlog),
    }
}

export async function createBlog(data: BlogCreateDTO): Promise<string> {
    const blogId = await createPrivateBlog({
        title: data.title,
        folderId: 0,
    })

    return normalizeId(blogId)
}

export async function getBlog(bloguid: string): Promise<Blog> {
    const blog = await getPublicBlog(bloguid)
    return toLegacyBlog(blog)
}

export function deleteBlog(bloguid: string): Promise<void> {
    return deletePrivateBlog(bloguid)
}

export function updateBlog(bloguid: string, data: BlogUpdateDTO): Promise<void> {
    return updatePrivateBlog(bloguid, {
        folderId: 0,
        isPublished: data.isPublished === 1 ? 1 : 0,
        title: data.title,
        summary: data.summary,
        content: data.content,
        tagIds: [],
    })
}
