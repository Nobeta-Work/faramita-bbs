import type { LoginDTO, RegisterDTO, TokenVO } from '@/types'
import request from '@/utils/request'

export function login(data: LoginDTO): Promise<TokenVO> {
    return request<TokenVO>({
        url: '/auth/login',
        method: 'post',
        data,
    })
}

export function register(data: RegisterDTO): Promise<void> {
    return request<void>({
        url: '/auth/register',
        method: 'post',
        data,
    })
}

export function refresh(refreshToken: string): Promise<TokenVO> {
    return request<TokenVO>({
        url: '/auth/refresh',
        method: 'post',
        params: { refreshToken },
    })
}

export function logout(): Promise<void> {
    return request<void>({
        url: '/auth/logout',
        method: 'post',
    })
}
