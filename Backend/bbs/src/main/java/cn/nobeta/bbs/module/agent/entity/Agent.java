package cn.nobeta.bbs.module.agent.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Agent {
    private Long token;
    private Long userId;
    private String name;
    private Integer expire;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean deleted;
}
