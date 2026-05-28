package online.faramita.bbs.module.blog.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogEditDTO {

    @Min(value = 0, message = "目录信息异常")
    @NotNull
    private Long folderId;

    @Min(value = 0, message = "发布状态异常")
    @Max(value = 1, message = "发布状态异常")
    private Integer isPublished;

    @Size(min = 1, max = 20, message = "标题长度介于 1-20 字")
    private String title;

    private String content;

    private List<Long> tagIds;

}
