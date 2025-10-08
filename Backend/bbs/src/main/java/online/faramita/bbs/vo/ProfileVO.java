package online.faramita.bbs.vo;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import online.faramita.bbs.entity.Blog;
import online.faramita.bbs.entity.User;

/**
 * 个人资料查询
 */
@Data
@Builder
public class ProfileVO {
    private User user;
    private List<Blog> blogList;
}
