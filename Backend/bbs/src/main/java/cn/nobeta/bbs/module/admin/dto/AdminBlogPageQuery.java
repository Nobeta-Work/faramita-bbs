package cn.nobeta.bbs.module.admin.dto;

import cn.nobeta.bbs.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminBlogPageQuery extends PageQuery {

    /** 关键字：博客标题 模糊匹配 */
    private String keyword;

    /** 作者 id */
    private Long authorId;

    /** 是否公开 0:私有 1:公开 */
    private Integer isPublished;

}
