package online.faramita.bbs.module.blog.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import online.faramita.bbs.module.tag.vo.TagBriefVO;
import online.faramita.bbs.module.user.vo.UserBriefVO;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BlogBaseVO {

    private Long id;
    private String title;
    private String summary;
    private Integer isPublished;
    private Integer likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private UserBriefVO author;
    private List<TagBriefVO> tags;
}
