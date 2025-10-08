package online.faramita.bbs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * JWT令牌配置类
 */
@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;  // 加密密钥
    private long expire;    // 有效时间
    private String header;  // header名称
}
