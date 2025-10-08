/**
 * 分类类
 */
public class BlogCategory {
    /**
     * 0 => 项目
     * 1 => 技术栈
     * 2 => 算法
     * 3 => 余文
     */
    private int bigCategoryId; // 大类逻辑外键
    private String bigCategoryName; // 大类名
    private int littleCategoryId;   // 小类逻辑外键
    private int littleCategoryName; // 小键名
}
