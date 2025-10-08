
import java.time.LocalDateTime;

/**
 * 博文类
 */
public class Blog {
    private String bloguid;     // 博文uid -> 索引
    private String title;       // 博文标题
    private String content;     // 博文内容,存储Markdown源码
    private String summary;     // 摘要
    private Long authorId;      // 作者ID
    private BlogCategory blogCategory;  // 博文分类
    private LocalDateTime createTime;   // 创建日期
    private LocalDateTime updateTime;   // 修改日期
}
