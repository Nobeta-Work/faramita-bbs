package online.faramita.bbs.config;

import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Swagger3
 * OpenAPI配置类
 */
@Configuration
public class SpringDocConfig {
    
    @Bean
    public OpenAPI openAPI() {
        Contact contact = new Contact()
                        .name("Nobeta")
                        .email("377877931@qq.com")
                        .url("https://www.faramita.online/bbs/1")
                        .extensions(new HashMap<String, Object>());

        Info info = new Info()
                        .title("Faramita BBS API Doc")
                        .description("彼岸论坛API文档")
                        .version("初渡v0.0.1")
                        .contact(contact);

        return new OpenAPI().info(info);
    }
}
