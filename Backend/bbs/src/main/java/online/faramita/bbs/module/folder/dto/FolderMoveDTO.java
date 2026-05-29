package online.faramita.bbs.module.folder.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FolderMoveDTO {

    @Min(value = 0, message = "目录信息异常")
    private Long targetParentId;

}
