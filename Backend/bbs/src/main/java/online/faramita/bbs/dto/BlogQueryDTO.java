package online.faramita.bbs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlogQueryDTO {
    private String bloguid;
    private String title;
    @Default
    private boolean needContent = true;    // 是否需要详情
    private Long authorId;
    private String authorName;
    private Integer categoryId;
    private Integer bigCategoryId;
    private String littleCategoryName;
    private String keyword;
    private String orderBy;
    private String sortOrder;
    private Long queryUid;
}
