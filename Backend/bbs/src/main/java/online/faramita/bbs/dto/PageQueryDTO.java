package online.faramita.bbs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用分页查询参数DTO
 * @param <T>
 */
@Data
@Schema(description = "通用分页查询参数类")
public class PageQueryDTO<T> {
    @Schema(title = "页码", description = "页码")
    private Integer page = 1;       // 页码
    @Schema(title = "每页大小", description = "单页词条数量")
    private Integer pageSize = 10;  // 每页大小
    @Schema(title = "查询条件", description = "具体业务查询类")
    private T query;                // 具体业务查询类
}
