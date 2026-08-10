package cn.nobeta.bbs.module.blog.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogTagBriefRelations {

    private Long blogId;
    private Long tagId;
    private String tagName;

}
