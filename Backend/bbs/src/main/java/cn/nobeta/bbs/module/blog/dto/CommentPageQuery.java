package cn.nobeta.bbs.module.blog.dto;

import cn.nobeta.bbs.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentPageQuery extends PageQuery {

    public CommentPageQuery() {
        setPageNum(1);
        setPageSize(20);
        setSortOrder("desc");
    }
}
