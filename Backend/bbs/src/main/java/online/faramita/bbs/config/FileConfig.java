package online.faramita.bbs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 文件信息配置类
 */
@Component
@Data
@ConfigurationProperties(prefix = "faramita.file")
public class FileConfig {
    
    // 头像文件配置
    private AvatarConfig avatar = new AvatarConfig();

    /**
     * 头像配置内部类
     */
    @Data
    public static class AvatarConfig {
        private String rootPath;
        private Long maxSize;
        private Integer expire;
    }
}
