import java.time.LocalDateTime;

/**
 * 用户类
 */
public class User {
    private Long id;  // 主键
    private String username;    // 账户
    private String password;    // 密码
    private String nickname;    // 昵称
    private String avator;      // 头像URL
    private Integer sex;        // 性别 0=>女 1=>男 2=>神秘
    private String race;        // 种族
    private String signature;   // 个性签名
    private LocalDateTime joinTime;     // 注册时间
    private LocalDateTime updateTime;   // 资料修改时间  
}
