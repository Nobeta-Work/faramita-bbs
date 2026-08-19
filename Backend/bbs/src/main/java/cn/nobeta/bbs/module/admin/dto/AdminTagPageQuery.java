package cn.nobeta.bbs.module.admin.dto;

import cn.nobeta.bbs.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminTagPageQuery extends PageQuery {

    /** 关键字：标签名 / 标签描述 模糊匹配 */
    private String keyword;

}
