package online.faramita.bbs.task;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import lombok.RequiredArgsConstructor;
import online.faramita.bbs.common.enums.RedisKeys;
import online.faramita.bbs.module.like.entity.LikeBlogChangelog;
import online.faramita.bbs.module.like.service.LikeService;

/**
 * 定时消费 like 任务队列
 */
@Component
@RequiredArgsConstructor
public class LikeFlushTask {

    private static final int BATCH_SIZE = 50;

    private final LikeService likeService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedDelay = 60_000)
    public void flushLikeBlogChangelog() {

        String key = RedisKeys.LIKE_CHANGELOG_BLOG.getPrefix();
        
        List<Object> rowLogs = redisTemplate.opsForList()
                .leftPop(key, BATCH_SIZE);
        if (rowLogs == null || rowLogs.isEmpty()) {
            return;
        }

        List<LikeBlogChangelog> logs = rowLogs.stream()
                .map(LikeBlogChangelog.class::cast).toList();

        if (logs.isEmpty()) { 
            return; 
        }

        try {
            likeService.flushLikeBlogChangelog(logs);
        } catch (Exception e) {
            redisTemplate.opsForList().leftPushAll(
                key, rowLogs
            );
            throw e;
        }
        

    }
}
