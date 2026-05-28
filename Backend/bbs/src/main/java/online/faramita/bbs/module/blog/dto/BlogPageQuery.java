package online.faramita.bbs.module.blog.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import online.faramita.bbs.common.dto.PageQuery;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BlogPageQuery extends PageQuery {

    private String keyword;     // 关键词
    private Long authorId;      // 作者 ID
    private List<Long> tagIds;  // tag ID 列表
    private Long userId;        // 查询者 Id

}
