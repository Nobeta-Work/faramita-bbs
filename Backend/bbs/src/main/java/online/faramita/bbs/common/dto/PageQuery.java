package online.faramita.bbs.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageQuery {

    @Min(value = 1, message = "排序字段不能小于1")
    private Integer pageNum;     // 当前页码

    @Min(value = 1, message = "单页条数不能小于1")
    @Max(value = 100, message = "单页条数不能大于100")
    private Integer pageSize;    // 单页条数
    
    private String sortField; // 排序字段

    private String sortOrder = "desc"; // 排序方式 (默认 desc: 倒序)

}
