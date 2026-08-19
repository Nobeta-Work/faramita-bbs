package cn.nobeta.bbs.security.filter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import cn.nobeta.bbs.common.constant.NameConstant;
import cn.nobeta.bbs.common.enums.RedisKeys;
import cn.nobeta.bbs.module.agent.entity.Agent;
import cn.nobeta.bbs.module.agent.mapper.AgentMapper;
import cn.nobeta.bbs.module.auth.dto.UserAuthInfo;
import cn.nobeta.bbs.module.auth.mapper.AuthMapper;
import cn.nobeta.bbs.module.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AgentAuthenticationFilter extends OncePerRequestFilter {

    private final AgentMapper agentMapper;
    private final AuthMapper authMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        // 1. 提取 token
        String token = resolveToken(request);
        if (token == null || isBlacklisted(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Redis 缓存
        UserAuthInfo userAuthInfo = (UserAuthInfo) redisTemplate.opsForValue().get(
            RedisKeys.AGENT_TOKEN.getFullKey(token)
        );
        if (userAuthInfo == null) {
            // 3. 查数据库
            userAuthInfo = mockLoginUser(token);
            if (userAuthInfo == null) {
                chain.doFilter(request, response);
                return;
            }

            // 4. 缓存 => mockLoginUser
        }

        
        // 5. 模拟用户上下文
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                userAuthInfo, null, userAuthInfo.getAuthorities()
            );
        authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        chain.doFilter(request, response);
    }

    /** ====== 一些功能方法 ====== **/
	/** 从 Authorization Header 提取 Token **/
	private String resolveToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(header) && header.startsWith("Bearer pa_")) {
			return header.substring(7);
		}
		return null;
	}
    private UserAuthInfo mockLoginUser(String token) {
        Long rawToken;
        try {
            rawToken = Long.parseLong(token.substring(NameConstant.AGENT_TOKEN_PRFIX.length()));
        } catch (NumberFormatException ex) {
            return null;
        }
        Agent agent = agentMapper.selectByToken(rawToken);
        if (agent == null) { return null; }
        // 过期判断
        int expire = agent.getExpire();
        LocalDateTime now = LocalDateTime.now();
        long ttl = Duration.between(now, agent.getCreateTime().plusDays(expire)).getSeconds();
        if (expire >= 0 && ttl <= 0) {
            return null;
        }
        Long userId = agent.getUserId();
        if (userId == null) return null;
        User user = User.builder().id(userId).build();
        List<String> roles = List.of(NameConstant.AGENT_ROLE);
        List<String> permissions = authMapper.selectPermCodesByRoleCode(NameConstant.AGENT_ROLE);
        UserAuthInfo userAuthInfo = new UserAuthInfo(user, permissions, roles);
        // 缓存
        ttl = Math.min(RedisKeys.AGENT_TOKEN.getDefaultTtl(), expire < 0 ? Long.MAX_VALUE : ttl);
        redisTemplate.opsForValue().set(
            RedisKeys.AGENT_TOKEN.getFullKey(token),
            userAuthInfo,
            Duration.ofSeconds(ttl)
        );
        return userAuthInfo;
    }
    private boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(
            RedisKeys.TOKEN_BLACK.getFullKey(token)
        ));
    }

}
