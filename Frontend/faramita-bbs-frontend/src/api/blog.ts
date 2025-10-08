// # 博文接口

import type { Blog, BlogCreateDTO, BlogPageQueryVO, BlogUpdateDTO, BlogPageQueryDTO } from "@/types";
import request from "@/utils/request";


// 分页查询接口
export function getBlogListPage(uid: number, data: BlogPageQueryDTO): Promise<BlogPageQueryVO> {
    return request({
        url: `/${uid}/blog`,
        method: 'get',
        params: data // 修正为 params
    })
}

// 博文创建接口
export function createBlog(uid: number, data: BlogCreateDTO): Promise<string> {
    return request({
        url: `/${uid}/blog/create`,
        method: 'post',
        data
    })
}

// 博文查询接口
export function getBlog(uid: number, bloguid: string): Promise<Blog> {
    return request({
        url: `/${uid}/blog/${bloguid}`,
        method: 'get'
    })
}

// 博文删除接口
export function deleteBlog(uid: number, bloguid: string) {
    return request({
        url: `/${uid}/blog/${bloguid}`,
        method: 'delete'
    })
}

// 博文更新接口
export function updateBlog(uid: number,bloguid: string , data: BlogUpdateDTO) {
    return request({
        url: `/${uid}/blog/${bloguid}`,
        method: 'put',
        data: data
    })
}