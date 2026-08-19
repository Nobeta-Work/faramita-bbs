package cn.nobeta.bbs.module.agent.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.github.pagehelper.Page;

import cn.nobeta.bbs.module.agent.dto.AgentTokenPageQuery;
import cn.nobeta.bbs.module.agent.entity.Agent;
import cn.nobeta.bbs.module.agent.vo.AgentTokenVO;

@Mapper
public interface AgentMapper {

    /**
     * 根据 token 查询所属用户
     * @param token
     * @return
     */
    @Select("SELECT user_id FROM agent_token WHERE token = #{token}")
    Long selectUserIdByAgentToken(@Param("token") Long token);

    /**
     * 分页查询 Agent Token
     * @param query
     * @return
     */
    Page<AgentTokenVO> selectAgentTokenPage(AgentTokenPageQuery query);

    void insertAgent(Agent agent);

    @Select("SELECT token FROM agent_token WHERE user_id = #{userId} AND name = #{name}")
    String selectTokenByUserIdAndName(@Param("userId") Long userId,@Param("name") String name);

    @Update("UPDATE agent_token SET is_deleted = 1 WHERE token = #{token}")
    void deleteAgentTokenByToken(@Param("token") String token);

    Agent selectByToken(@Param("token") Long token);

}
