package cn.nobeta.bbs.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordEditDTO {

    @NotBlank(message = "旧密码不能为空")
    @Size(min = 4, max = 20, message = "密码长度需在4-20位")
    @Pattern(regexp = "^[a-zA-Z0-9!@#._-]{4,20}$", message = "密码仅支持字母、数字及 ! @ # . _ - 符号")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 4, max = 20, message = "密码长度需在4-20位")
    @Pattern(regexp = "^[a-zA-Z0-9!@#._-]{4,20}$", message = "密码仅支持字母、数字及 ! @ # . _ - 符号")
    private String newPassword;

}
