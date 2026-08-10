package cn.nobeta.bbs.module.user.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserBriefVO {

    private Long id;
    private String nickname;
    private String avatar;

}
