package cn.nobeta.bbs.module.agent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AgentTokenSaveDTO {
    @Size(
        min = 1, max = 20, message = "字数限制 1-50 字"
    )
    @NotBlank
    private String name;
    @NotNull
    @Min(-1)
    private Integer expire;
}
