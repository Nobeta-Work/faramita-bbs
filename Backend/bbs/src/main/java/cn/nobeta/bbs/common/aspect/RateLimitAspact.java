package cn.nobeta.bbs.common.aspect;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.nobeta.bbs.common.annotation.RateLimit;
import cn.nobeta.bbs.common.enums.RedisKeys;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.enums.Scene;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.config.RedisScriptConfig;
import cn.nobeta.bbs.security.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspact {

    private final StringRedisTemplate redisTemplate;
    private final RedisScriptConfig redisScriptConfig;

    @Around("@annotation(rateLimit)")
    public Object around(
        ProceedingJoinPoint joinPoint,
        RateLimit rateLimit
    ) throws Throwable {

        // 1. 用户键/IP
        HttpServletRequest request = ((ServletRequestAttributes) 
            RequestContextHolder.getRequestAttributes()).getRequest();
        Long userId = SecurityUtil.getLoginUserId();
        String identity = userId != null
            ? userId.toString() : request.getRemoteAddr();
        String key = RedisKeys.RATE_LIMIT.getFullKey(identity);

        // 2. 参数解析
        int capacity = rateLimit.capacity();
        int refill = rateLimit.refill();
        if (capacity == -1 || refill == -1) {
            Scene scene = rateLimit.scene();
            switch (scene) {
                case READ:
                    capacity = capacity == -1 ? 200 : capacity;
                    refill = refill == -1 ? 20 : refill;
                    break;
            
                default:
                    capacity = capacity == -1 ? 50 : capacity;
                    refill = refill == -1 ? 5 : refill;
                    break;
            }
        }

        // 3. Lua 脚本
        Long result = redisTemplate.execute(
            redisScriptConfig.rateLimitScript(),
            List.of(key),
            String.valueOf(capacity),
            String.valueOf(refill),
            String.valueOf(System.currentTimeMillis() / 1000),
            String.valueOf(RedisKeys.RATE_LIMIT.getDefaultTtl())
        );

        // 4. result
        if (result == null || result <= 0) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS);
        }

        return joinPoint.proceed();
    }
}
