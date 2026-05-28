package online.faramita.bbs.common.enums;

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
        "RefreshToken 缓存 refresh_token:{userId} refreshToken"
    ), LIKE_BLOG(
        "like:blog:",
        86400L,
        "博客的点赞状态集合 like:blog:{blogId} userId"
    );


    private final String prefix;
    private final Long defaultTtl;
    private final String description;

    public String getFullKey(Object suffix) {
        return prefix + suffix;
    }

}
