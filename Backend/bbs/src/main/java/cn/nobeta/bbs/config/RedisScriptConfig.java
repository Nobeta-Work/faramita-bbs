package cn.nobeta.bbs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisScriptConfig {

    

    /**
     * 博客点赞 lua 脚本
     * toggle + 计入变更任务队列
     * @return
     */
    public static DefaultRedisScript<Long> likeToggleScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/like_toggle.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
