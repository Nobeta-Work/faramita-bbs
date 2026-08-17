package cn.nobeta.bbs.module.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import cn.nobeta.bbs.common.constant.NameConstant;


@Data
public class UserProfileDTO {

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 10, message = "昵称长度需在 1-10 之间")
    private String nickname;

    @Min(value = 0, message = "性别信息异常")
    @Max(value = 2, message = "性别信息异常")
    private Integer sex = NameConstant.DEFAULT_SEX;

    @Size(min = 1, max = 10, message = "种族长度需在 1-10 之间")
    private String race = NameConstant.DEFAULT_RACE;
}
