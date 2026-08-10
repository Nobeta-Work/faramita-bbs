package cn.nobeta.bbs.module.user.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO {

    private Long id;
    private String nickname;
    private String avatar;
    private Integer sex;
    private String race;
    private String signature;
    private LocalDateTime createTime;

}
