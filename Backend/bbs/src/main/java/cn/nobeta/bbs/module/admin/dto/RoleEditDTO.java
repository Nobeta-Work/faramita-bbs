package cn.nobeta.bbs.module.admin.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleEditDTO {

    @NotNull(message = "角色 id 不能为空")
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 20, message = "角色编码不能超过20字符")
    private String roleCode;

    @NotBlank(message = "角色名不能为空")
    @Size(max = 20, message = "角色名不能超过20字符")
    private String roleName;

    @Size(max = 255, message = "角色描述不能超过255字符")
    private String description;

    /** 修改后的权限 id 列表（整体覆盖），可为空 */
    private List<Long> permIds;

}
