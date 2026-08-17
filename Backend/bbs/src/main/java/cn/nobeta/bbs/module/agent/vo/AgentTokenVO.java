package cn.nobeta.bbs.module.agent.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentTokenVO {
    private String token;
    private String name;
    private Integer expire;
}