package cn.nobeta.bbs.module.agent.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.nobeta.bbs.common.annotation.RateLimit;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.enums.Scene;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.result.Result;
import cn.nobeta.bbs.module.agent.dto.AgentTokenPageQuery;
import cn.nobeta.bbs.module.agent.dto.AgentTokenSaveDTO;
import cn.nobeta.bbs.module.agent.service.AgentService;
import cn.nobeta.bbs.module.agent.vo.AgentTokenVO;
import cn.nobeta.bbs.module.auth.dto.UserAuthInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    /**
     * 获取 Agent Token 列表
     * @param loginUser
     * @param query
     * @return
     */
    @RateLimit(scene = Scene.READ)
    @GetMapping("/page")
    public Result<PageResult<AgentTokenVO>> getAgentTokenPage(
        @AuthenticationPrincipal UserAuthInfo loginUser,
        AgentTokenPageQuery query
    ) {
        Long userId = loginUser.getUser().getId();
        query.setUserId(userId);
        PageResult<AgentTokenVO> vo = agentService.queryAgentTokenPageByUserId(query);
        return Result.success(vo);
    }

    /**
     * 创建 Agent Token
     * @param loginUser
     * @param dto
     * @return
     */
    @RateLimit(scene = Scene.WRITE)
    @PostMapping
    public Result<String> saveAgentToken(
        @AuthenticationPrincipal UserAuthInfo loginUser,
        @Valid @RequestBody AgentTokenSaveDTO dto
    ) {
        Long userId = loginUser.getUser().getId();
        if (userId == null) return Result.fail(ResultCode.UNAUTHORIZED);
        
        String token = agentService.addAgentTokenByUserId(userId, dto);

        return Result.success(token);
    } 

    /**
     * 删除 Agent Token
     * @param loginUser
     * @param name
     * @return
     */
    @RateLimit(scene = Scene.WRITE)
    @DeleteMapping
    public Result<?> deleteAgentToken(
        @AuthenticationPrincipal UserAuthInfo loginUser,
        @RequestBody String name
    ) {
        Long userId = loginUser.getUser().getId();

        agentService.deleteAgentTokenByUserIdAndName(userId, name);

        return Result.success();
    }
}
