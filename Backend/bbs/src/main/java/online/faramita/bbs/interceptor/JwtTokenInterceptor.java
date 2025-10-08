package online.faramita.bbs.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.config.JwtConfig;
import online.faramita.bbs.constant.JwtClaimsConstant;
import online.faramita.bbs.context.BaseContext;
import online.faramita.bbs.util.JwtUtil;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 校验jwt
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.判断拦截的方式是否为controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        // 2. 尝试从请求头解析Token
        String authHeader = request.getHeader(jwtConfig.getHeader());
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = JwtUtil.parseJWT(jwtConfig.getSecret(), token);
                Long uid = Long.valueOf(claims.get(JwtClaimsConstant.UID).toString());
                BaseContext.setCurrentId(uid);
                log.info(">请求访问用户id->{}<", uid);
                // 解析成功，无论什么方法都放行
                return true;
            } catch (Exception ex) {
                log.error(">JWT解析异常<", ex);
                // 解析失败，token无效
            }
        }

        // 3. 到这里说明：要么没有token，要么token无效
        String method = request.getMethod();
        if ("GET".equals(method)) {
            // 对于GET请求，允许匿名访问，但不设置用户ID
            // BaseContext.currentId()将保持为null
            log.info(">GET请求，匿名访问<");
            return true;
        } else {
            // 对于非GET请求（POST, PUT, DELETE等），要求必须登录
            log.warn(">非GET请求，未提供有效Token，拒绝访问<");
            response.setStatus(401); // Unauthorized
            return false;
        }
    }

    // 清理内存
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal，防止内存泄漏
        BaseContext.removeCurrentId();
    }
}
