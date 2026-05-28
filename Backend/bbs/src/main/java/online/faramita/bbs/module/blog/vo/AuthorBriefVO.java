package online.faramita.bbs.module.blog.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorBriefVO {

    private Long id;
    private String nickname;
    private String avatar;

}
