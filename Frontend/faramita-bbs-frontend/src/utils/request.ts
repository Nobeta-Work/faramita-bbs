import axios, { type InternalAxiosRequestConfig, type AxiosResponse} from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/stores/user";
import router from "@/router";

// 1.创建Axios实例
const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
})

// 2.请求拦截器
service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        // 2.1 请求前处理
        const userStore = useUserStore()

        // 2.1.1 添加token
        if (userStore.token) {
            config.headers = config.headers || {}
            config.headers['Authorization'] = `Bearer ${userStore.token}`
        }

        return config
    },
    (error) => {
        console.error('Request Error:', error)
        return Promise.reject(error)
    }
)

// 3.响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse) => {
        // 3.0检查是否为 Blob响应
        if (response.config.responseType === 'blob') {
            return response
        }

        const res = response.data
        // 3.1获取响应code
        if (res.code !== 1) {
            // 3.1.1响应逻辑性失败
            console.error(res.message || 'Error')
            if (res.message) {
                ElMessage.error(res.message)
            }
            // 3.1.2处理401错误响应
            if (res.code === 401) {
                ElMessageBox.confirm('访问拒绝','您未登录或身份已失效，请重新登录',{
                    confirmButtonText: '点击登录',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    const userStore = useUserStore()
                    userStore.logout()
                    router.push('/login')
                })
            }

            return Promise.reject(new Error(res.message || 'Error'))
        }

        // 返回响应数据
        return res.data
    },
    (error) => {
        console.error('Response Error:' + error)
        
        // 处理错误类型
        if (error.message.includes('Network Error')) {
            ElMessage.error('网络连接失败，请检查网络设置')
        } else if (error.message.includes('timeout')) {
            ElMessage.error('请求超时，请稍后重试')
        } else if (error.response) {
            // 处理HTTP错误状态码
            switch (error.response.status) {
                case 400:
                    ElMessage.error('请求参数错误')
                    break
                case 401:
                    ElMessage.error('未授权，请登录')
                    break
                case 403:
                    ElMessage.error("拒绝访问")
                    break
                case 404:
                    ElMessage.error('请求资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error('网络异常，请稍后重试')
            }
        }

        // 返回
        return Promise.reject(error)
    }
)

export default service