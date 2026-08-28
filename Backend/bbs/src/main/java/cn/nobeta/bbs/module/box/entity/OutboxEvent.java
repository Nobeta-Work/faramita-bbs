package cn.nobeta.bbs.module.box.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OutboxEvent {
    private Long id;
    private String eventType;
    private String aggregateType;
    private Long aggregateId;
    private String payload;
    private Integer status;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private LocalDateTime createTime;
    private LocalDateTime publishedTime;
}
