package cn.nobeta.bbs.module.agent.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.nobeta.bbs.common.constant.NameConstant;
import cn.nobeta.bbs.common.enums.RedisKeys;
import cn.nobeta.bbs.common.enums.ResultCode;
import cn.nobeta.bbs.common.exception.BusinessException;
import cn.nobeta.bbs.common.result.PageResult;
import cn.nobeta.bbs.common.util.SnowflakeUtil;
import cn.nobeta.bbs.module.agent.dto.AgentTokenPageQuery;
import cn.nobeta.bbs.module.agent.dto.AgentTokenSaveDTO;
import cn.nobeta.bbs.module.agent.entity.Agent;
import cn.nobeta.bbs.module.agent.mapper.AgentMapper;
import cn.nobeta.bbs.module.agent.vo.AgentTokenVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentMapper agentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public PageResult<AgentTokenVO> queryAgentTokenPageByUserId(AgentTokenPageQuery query) {

        Long userId = query.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.ILLEGAL_ARGUMENT);
        }

        // 1. 提取分页参数
        Integer pageNum = query.getPageNum();
        Integer pageSize = query.getPageSize();

        // 2.开启PageHelper分页查询
        PageHelper.startPage(pageNum, pageSize);

        Page<AgentTokenVO> page = agentMapper.selectAgentTokenPage(query);
        if (page.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 3. 组装 records
        List<AgentTokenVO> records = (List<AgentTokenVO>) page;

        // 4. 消除 token

        records.forEach(record -> {
            record.setToken(NameConstant.AGENT_TOKEN_PRFIX);
        });

        return PageResult.<AgentTokenVO>builder()
                .total(page.getTotal())
                .pageNum(page.getPageNum())
                .pageSize(page.getPageSize())
                .pages(page.getPages())
                .records(records)
                .build();
    }

    public String addAgentTokenByUserId(Long userId, AgentTokenSaveDTO dto) {
        // 1. 生成 Agent Token
        Long rawToken = SnowflakeUtil.nextId();
        String token = NameConstant.AGENT_TOKEN_PRFIX + rawToken;

        // 2. 封装 Agent
        Agent agent = Agent.builder()
                .token(rawToken)
                .userId(userId)
                .name(dto.getName())
                .expire(dto.getExpire())
                .build();
        
        // 3. 插入数据库
        try {
            agentMapper.insertAgent(agent);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.AGENT_NAME_DUPLICATE);
        }

        return token;
    }

    public void deleteAgentTokenByUserIdAndName(Long userId, String name) {
        if (userId == null) {
            throw new BusinessException(ResultCode.ILLEGAL_ARGUMENT);
        }

        String rawToken = agentMapper.selectTokenByUserIdAndName(userId, name);
        if (rawToken == null) return;

        String token = NameConstant.AGENT_TOKEN_PRFIX + rawToken;

        agentMapper.deleteAgentTokenByToken(rawToken);
        redisTemplate.delete(
            RedisKeys.AGENT_TOKEN.getFullKey(token)
        );
    }

}
