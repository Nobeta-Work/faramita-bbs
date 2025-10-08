package online.faramita.bbs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogUpdateDTO {
    
    // bloguid 从路径获取，不在此DTO中
    
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String summary;

    @NotNull(message = "大类ID不能为空")
    private Long bigCategoryId;

    @NotBlank(message = "小类名称不能为空")
    private String littleCategoryName;

    @NotNull(message = "发布状态不能为空")
    private Integer isPublished;
}
