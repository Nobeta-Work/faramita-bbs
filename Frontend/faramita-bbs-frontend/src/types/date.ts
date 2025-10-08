// # 日期相关类型和工具函数

// 标准时间格式 yyyy-MM-dd HH:mm:ss
export type DateTime = string

// 日期格式化
export interface DateFormatOptions {
    format?: 'short' | 'long' | 'time' | 'full' | 'friendly' | 'relative'
    locale?: string
}

// 日期工具函数
export class DateUtils {
    // 将Date对象转化为标准时间格式字符串
    static formatToDateTime(date: Date): DateTime {
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hour = String(date.getHours()).padStart(2, '0')
        const minute = String(date.getMinutes()).padStart(2, '0');
        const second = String(date.getSeconds()).padStart(2, '0');

        return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    }
    // 将Date对象转化为仅日期格式字符串
    static formatToDateOnly(date: Date) {
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')

        return `${year}-${month}-${day}`
    }
    // 获取完整的日期时间显示
    static getFullDateTime(date: Date): string {
        return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}时${date.getMinutes()}分`
    }
    // 将string类型的Date(2025-10-05T01:11:53)格式转化为标准时间格式
    static isoToDateTime(isoString: string): DateTime {
        try {
            // 解析ISO 8601格式的字符串
            const date = new Date(isoString);
            
            // 验证日期是否有效
            if (isNaN(date.getTime())) {
                throw new Error('Invalid date format');
            }
            
            // 使用现有的formatToDateTime方法转换为标准格式
            return this.formatToDateTime(date);
        } catch (error) {
            console.error('日期转换失败:', error);
            return '无效日期';
        }
    }
    // 将string类型的Date转换为标准格式时间(且仅保留date)
    static isoToDateOnly(isoString: string) {
        try {
            // 解析ISO 8601格式的字符串
            const date = new Date(isoString);
            
            // 验证日期是否有效
            if (isNaN(date.getTime())) {
                throw new Error('Invalid date format');
            }
            
            // 使用formatToDateOnly方法转换为仅日期格式
            return this.formatToDateOnly(date);
        } catch (error) {
            console.error('日期转换失败:', error);
            return '无效日期';
        }
    }
    
}