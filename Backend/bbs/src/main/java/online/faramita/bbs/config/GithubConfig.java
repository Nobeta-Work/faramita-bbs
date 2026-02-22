package online.faramita.bbs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "faramita.github")
public class GithubConfig {
    public String branch;
    public String name;
    public String email;
    public String owner;
    public String repo;
    public String path;
    private String token;

    public String getAuthorization() {
        return "token " + token;
    }
}
