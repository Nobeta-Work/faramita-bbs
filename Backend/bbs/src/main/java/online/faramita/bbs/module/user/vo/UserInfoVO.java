package online.faramita.bbs.module.user.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer sex;
    private String race;
    private String signature;
    private List<String> roles;
    private LocalDateTime createTime;
    
}
