package cn.nobeta.bbs.module.admin.service;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.constant.NameConstant;
import cn.nobeta.bbs.common.enums.RedisKeys;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.module.admin.dto.AdminLoginDTO;
import cn.nobeta.bbs.module.auth.dto.UserAuthInfo;
import cn.nobeta.bbs.module.auth.vo.TokenVO;
import cn.nobeta.bbs.security.util.TokenProvider;

/**
 * 后台登录服务
 */
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 后台登录：校验账号密码后，额外校验 ADMIN 角色
     * @param dto
     * @return
     */
    public TokenVO login(AdminLoginDTO dto) {

        // 1. 委托 SpringSecurity 认证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        // 2. 提取 loginUser
        UserAuthInfo loginUser = (UserAuthInfo) authentication.getPrincipal();

        // 3. 校验管理员角色
        List<String> roles = loginUser.getRoles();
        if (roles == null || !roles.contains(NameConstant.ADMIN_ROLE)) {
            throw new BusinessException(ResultCode.ADMIN_FORBIDDEN);
        }

        // 4. 生成双 Token
        Long userId = loginUser.getUser().getId();
        String accessToken = tokenProvider.generateAccessToken(loginUser);
        String refreshToken = tokenProvider.generateRefreshToken(loginUser);
        Date expireIn = tokenProvider.getExpiration(accessToken);

        // 5. 缓存用户信息 + RefreshToken
        Duration ttl = Duration.ofSeconds(RedisKeys.REFRESH_TOKEN.getDefaultTtl());
        redisTemplate.opsForValue().set(
            RedisKeys.LOGIN_USER.getFullKey(userId),
            loginUser, ttl
        );
        redisTemplate.opsForValue().set(
            RedisKeys.REFRESH_TOKEN.getFullKey(userId),
            refreshToken, ttl
        );

        // 6. 返回结果
        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expireIn(expireIn)
                .build();
    }

}
