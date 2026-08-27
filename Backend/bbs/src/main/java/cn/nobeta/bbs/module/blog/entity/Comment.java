package cn.nobeta.bbs.module.blog.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Comment {

    private Long id;
    private Long blogId;
    private Long userId;
    private Long parentId;
    private Long rootId;
    private String content;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
