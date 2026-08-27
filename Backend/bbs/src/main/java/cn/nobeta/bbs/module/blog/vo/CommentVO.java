package cn.nobeta.bbs.module.blog.vo;

import java.time.LocalDateTime;
import java.util.List;

import cn.nobeta.bbs.module.user.vo.UserBriefVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentVO {

    private Long id;
    private Long blogId;
    private Long parentId;
    private Long rootId;
    private String content;
    private Integer likeCount;
    private Integer status;
    private UserBriefVO author;
    private UserBriefVO replyTo;
    private Integer replyCount;
    private List<CommentVO> replies;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
