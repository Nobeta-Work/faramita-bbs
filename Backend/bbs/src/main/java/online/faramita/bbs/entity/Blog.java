package online.faramita.bbs.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Blog")
public class Blog {
    private String bloguid;
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private Long bigCategoryId;
    private String littleCategoryName;
    private Integer isPublished;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
