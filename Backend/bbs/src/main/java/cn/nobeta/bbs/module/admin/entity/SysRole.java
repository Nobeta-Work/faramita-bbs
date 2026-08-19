package cn.nobeta.bbs.module.admin.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SysRole {

    private Long id;            // 角色主键id(雪花)
    private String roleCode;    // 角色唯一编码
    private String roleName;    // 角色名
    private String description; // 角色描述
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

}
