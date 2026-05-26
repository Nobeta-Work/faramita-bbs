package online.faramita.bbs.module.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.common.result.Result;
import online.faramita.bbs.module.auth.dto.LoginDTO;
import online.faramita.bbs.module.auth.dto.RegisterDTO;
import online.faramita.bbs.module.auth.dto.UserAuthInfo;
import online.faramita.bbs.module.auth.service.AuthService;
import online.faramita.bbs.module.auth.vo.TokenVO;

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
    @Operation(summary = "登录接口", description = "登录接口")
    @PostMapping("/login")
    public Result<TokenVO> login(@RequestBody LoginDTO loginDTO) {

        TokenVO tokenVO = authService.login(loginDTO);

        return Result.success(tokenVO);
    }

    /**
     * 账号注册
     * @param registerDTO
     * @return
     */
    @Operation(summary = "账号注册", description = "账号注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO registerDTO) {
        
        authService.register(registerDTO);

        return Result.success();
    }

    /**
     * 刷新令牌
     * @param refreshToken
     * @return
     */
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
