package online.faramita.bbs.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlogDTO {
    private String title;
    private String content;
    private String summary;
    private Long authorId;
    private String authorName;
    private Long bigCategoryId;
    private String littleCategoryName;
    private Long categoryId;
}
