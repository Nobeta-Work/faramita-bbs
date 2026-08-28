package cn.nobeta.bbs.module.box.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InboxEvent {
    private String consumerGroup;
    private Long id;
    private LocalDateTime consumedTime;
}
