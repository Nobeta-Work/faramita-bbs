package cn.nobeta.bbs.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "para.github")
public class GithubProperties {
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
