import { getCurrentUserInfo } from "@/api/user";
import type { UserInfo, UserState } from "@/types";
import { defineStore } from "pinia";

export const useUserStore = defineStore('user', {
    state: (): UserState => {
        return {
            token: localStorage.getItem('token') || null,
            userInfo: null
        }
    },
    getters: {
        isAuthenticated: (state: UserState) => {
            return !!state.token    // 将state.token转化为布尔值
        },
    },
    actions: {
        setToken(token: string | null): void {
            this.$state.token = token;
            if (token) {
                localStorage.setItem('token', token)
            } else {
                localStorage.removeItem('token')
            }
        },
        setUserInfo(userInfo: UserInfo | null): void {
            const token = userInfo?.token
            if (token) {
                this.setToken(token)
            }
            this.$state.userInfo = userInfo
        },
        async fetchUserInfo(): Promise<void> {
            if (!this.token) {
                return
            }

            try {
                const response:UserInfo = await getCurrentUserInfo()
                this.setUserInfo(response)
            } catch (error) {
                console.error('获取用户信息失败:', error)
                this.logout()
            }
        },
        // ? 用户登出
        logout(): void {
            this.setToken(null)
            this.setUserInfo(null)
        }
    }
})