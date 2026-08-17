package cn.nobeta.bbs.module.agent.dto;

import cn.nobeta.bbs.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AgentTokenPageQuery extends PageQuery {

    private Long userId;
    
}
