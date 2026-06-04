import request from '@/utils/request'

function toFileForm(file: File): FormData {
    const formData = new FormData()
    formData.append('file', file)
    return formData
}

export function uploadAvatar(file: File): Promise<string> {
    return request<string>({
        url: '/uploadAvatar',
        method: 'post',
        data: toFileForm(file),
    })
}

export function uploadImage(file: File): Promise<string | { data: string }> {
    return request<string | { data: string }>({
        url: '/uploadImage',
        method: 'post',
        data: toFileForm(file),
    })
}
