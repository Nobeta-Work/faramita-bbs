package online.faramita.bbs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录接口响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录VO")
public class LoginVO {

    @Schema(title = "用户ID", description = "用户ID")
    private Long id;
    
    @Schema(title = "用户名", description = "用户名")
    private String username;
    
    @Schema(title = "昵称", description = "昵称")
    private String nickname;
    
    @Schema(title = "头像", description = "头像URL")
    private String avatar;
    
    @Schema(title = "jwt令牌", description = "jwt令牌")
    private String token;
}
