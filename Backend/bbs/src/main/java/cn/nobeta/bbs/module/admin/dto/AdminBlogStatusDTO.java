package cn.nobeta.bbs.module.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminBlogStatusDTO {

    @NotNull(message = "博客 id 不能为空")
    private Long id;

    @NotNull(message = "公开状态不能为空")
    private Integer isPublished;

}
