package cn.nobeta.bbs.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisKeys {

    /** =========== 登陆认证模块 =========== */

    TOKEN_BLACK(
        "token:blacklisted:",
        1800L,
        "JWT AccessToken 黑名单 token:blacklisted:{jti} 1"
    ),
    LOGIN_USER(
        "login_user:",
        1800L,
        "记录活跃登陆的用户信息 login_user:{userId} loginUser"
    ), 
    REFRESH_TOKEN(
        "refresh_token:",
        604800L,
        "RefreshToken 缓存 refresh_token:{userId} {jti}:refreshToken"
    ),
    AGENT_TOKEN(
        "agent_token:",
        604800L,
        "AgentToken 缓存 agent_token:{agent_token} userAuthInfo"
    ),
    RATE_LIMIT(
        "rate_limit:",
        3600L,
        "限流桶Hash表 rate_limit:{userId/IP} {tokens:tokens} {last_time:last_time}"
    ),
    /** =========== 点赞模块 =========== */
    LIKE_BLOG(
        "like:blog:",
        86400L,
        "博客的点赞状态Set集合 like:blog:{blogId} : userId"
    ),
    LIKE_COMMENT(
        "like:comment:",
        86400L,
        "评论的点赞状态 Set 集合 like:comment:{commentId} : userId"
    ),
    LIKE_OUTBOX_EVENT(
        "like:outbox:event:",
        86400L,
        "点赞事件内容和状态 Hash 表 like:outbox:event:{eventId}"
    ),
    LIKE_OUTBOX_PENDING(
        "like:outbox:pending",
        -1L,
        "点赞待发布事件 ZSet，member: eventId，score: 下次发布时间"
    ),
    LIKE_OUTBOX_FAILED(
        "like:outbox:failed",
        -1L,
        "点赞发布失败事件 ZSet，member: eventId，score: 失败时间"
    )
    
    
    
    ;



    private final String prefix;
    private final Long defaultTtl;
    private final String description;

    public String getFullKey(Object suffix) {
        return prefix + suffix;
    }

}
