package cn.nobeta.bbs.module.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 角色-权限关系简要信息（用于批量组装角色权限列表）
 */
@Data
@Builder
public class RolePermBrief {

    private Long roleId;
    private Long permId;
    private String permCode;
    private String permName;

}
