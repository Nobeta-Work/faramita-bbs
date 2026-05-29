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
    ),
    
    /** =========== 点赞模块 =========== */
    LIKE_BLOG(
        "like:blog:",
        86400L,
        "博客的点赞状态Set集合 like:blog:{blogId} : userId"
    ),
    LIKE_CHANGELOG_BLOG(
        "like:changelog:blog",
        0L,
        "点赞变更消息List队列 like:change:blog : likeBlogChangelog"
    );



    private final String prefix;
    private final Long defaultTtl;
    private final String description;

    public String getFullKey(Object suffix) {
        return prefix + suffix;
    }

}
