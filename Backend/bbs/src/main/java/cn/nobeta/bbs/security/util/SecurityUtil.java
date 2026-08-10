package cn.nobeta.bbs.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import cn.nobeta.bbs.module.auth.dto.UserAuthInfo;

public class SecurityUtil {


    /**
     * 返回当前登陆用户认证信息
     * @return 已登陆:UserAuthInfo, 匿名/无上下文:null
     */
    public static UserAuthInfo getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            // 无上下文 || 匿名访问
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserAuthInfo) {
            return (UserAuthInfo) principal;
        }

        return null;
    }

    /**
     * 返回当前操作用户的 id
     * @return
     */
    public static Long getLoginUserId() {
        UserAuthInfo loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUser().getId();
    }
}
