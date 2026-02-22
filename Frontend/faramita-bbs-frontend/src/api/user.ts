// # 登录相关API
import type { ProfileResponse, UserInfo } from '@/types';
import request from '@/utils/request';

// 登录API
export function login(data: {
        username: string
        password: string
}):Promise<UserInfo> {
    return request({
        url: '/login',
        method: 'post',
        data,
    })
}

// 注册API
export function register(data: {
        nickname: string
        username: string
        password: string
        sex: number
        race: string
        avatar: string
}) {
    return request({
        url: '/register',
        method: 'post',
        data,
    })
}

// 获取个人资料API
export function getProfileByUid(uid: number)
:Promise<ProfileResponse>{
    return request({
        url: `/${uid}`,
        method: 'get',
    })
}

// 修改个人资料API
export function updateProfile(uid: number,data: {
    id: number,
    password: string,
    nickname: string,
    avatar: string,
    sex: number,
    race: string,
    signature: string,
}) {
    return request({
        url: `/${uid}/profile`,
        method: 'put',
        data,
    })
}

// 个人资料头像更新API
export function updateAvatar(uid: number, file: File) {
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: `/${uid}/upload/avatar`,
        method: 'post',
        data: formData
    })
}

// 根据token获取用户信息
export function getCurrentUserInfo(): Promise<UserInfo> {
    const uid = 0
    return request({
        url: `${uid}/current`,
        method: 'get',
    })
}

