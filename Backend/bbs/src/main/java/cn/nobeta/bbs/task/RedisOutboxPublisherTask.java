package cn.nobeta.bbs.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.nobeta.bbs.common.enums.RedisKeys;
import cn.nobeta.bbs.common.event.DomainEvent;
import cn.nobeta.bbs.config.RabbitTopologyConfig;
import cn.nobeta.bbs.config.RedisScriptConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisOutboxPublisherTask {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY = 10;
    private static final long LEASE_MILLIS = 30_000;
    private static final long MAX_RETRY_DELAY_MILLIS = 300_000;

    private static final String PENDING_KEY =
        RedisKeys.LIKE_OUTBOX_PENDING.getPrefix();
    private static final String FAILED_KEY =
        RedisKeys.LIKE_OUTBOX_FAILED.getPrefix();

    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RedisScriptConfig redisScriptConfig;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        long now = System.currentTimeMillis();
        // 1. 批量读取到达处理时间的事件
        Set<String> eventIds = stringRedisTemplate.opsForZSet()
            .rangeByScore(PENDING_KEY, 0, now, 0, BATCH_SIZE);

        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }

        for (String eventId : eventIds) {
            // 2. 根据eventId查询事件信息
            String eventKey = RedisKeys.LIKE_OUTBOX_EVENT.getFullKey(eventId);
            long leaseExpireTime = System.currentTimeMillis() + LEASE_MILLIS;
            // 3. 将事件标记为发布中
            Long claimed = stringRedisTemplate.execute(
                redisScriptConfig.redisOutboxClaimScript(),
                List.of(PENDING_KEY, eventKey),
                eventId,
                Long.toString(System.currentTimeMillis()),
                Long.toString(leaseExpireTime)
            );

            if (!Long.valueOf(1).equals(claimed)) {
                continue;
            }

            publish(eventId, eventKey, leaseExpireTime);
        }
    }

    private void publish(
        String eventId,
        String eventKey,
        long leaseExpireTime
    ) {
        try {
            Map<Object, Object> values =
                stringRedisTemplate.opsForHash().entries(eventKey);

            DomainEvent event = DomainEvent.builder()
                .eventId(Long.valueOf(required(values, "eventId")))
                .eventType(required(values, "eventType"))
                .aggregateType(required(values, "aggregateType"))
                .aggregateId(Long.valueOf(required(values, "aggregateId")))
                .createTime(LocalDateTime.parse(required(values, "createTime")))
                .payload(Map.of(
                    "userId", Long.valueOf(required(values, "userId")),
                    "liked", Boolean.valueOf(required(values, "liked"))
                ))
                .build();

            CorrelationData correlation = new CorrelationData(eventId);
            rabbitTemplate.convertAndSend(
                RabbitTopologyConfig.COMMAND_EXCHANGE,
                event.getEventType(),
                event,
                message -> {
                    message.getMessageProperties().setMessageId(eventId);
                    message.getMessageProperties()
                        .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlation
            );

            CorrelationData.Confirm confirm = correlation.getFuture()
                .get(5, TimeUnit.SECONDS);
            boolean returned = correlation.getReturned() != null;

            if (confirm.isAck() && !returned) {
                markPublished(eventId, eventKey, leaseExpireTime);
                return;
            }

            retry(eventId, eventKey, leaseExpireTime);
        } catch (Exception e) {
            log.error("Redis outbox publish failed, eventId={}", eventId, e);
            retry(eventId, eventKey, leaseExpireTime);
        }
    }

    private void markPublished(
        String eventId,
        String eventKey,
        long leaseExpireTime
    ) {
        stringRedisTemplate.execute(
            redisScriptConfig.redisOutboxPublishedScript(),
            List.of(PENDING_KEY, eventKey),
            eventId,
            Long.toString(leaseExpireTime),
            Long.toString(System.currentTimeMillis()),
            RedisKeys.LIKE_OUTBOX_EVENT.getDefaultTtl().toString()
        );
    }

    private void retry(
        String eventId,
        String eventKey,
        long leaseExpireTime
    ) {
        Long retryCount = stringRedisTemplate.execute(
            redisScriptConfig.redisOutboxRetryScript(),
            List.of(PENDING_KEY, eventKey, FAILED_KEY),
            eventId,
            Long.toString(leaseExpireTime),
            Long.toString(System.currentTimeMillis()),
            Integer.toString(MAX_RETRY),
            Long.toString(MAX_RETRY_DELAY_MILLIS)
        );

        if (retryCount != null && retryCount >= MAX_RETRY) {
            log.error(
                "Redis outbox event marked failed, eventId={}, retryCount={}",
                eventId,
                retryCount
            );
        }
    }

    private String required(Map<Object, Object> values, String field) {
        Object value = values.get(field);
        if (value == null) {
            throw new IllegalStateException(
                "Redis outbox field is missing: " + field
            );
        }
        return value.toString();
    }

}
