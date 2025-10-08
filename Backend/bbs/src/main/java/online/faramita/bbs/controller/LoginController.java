package online.faramita.bbs.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.config.JwtConfig;
import online.faramita.bbs.constant.JwtClaimsConstant;
import online.faramita.bbs.dto.LoginDTO;
import online.faramita.bbs.dto.RegisterDTO;
import online.faramita.bbs.entity.User;
import online.faramita.bbs.result.Result;
import online.faramita.bbs.service.UserService;
import online.faramita.bbs.util.JwtUtil;
import online.faramita.bbs.vo.LoginVO;

/**
 * 登录注册相关接口
 */
@RequestMapping("api/")
@RestController
@Validated  // 开启参数校验
@Slf4j
@Tag(name = "登录注册相关接口", description = "登录注册相关接口")
public class LoginController {

    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    private UserService userService;

    /**
     * 登录接口
     * @param loginDTO
     * @return
     */
    @Operation(summary = "登录接口", description = "登录接口")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info(">账号登录:{}<", loginDTO.getUsername());
        User user = userService.login(loginDTO);

        // 登录成功，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.UID, user.getId());
        String token = JwtUtil.createJwt(
            jwtConfig.getSecret(),
            jwtConfig.getExpire(),
            claims);
            
        // 封装返回结果
        LoginVO loginVO = LoginVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .token(token)
                        .build();
        
        log.info(">登录成功 uid:{}<", user.getId());
        return Result.success(loginVO);
    }

    /**
     * 账号注册
     * @param registerDTO
     * @return
     */
    @Operation(summary = "账号注册", description = "账号注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("账号注册:{}", registerDTO.getUsername());
        userService.register(registerDTO);
        return Result.success();
    } 
}
