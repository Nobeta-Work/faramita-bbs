package cn.nobeta.bbs.module.auth.vo;


import java.util.Date;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenVO {

    private String accessToken;
    private String refreshToken;
    private Date expireIn;
    
}
