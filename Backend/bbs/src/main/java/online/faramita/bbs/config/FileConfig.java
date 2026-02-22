package online.faramita.bbs.config;

import java.util.Arrays;
import java.util.List;

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

    // 图片文件配置
    private ImageConfig image = new ImageConfig();

    /**
     * 图片配置内部类
     */
    @Data
    public static class ImageConfig {
        private Long maxSize;
        private List<String> acceptTypes = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg");
    }
}
