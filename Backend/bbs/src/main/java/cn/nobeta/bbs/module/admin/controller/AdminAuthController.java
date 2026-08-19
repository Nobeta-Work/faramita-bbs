package cn.nobeta.bbs.module.admin.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.result.Result;
import cn.nobeta.bbs.module.admin.dto.AdminLoginDTO;
import cn.nobeta.bbs.module.admin.service.AdminAuthService;
import cn.nobeta.bbs.module.auth.vo.TokenVO;

/**
 * 后台登录接口
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 后台登录
     * @param dto
     * @return
     */
    @AuditLog(message = "后台登录", data = "{'username': #p0.username}")
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody AdminLoginDTO dto) {

        TokenVO tokenVO = adminAuthService.login(dto);

        return Result.success(tokenVO);
    }

}
