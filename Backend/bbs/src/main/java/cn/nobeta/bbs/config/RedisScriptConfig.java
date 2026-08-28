package cn.nobeta.bbs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisScriptConfig {

    private final DefaultRedisScript<Long> likeToggleScript =
        load("redis/like_toggle.lua");

    private final DefaultRedisScript<Long> rateLimitScript =
        load("redis/rate_limit.lua");

    private final DefaultRedisScript<Long> redisOutboxClaimScript =
        load("redis/redis_outbox_claim.lua");

    private final DefaultRedisScript<Long> redisOutboxPublishedScript =
        load("redis/redis_outbox_published.lua");

    private final DefaultRedisScript<Long> redisOutboxRetryScript =
        load("redis/redis_outbox_retry.lua");

    /**
     * 通用点赞 Lua 脚本
     * toggle + 写入 Redis Outbox
     * @return
     */
    public DefaultRedisScript<Long> likeToggleScript() {
        return likeToggleScript;
    }

    /**
     * 限流 Lua 脚本
     * @return
     */
    public DefaultRedisScript<Long> rateLimitScript() {
        return rateLimitScript;
    }

    public DefaultRedisScript<Long> redisOutboxClaimScript() {
        return redisOutboxClaimScript;
    }

    public DefaultRedisScript<Long> redisOutboxPublishedScript() {
        return redisOutboxPublishedScript;
    }

    public DefaultRedisScript<Long> redisOutboxRetryScript() {
        return redisOutboxRetryScript;
    }

    private static DefaultRedisScript<Long> load(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(Long.class);
        return script;
    }
}
