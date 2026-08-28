package cn.nobeta.bbs.common.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DomainEvent {
    private Long eventId;
    private String eventType;
    private String aggregateType;
    private Long aggregateId;
    private LocalDateTime createTime;
    private Object payload;
}
