package cn.nobeta.bbs.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermEditDTO {

    @NotNull(message = "权限 id 不能为空")
    private Long id;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 20, message = "权限编码不能超过20字符")
    private String permCode;

    @NotBlank(message = "权限名不能为空")
    @Size(max = 20, message = "权限名不能超过20字符")
    private String permName;

    @Size(max = 200, message = "权限描述不能超过200字符")
    private String description;

}
