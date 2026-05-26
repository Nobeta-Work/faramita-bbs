package online.faramita.bbs.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(
        regexp = "^[a-zA-Z0-9!@#._-]{4,20}$",
        message = "用户名仅支持字母、数字及 ! @ # . _ - 符号"
    )
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(
        regexp = "^[a-zA-Z0-9!@#._-]{4,20}$",
        message = "密码仅支持字母、数字及 ! @ # . _ - 符号"
    )
    private String password;

}
