import { getCurrentUserInfo } from '@/api/user'
import type { TokenVO, UserInfo, UserRole, UserState } from '@/types'
import { defineStore } from 'pinia'

const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const TOKEN_EXPIRE_IN_KEY = 'tokenExpireIn'
const LEGACY_TOKEN_KEY = 'token'

function readAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY) || localStorage.getItem(LEGACY_TOKEN_KEY)
}

function normalizeRoles(roles?: UserRole[]): UserRole[] {
    return (roles ?? []).map((role) => {
        const value = String(role)
        return (value.startsWith('ROLE_') ? value : `ROLE_${value}`) as UserRole
    })
}

export const useUserStore = defineStore('user', {
    state: (): UserState => ({
        token: readAccessToken(),
        refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
        tokenExpireIn: localStorage.getItem(TOKEN_EXPIRE_IN_KEY),
        userInfo: null,
    }),
    getters: {
        isAuthenticated: (state: UserState) => !!state.token,
        roles: (state: UserState): UserRole[] => state.userInfo?.roles ?? [],
        hasAnyRole: (state: UserState) => (roles?: UserRole[]) => {
            if (!roles || roles.length === 0) {
                return true
            }

            const userRoles = state.userInfo?.roles ?? []
            return roles.some((role) => userRoles.includes(role))
        },
    },
    actions: {
        setToken(token: string | null): void {
            this.token = token

            if (token) {
                localStorage.setItem(ACCESS_TOKEN_KEY, token)
                localStorage.setItem(LEGACY_TOKEN_KEY, token)
            } else {
                localStorage.removeItem(ACCESS_TOKEN_KEY)
                localStorage.removeItem(LEGACY_TOKEN_KEY)
            }
        },
        setTokens(tokens: TokenVO): void {
            this.setToken(tokens.accessToken)
            this.refreshToken = tokens.refreshToken
            this.tokenExpireIn = tokens.expireIn

            localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken)
            localStorage.setItem(TOKEN_EXPIRE_IN_KEY, tokens.expireIn)
        },
        setUserInfo(userInfo: UserInfo | null): void {
            if (!userInfo) {
                this.userInfo = null
                return
            }

            if (userInfo.token) {
                this.setToken(userInfo.token)
            }

            if (userInfo.refreshToken) {
                this.refreshToken = userInfo.refreshToken
                localStorage.setItem(REFRESH_TOKEN_KEY, userInfo.refreshToken)
            }

            if (userInfo.tokenExpireIn) {
                this.tokenExpireIn = userInfo.tokenExpireIn
                localStorage.setItem(TOKEN_EXPIRE_IN_KEY, userInfo.tokenExpireIn)
            }

            this.userInfo = {
                ...userInfo,
                token: userInfo.token ?? this.token,
                refreshToken: userInfo.refreshToken ?? this.refreshToken,
                tokenExpireIn: userInfo.tokenExpireIn ?? this.tokenExpireIn,
                roles: normalizeRoles(userInfo.roles),
            }
        },
        async fetchUserInfo(clearOnFailure = true): Promise<boolean> {
            if (!this.token) {
                return false
            }

            try {
                const response: UserInfo = await getCurrentUserInfo()
                this.setUserInfo({
                    ...response,
                    token: this.token,
                    refreshToken: this.refreshToken,
                    tokenExpireIn: this.tokenExpireIn,
                })
                return true
            } catch (error) {
                console.error('获取用户信息失败:', error)
                if (clearOnFailure) {
                    this.logout()
                }
                return false
            }
        },
        logout(): void {
            this.token = null
            this.refreshToken = null
            this.tokenExpireIn = null
            this.userInfo = null

            localStorage.removeItem(ACCESS_TOKEN_KEY)
            localStorage.removeItem(REFRESH_TOKEN_KEY)
            localStorage.removeItem(TOKEN_EXPIRE_IN_KEY)
            localStorage.removeItem(LEGACY_TOKEN_KEY)
        },
    },
})
