package online.faramita.bbs.module.like.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeBlogChangelog {

    private Long blogId;
    private Long userId;
    private boolean isLikeAction;
    private LocalDateTime timestamp;
}
