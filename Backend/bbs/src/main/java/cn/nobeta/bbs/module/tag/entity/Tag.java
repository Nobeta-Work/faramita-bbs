package cn.nobeta.bbs.module.tag.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tag {

    private Long id;            // 主键id (雪花)
    private String name;        // 标签名
    private String description; // 描述
    private LocalDateTime createTime; // 创建时间
    
}
