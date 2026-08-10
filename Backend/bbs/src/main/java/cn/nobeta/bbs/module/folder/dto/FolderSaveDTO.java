package cn.nobeta.bbs.module.folder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FolderSaveDTO {

    @Min(value = 0, message = "目录信息异常")
    @NotNull
    private Long parentId;

    @Size(min = 1, max = 20, message = "目录名长度限制 1-20 字符")
    @NotBlank
    private String name;
}
