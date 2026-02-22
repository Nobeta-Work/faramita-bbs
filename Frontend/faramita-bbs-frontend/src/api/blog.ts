// # 博文接口

import type { Blog, BlogCreateDTO, BlogPageQueryVO, BlogUpdateDTO, BlogPageQueryDTO } from "@/types";
import request from "@/utils/request";


// 分页查询接口
export function getBlogListPage(data: BlogPageQueryDTO): Promise<BlogPageQueryVO> {
    return request({
        url: `/blog`,
        method: 'get',
        params: data
    })
}

// 博文创建接口
export function createBlog(data: BlogCreateDTO): Promise<string> {
    return request({
        url: `/blog/create`,
        method: 'post',
        data
    })
}

// 博文查询接口
export function getBlog(bloguid: string): Promise<Blog> {
    return request({
        url: `/blog/${bloguid}`,
        method: 'get'
    })
}

// 博文删除接口
export function deleteBlog(bloguid: string) {
    return request({
        url: `/blog/${bloguid}`,
        method: 'delete'
    })
}

// 博文更新接口
export function updateBlog(bloguid: string , data: BlogUpdateDTO) {
    return request({
        url: `/blog/${bloguid}`,
        method: 'put',
        data: data
    })
}