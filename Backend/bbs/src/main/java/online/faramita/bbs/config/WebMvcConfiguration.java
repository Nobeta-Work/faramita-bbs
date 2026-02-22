package online.faramita.bbs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.interceptor.JwtTokenInterceptor;

@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer{
    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    /**
     * 注册自定义拦截器
     * @param registry
     */
    public void addInterceptors(InterceptorRegistry registry) {
        log.info(">开始注册自定义拦截器<");
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/api/{uid}/*")
                .addPathPatterns("/api/{uid}/**/*")
                .addPathPatterns("/api/{uid}/blog/*")
                .addPathPatterns("/api/blog/**")
                .addPathPatterns("/api/0/current")
                .excludePathPatterns("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**");
    }
}
