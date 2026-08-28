package cn.nobeta.bbs.module.box;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.common.event.DomainEventPublisher;
import cn.nobeta.bbs.module.box.entity.OutboxEvent;
import cn.nobeta.bbs.module.box.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxDomainEventPublisher
    implements DomainEventPublisher {

    private final OutboxMapper outboxMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(DomainEvent event) {
        OutboxEvent outbox = OutboxEvent.builder()
                .id(event.getEventId())
                .eventType(event.getEventType())
                .aggregateType(event.getAggregateType())
                .aggregateId(event.getAggregateId())
                .payload(serialize(event))
                .status(0)
                .retryCount(0)
                .build();

        outboxMapper.insertEvent(outbox);
    }

    private String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "领域事件序列化失败",
                e
            );
        }
    }

}
