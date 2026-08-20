package cn.sl.ehub.console.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "console.auth")
public class ConsoleAuthProperties {

    private Boolean enabled = true;

    private String tokenSecret;

    /** 会话存储方式：redis（默认，支持多实例）或 local（仅开发调试）。 */
    private String sessionStore = "redis";

    private Long expireMinutes = 720L;

    private List<String> excludePaths = new ArrayList<>(Arrays.asList(
            "/auth/login",
            "/console",
            "/console/",
            "/console/**",
            "/health/**",
            "/data-collector/**",
            "/error",
            "/favicon.ico",
            "/doc.html",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/v2/api-docs",
            "/v3/api-docs/**"
    ));
}
