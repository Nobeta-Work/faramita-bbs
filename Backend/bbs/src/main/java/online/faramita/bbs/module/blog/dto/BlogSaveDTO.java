package online.faramita.bbs.module.blog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogSaveDTO {

    @Size(min = 1, max = 50, message = "标题字数限制 1-50 字")
    @NotBlank
    private String title;   // 标题

    @Min(value = 0, message = "目录异常")
    private Long folderId = 0L;  // 目录 ID，默认指向根目录

}
