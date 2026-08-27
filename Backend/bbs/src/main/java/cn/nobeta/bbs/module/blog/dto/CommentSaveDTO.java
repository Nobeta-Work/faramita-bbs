package cn.nobeta.bbs.module.blog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentSaveDTO {

    @NotNull(message = "博客信息异常")
    @Min(value = 1, message = "博客信息异常")
    private Long blogId;

    @Min(value = 0, message = "父评论信息异常")
    private Long parentId = 0L;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容不能超过 2000 字")
    private String content;
}
