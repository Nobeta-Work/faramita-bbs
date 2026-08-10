package cn.nobeta.bbs.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * JWT令牌配置类
 */
@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;  // 加密密钥
    private Long accessTokenExpire; // 业务令牌有效时间 单位s
    private Long refreshTokenExpire; // 刷新令牌有效时间 单位s
}
