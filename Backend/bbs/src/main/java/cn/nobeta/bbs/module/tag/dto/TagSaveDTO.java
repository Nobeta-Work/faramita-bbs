package cn.nobeta.bbs.module.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagSaveDTO {

    @NotBlank
    @Size(min = 1, max = 20, message = "标签名限制在 1-20 字符")
    private String name;

    @Size(min = 1, max = 200, message = "标签描述限制在 1-200 字符")
    private String description;
}
