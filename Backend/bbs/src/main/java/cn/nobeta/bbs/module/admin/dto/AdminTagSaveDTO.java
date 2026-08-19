package cn.nobeta.bbs.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminTagSaveDTO {

    @NotBlank(message = "标签名不能为空")
    @Size(max = 20, message = "标签名不能超过20字符")
    private String name;

    @Size(max = 200, message = "标签描述不能超过200字符")
    private String description;

}
