// # 用户相关类型定义

// 用户接口
export interface User {
    id: number; // uid，唯一标识
    username: string;   // 账号
    password: string;   // 密码
    nickname: string;   // 昵称
    avatar: string;     // 头像URL
    sex: number;        // 性别
    race: string;       // 种族
    signature: string;  // 个性签名
    createTime: string; // 创建时间
    updateTime: string; // 更新时间
}

// 用户信息响应接口
export interface UserInfo {
    id: number | null
    username: string | null
    nickname: string | null
    avatar: string | null
    token: string | null
}

// 定义状态接口
export interface UserState {
    token: string | null
    userInfo: UserInfo | null
}
