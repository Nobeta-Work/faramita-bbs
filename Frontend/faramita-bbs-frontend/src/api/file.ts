// # 文件传输相关API
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

// 头像图片上传
export function uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: '/uploadAvatar',
        method: 'post',
        data: formData
    })
}

// 头像图片下载
export function downloadAvatar(avatar: string) {
    return request({
        url: '/downloadAvatar',
        method: 'get',
        params: { avatar },
        responseType: 'blob'
    }).then((response: AxiosResponse<Blob>) => {
        // 返回实际Blob
        return response.data
    })
}