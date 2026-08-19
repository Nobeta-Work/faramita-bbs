package cn.nobeta.bbs.module.admin.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleVO {

    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /** 角色持有的权限列表 */
    private List<PermVO> perms;

}
