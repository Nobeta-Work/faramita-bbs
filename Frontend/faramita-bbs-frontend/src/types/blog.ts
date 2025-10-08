// # 博文相关类型定义

// 博文接口
export interface Blog{
    bloguid: string;
    title: string;
    content: string;
    summary: string;
    authorId: number;
    authorName: string;
    categoryId: number;
    bigCategoryId: number;
    littleCategoryName: string;
    isPublished: number;
    createTime: string;
    updateTime: string;
}

// 博文类

export class BlogUtils {
    static bigIdToString(bigId: string | number) {
        if (bigId == '1') {
            return '项目'
        } else if (bigId == '2') {
            return '技术栈'
        } else if (bigId == '3') {
            return '算法'
        } else if (bigId == '4') {
            return '游戏'
        } else if (bigId == '5') {
            return '余文'
        }
    }
    static getCategoryClass(bigCategoryId: number): string {
        const categoryMap: Record<number, string> = {
            1: 'category-project',
            2: 'category-tech', 
            3: 'category-algo',
            4: 'category-game',
            5: 'category-other'
        };
        return categoryMap[bigCategoryId] || '';
    }
}