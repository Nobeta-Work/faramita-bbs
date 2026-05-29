package online.faramita.bbs.module.folder.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FolderBlogsMoveDTO {

    @NotEmpty(message = "博客列表不能为空")
    private List<Long> blogIds;

    @NotNull(message = "目标目录不能为空")
    @Min(value = 0, message = "目标目录异常")
    private Long targetId;

}
