package cn.nobeta.bbs.module.user.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户实体类对象")
public class User {
    @Schema(title = "uid", description = "唯一用户标识")
    private Long id;  // 主键
    @Schema(title = "账号", description = "用户账号")
    private String username;    // 账户
    @Schema(title = "密码", description = "用户加密密码")
    private String password;    // 密码
    @Schema(title = "昵称", description = "用户昵称")
    private String nickname;    // 昵称
    @Schema(title = "头像", description = "用户头像URL")
    private String avatar;      // 头像URL
    @Schema(title = "性别", description = "用户性别")
    private Integer sex;        // 性别 0=>女 1=>男 2=>神秘
    @Schema(title = "种族", description = "用户种族")
    private String race;        // 种族
    @Schema(title = "个性签名", description = "用户个性签名")
    private String signature;   // 个性签名

    private Integer status;     // 状态 0:封禁 1:正常
    @Schema(title = "注册时间", description = "用户注册时间")
    private LocalDateTime createTime;     // 注册时间
    @Schema(title = "资料修改时间", description = "用户资料修改时间")
    private LocalDateTime updateTime;   // 资料修改时间
}
