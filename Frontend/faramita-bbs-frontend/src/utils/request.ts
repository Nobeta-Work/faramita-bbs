import axios, {
    type AxiosError,
    type AxiosRequestConfig,
    type AxiosResponse,
    type InternalAxiosRequestConfig,
} from 'axios'
import { createDiscreteApi } from 'naive-ui'
import router from '@/router'
import { useUserStore } from '@/stores/user'
import {
    API_SUCCESS_CODE,
    LEGACY_API_SUCCESS_CODE,
    type ApiResponse,
    type TokenVO,
} from '@/types'
import { serializeQueryParams } from './params'

const { message, dialog } = createDiscreteApi(['message', 'dialog'])

interface RetriableRequestConfig extends InternalAxiosRequestConfig {
    _retry?: boolean
}

const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
    paramsSerializer: {
        serialize: serializeQueryParams,
    },
})

let refreshPromise: Promise<string | null> | null = null

function isApiResponse<T = unknown>(value: unknown): value is ApiResponse<T> {
    return typeof value === 'object' && value !== null && 'code' in value
}

function getResponseMessage(response: ApiResponse): string {
    return response.msg || response.message || 'Error'
}

function isSuccessCode(code: number): boolean {
    return code === API_SUCCESS_CODE || code === LEGACY_API_SUCCESS_CODE
}

async function requestNewAccessToken(refreshToken: string): Promise<string | null> {
    const refreshResponse = await axios.request<ApiResponse<TokenVO>>({
        baseURL: import.meta.env.VITE_API_BASE_URL,
        url: '/auth/refresh',
        method: 'post',
        params: { refreshToken },
        paramsSerializer: {
            serialize: serializeQueryParams,
        },
    })

    const payload = refreshResponse.data
    if (!isApiResponse<TokenVO>(payload) || !isSuccessCode(payload.code) || !payload.data) {
        return null
    }

    const userStore = useUserStore()
    userStore.setTokens(payload.data)
    return payload.data.accessToken
}

async function replayWithFreshToken(config: RetriableRequestConfig): Promise<unknown | null> {
    const userStore = useUserStore()

    if (config._retry || !userStore.refreshToken) {
        return null
    }

    config._retry = true

    refreshPromise = refreshPromise ?? requestNewAccessToken(userStore.refreshToken)
    const accessToken = await refreshPromise.finally(() => {
        refreshPromise = null
    })

    if (!accessToken) {
        return null
    }

    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${accessToken}`
    return service.request(config)
}

function promptLogin(messageText = '您未登录或身份已失效，请重新登录'): void {
    const userStore = useUserStore()

    dialog.warning({
        title: '访问拒绝',
        content: messageText,
        positiveText: '点击登录',
        negativeText: '取消',
        onPositiveClick: () => {
            userStore.logout()
            router.push('/login')
        },
    })
}

service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const userStore = useUserStore()

        if (userStore.token) {
            config.headers = config.headers || {}
            config.headers.Authorization = `Bearer ${userStore.token}`
        }

        return config
    },
    (error) => {
        console.error('Request Error:', error)
        return Promise.reject(error)
    },
)

service.interceptors.response.use(
    async (response: AxiosResponse) => {
        if (response.config.responseType === 'blob') {
            return response
        }

        const res = response.data
        if (!isApiResponse(res)) {
            return res
        }

        if (isSuccessCode(res.code)) {
            return res.data
        }

        if (res.code === 401) {
            const replayed = await replayWithFreshToken(response.config as RetriableRequestConfig)
            if (replayed !== null) {
                return replayed
            }

            promptLogin(getResponseMessage(res))
        } else {
            message.error(getResponseMessage(res))
        }

        return Promise.reject(new Error(getResponseMessage(res)))
    },
    async (error: AxiosError) => {
        console.error('Response Error:', error)

        const response = error.response
        if (response?.status === 401 && response.config) {
            const replayed = await replayWithFreshToken(response.config as RetriableRequestConfig)
            if (replayed !== null) {
                return replayed
            }

            promptLogin()
            return Promise.reject(error)
        }

        if (error.message.includes('Network Error')) {
            message.error('网络连接失败，请检查网络设置')
        } else if (error.message.includes('timeout')) {
            message.error('请求超时，请稍后重试')
        } else if (response) {
            switch (response.status) {
                case 400:
                    message.error('请求参数错误')
                    break
                case 403:
                    message.error('拒绝访问')
                    break
                case 404:
                    message.error('请求资源不存在')
                    break
                case 500:
                    message.error('服务器错误')
                    break
                default:
                    message.error('网络异常，请稍后重试')
            }
        }

        return Promise.reject(error)
    },
)

export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
    return service.request<unknown, T>(config)
}

export { service as requestClient }
export default request
