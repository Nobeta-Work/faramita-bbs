// # 文件传输相关API
import request from '@/utils/request'

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

// 博文图片上传
export function uploadImage(file: File) {
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: '/uploadImage',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
