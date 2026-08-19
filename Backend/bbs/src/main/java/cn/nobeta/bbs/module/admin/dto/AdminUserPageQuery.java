package cn.nobeta.bbs.module.admin.dto;

import cn.nobeta.bbs.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminUserPageQuery extends PageQuery {

    /** 关键字：用户名 / 昵称 模糊匹配 */
    private String keyword;

    /** 状态 0:封禁 1:正常 */
    private Integer status;

}
