package online.faramita.bbs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册接口请求DTO
 */
@Data
@Schema(description = "注册DTO")
public class RegisterDTO {

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9@#]{6,30}$", message = "用户名格式不正确")
    @Schema(title = "用户名（6-30位字母/数字/特殊字符）", description = "用户名")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度必须在6-30之间")
    @Schema(title = "密码（6-30位字母/数字/特殊字符）", description = "密码")
    private String password;
    
    @NotBlank(message = "昵称不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\s-]{1,20}$", message = "昵称格式不正确")
    @Schema(title = "昵称（1-20位非空字符，支持中文）", description = "昵称")
    private String nickname;
    
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "输入有问题")
    @Max(value = 2, message = "输入有问题")
    private Integer sex;
    
    @NotBlank(message = "种族不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\s-]{1,20}$", message = "种族格式不正确")
    @Schema(title = "种族（1-20位非空字符）", description = "种族")
    private String race;
    
    @Schema(title = "头像URL", description = "头像URL(uuid+扩展名)")
    private String avatar;
}
