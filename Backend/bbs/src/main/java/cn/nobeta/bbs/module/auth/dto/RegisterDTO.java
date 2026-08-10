package cn.nobeta.bbs.module.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import cn.nobeta.bbs.common.constant.AuthConstant;

@Data
public class RegisterDTO {

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

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 10, message = "昵称长度需在 1-10 之间")
    private String nickname;

    @Min(value = 0, message = "性别信息异常")
    @Max(value = 2, message = "性别信息异常")
    private Integer sex = AuthConstant.DEFAULT_SEX;

    @Size(min = 1, max = 10, message = "种族长度需在 1-10 之间")
    private String race = AuthConstant.DEFAULT_RACE;
}
