package cn.nobeta.bbs.module.admin.dto;

import cn.nobeta.bbs.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PermPageQuery extends PageQuery {

    /** 关键字：权限编码 / 权限名 模糊匹配 */
    private String keyword;

}
