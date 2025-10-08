package online.faramita.bbs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录接口请求DTO
 */
@Data
@Schema(description = "登录DTO")
public class LoginDTO {

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9@#]{6,30}$", message = "用户名格式不正确")
    @Schema(title = "用户名（6-30位字母/数字/特殊字符）", description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度必须在6-30之间")
    @Schema(title = "密码（6-30位字母/数字/特殊字符）", description = "密码")
    private String password;
}
