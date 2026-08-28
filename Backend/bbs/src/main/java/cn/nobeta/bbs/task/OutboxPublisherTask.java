package cn.nobeta.bbs.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.config.RabbitTopologyConfig;
import cn.nobeta.bbs.module.box.entity.OutboxEvent;
import cn.nobeta.bbs.module.box.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisherTask {

    private static final int BTACH_SIZE = 100;
    private static final int MAX_RETRY = 4;
    private static final int WAIT_SECONDS = 30;

    private final OutboxMapper outboxMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 定时扫描待发送 outbox
     */
    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        // 1. 查询待发送 outbox
        // - 一般待发送：status = 0 && next_retry_time = null
        // - 失败重试：status = 0 || next_retry_time <= now
        // - 超时重试：status = 1 && next_retry_time <= now
        List<OutboxEvent> events =
            outboxMapper.selectPendingEvents(BTACH_SIZE);

        // ! 并发窗口: 查询统一批 outbox
        // ! -> 抢占更新 status=0 => 1，失败则不发布

        // 2. 声明发送中状态，更新 outbox
        // status = 1 && next_retry_time = now + WAIT_TIME
        for (OutboxEvent event : events) {
            if (outboxMapper.claimEvent(event.getId(), WAIT_SECONDS) == 0) {
                continue;
            }

            publish(event);
        }
    }

    private void publish(OutboxEvent event) {
        try {
            DomainEvent domainEvent = objectMapper.readValue(
                event.getPayload(), 
                DomainEvent.class
            );

            CorrelationData correlation =
                new CorrelationData(event.getId().toString());

            rabbitTemplate.convertAndSend(
                RabbitTopologyConfig.DOMAIN_EXCHANGE,
                event.getEventType(),
                domainEvent,
                message -> {
                    message.getMessageProperties()
                        .setMessageId(event.getId().toString());
                    message.getMessageProperties()
                        .setDeliveryMode(
                            MessageDeliveryMode.PERSISTENT
                        );
                    return message;
                },
                correlation
            );

            CorrelationData.Confirm confirm =
                correlation.getFuture()
                    .get(5, TimeUnit.SECONDS);

            boolean returned = correlation.getReturned() != null;

            if (confirm.isAck() && !returned) {
                outboxMapper.markPublished(event.getId());
            } else {
                retry(event);
            }
        } catch (Exception e) {
            log.error(
                "Outbox publish failed, eventId={}",
                event.getId(),
                e
            );
            retry(event);
        }
    }

    private void retry(OutboxEvent event) {
        int retryCount = event.getRetryCount() + 1;
        if (retryCount >= MAX_RETRY) {
            outboxMapper.markFailed(event.getId());
            return;
        }

        long delaySeconds = Math.min(300, 1L << event.getRetryCount());
    
        outboxMapper.markRetry(
            event.getId(), 
            LocalDateTime.now().plusSeconds(delaySeconds)
        );
    }
}
