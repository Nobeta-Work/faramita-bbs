package cn.nobeta.bbs.module.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 用户-角色关系简要信息（用于批量组装用户角色列表）
 */
@Data
@Builder
public class UserRoleBrief {

    private Long userId;
    private String roleCode;

}
