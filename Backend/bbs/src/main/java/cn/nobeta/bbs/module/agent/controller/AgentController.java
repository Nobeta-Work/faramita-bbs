package cn.nobeta.bbs.module.agent.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.nobeta.bbs.common.enums.ResultCode;
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
