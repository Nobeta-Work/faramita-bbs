package cn.nobeta.bbs.module.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.annotation.RateLimit;
import cn.nobeta.bbs.common.result.Result;
import cn.nobeta.bbs.module.auth.dto.LoginDTO;
import cn.nobeta.bbs.module.auth.dto.RegisterDTO;
import cn.nobeta.bbs.module.auth.dto.UserAuthInfo;
import cn.nobeta.bbs.module.auth.service.AuthService;
import cn.nobeta.bbs.module.auth.vo.TokenVO;

/**
 * 登录注册相关接口
 */
@RequestMapping("/api/auth")
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     * @param loginDTO
     * @return
     */
    // {"username": "admin", "password": "password"}
    @RateLimit(capacity = 5, refill = 1)
    @AuditLog(message = "用户登陆", data = "{'username': #p0.username}")
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginDTO loginDTO) {

        TokenVO tokenVO = authService.login(loginDTO);

        return Result.success(tokenVO);
    }

    /**
     * 账号注册
     * @param registerDTO
     * @return
     */
    @RateLimit(capacity = 5, refill = 1)
    @AuditLog(message = "用户注册",
        data = "{'username': #p0.username, 'nickname': #p0.nickname, 'sex': #p0.sex, 'race': #p0.race}"
    )
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        
        authService.register(registerDTO);

        return Result.success();
    }

    /**
     * 刷新令牌
     * @param refreshToken
     * @return
     */
    @RateLimit(capacity = 5, refill = 1)
    @AuditLog(message = "用户刷新令牌")
    @PostMapping("/refresh")
    public Result<TokenVO> refresh(@NotBlank @RequestParam String refreshToken) {

        TokenVO tokenVO = authService.refresh(refreshToken);

        return Result.success(tokenVO);
    }

    /**
     * 登出接口
     * @param loginUser
     * @param refreshToken
     * @return
     */
    @AuditLog(message = "用户登出", data = "{'username': #p0.getUsername()}")
    @PostMapping("/logout")
    public Result<Void> logout(
        @AuthenticationPrincipal UserAuthInfo loginUser,
        @RequestHeader("Authorization") String authHeader
    ) {
        String accessToken = authHeader.substring(7);
        Long userId = loginUser.getUser().getId();

        authService.logout(userId, accessToken);

        return Result.success();
    }
}
