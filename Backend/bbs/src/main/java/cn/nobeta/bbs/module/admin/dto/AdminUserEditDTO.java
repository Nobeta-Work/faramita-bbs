package cn.nobeta.bbs.module.admin.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserEditDTO {

    @NotNull(message = "用户 id 不能为空")
    private Long id;

    /** 状态 0:封禁 1:正常，为空表示不修改 */
    @Min(value = 0, message = "状态只能是 0 或 1")
    @Max(value = 1, message = "状态只能是 0 或 1")
    private Integer status;

    /** 分配的角色 id 列表（整体覆盖），为空表示不修改 */
    private List<Long> roleIds;

}
