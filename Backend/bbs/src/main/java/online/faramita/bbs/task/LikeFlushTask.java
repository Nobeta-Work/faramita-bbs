package online.faramita.bbs.task;

import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import online.faramita.bbs.common.enums.RedisKeys;
import online.faramita.bbs.common.enums.ResultCode;
import online.faramita.bbs.common.exception.BusinessException;
import online.faramita.bbs.module.like.entity.LikeBlogChangelog;
import online.faramita.bbs.module.like.service.LikeService;

/**
 * 定时消费 like 任务队列
 */
@Component
@RequiredArgsConstructor
public class LikeFlushTask {

    private static final int BATCH_SIZE = 50;

    private static final String GROUP  = "like-flush-group";
    private static final String CONSUMER = "like-flush-consumer";

    private final LikeService likeService;
    private final StringRedisTemplate stringRedisTemplate;


    @Scheduled(fixedDelay = 60_000)
    public void flushLikeBlogChangelog() {

        // 1. 获取 Redis Stream 的 Key
        String key = RedisKeys.LIKE_CHANGELOG_BLOG.getPrefix();

        // 2. 校验消费组是否存在
        if (!ensureConsumerGroup(key)) {
            return;
        }
        
        // 3.1 第一次读取：优先读取未 ACK 的积压消息
        List<MapRecord<String, Object, Object>> records =
                stringRedisTemplate.opsForStream().read(
                        Consumer.from(GROUP, CONSUMER),
                        StreamReadOptions.empty().count(BATCH_SIZE),
                        StreamOffset.create(key, ReadOffset.from("0"))
                );

        // 3.2 如果第一次读取为空 (pending)，处理正常消息
        if (records == null || records.isEmpty()) {
            records = stringRedisTemplate.opsForStream().read(
                    Consumer.from(GROUP, CONSUMER),
                    StreamReadOptions.empty().count(BATCH_SIZE),
                    StreamOffset.create(key, ReadOffset.lastConsumed())
            );
        }

        if (records == null || records.isEmpty()) {
            return;
        }

        List<LikeBlogChangelog> logs = records.stream()
                .map(r -> {
                    try {
                        Map<Object, Object> value = r.getValue();

                        return LikeBlogChangelog.builder()
                                .blogId(Long.valueOf(value.get("blogId").toString()))
                                .userId(Long.valueOf(value.get("userId").toString()))
                                .isLikeAction(Boolean.parseBoolean(value.get("isLikeAction").toString()))
                                .timestamp(LocalDateTime.parse(value.get("timestamp").toString()))
                                .build();
                    } catch (Exception e) {
                        throw new BusinessException(ResultCode.FAIL);
                    }
                }).filter(Objects::nonNull).toList();

        likeService.flushLikeBlogChangelog(logs);

        RecordId[] ids = records.stream()
                .map(MapRecord::getId)
                .toArray(RecordId[]::new);

        stringRedisTemplate.opsForStream().acknowledge(key, GROUP, ids);
        
    }

    // ? =================== 功能方法 ===================
    // 确保消费组已经创建
    private boolean ensureConsumerGroup(String key) {
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            return false;
        }

        try {
            // 尝试创建消费组 XGROUP CREATE
            stringRedisTemplate.opsForStream()
                    .createGroup(key, ReadOffset.from("0-0"), GROUP);
            // 创建成功
            return true;
        } catch (Exception e) {
            String message = collectExceptionMessages(e);

            // 异常：BUSYGROUP -> 消费组已创建，放行
            if (message.contains("BUSYGROUP")) {
                return true;
            }

            // 异常：no such key -> 整个 Stream 队列还没创建（未生产过消息）
            if (message.contains("no such key") || message.contains("requires the key to exist")) {
                return false;
            }

            throw e;
        }
    }

    private String collectExceptionMessages(Throwable e) {
        StringBuilder message = new StringBuilder();
        Throwable current = e;
        while (current != null) {
            message.append(current.getMessage()).append('\n');
            current = current.getCause();
        }
        return message.toString();
    }
}
