package cn.nobeta.bbs.module.admin.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SysPerm {

    private Long id;            // 权限主键id(雪花)
    private String permCode;    // 权限唯一编码
    private String permName;    // 权限名
    private String description; // 权限描述
    private LocalDateTime createTime; // 创建时间

}
