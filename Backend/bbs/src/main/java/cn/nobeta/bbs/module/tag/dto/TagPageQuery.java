package cn.nobeta.bbs.module.tag.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import cn.nobeta.bbs.common.dto.PageQuery;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TagPageQuery extends PageQuery{

    @Size(min = 1, max = 20, message = "标签名长度限制 1-20 字符")
    private String keyword;     // 关键词

}
