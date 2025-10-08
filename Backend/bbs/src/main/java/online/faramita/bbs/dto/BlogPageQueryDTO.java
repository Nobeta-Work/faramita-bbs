package online.faramita.bbs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlogPageQueryDTO {
    @Schema(title = "页码", description = "页码")
    private Integer page = 1;       // 页码
    @Schema(title = "每页大小", description = "单页词条数量")
    private Integer pageSize = 10;  // 每页大小
    @Schema(title = "作者ID", description = "作者ID")
    private Long authorId;
    @Schema(title = "作者名称", description = "作者名称")
    private String authorName;
    @Schema(title = "关键字", description = "搜索关键字")
    private String keyword;
    @Schema(title = "排序字段", description = "排序字段")
    private String orderBy;
    @Schema(title = "排序方向", description = "排序方向")
    private String sortOrder;
    @Schema(title = "大分类ID", description = "大分类ID")
    private Integer bigCategoryId;
    @Schema(title = "小分类ID", description = "小分类ID")
    private Integer categoryId;
    @Schema(title = "小分类名称", description = "小分类名称")
    private String littleCategoryName;
}