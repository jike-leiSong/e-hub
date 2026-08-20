package cn.sl.ehub.common.config;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * @Description: 跨域配置
 * @Author sl
 * @Date 2026-05-28
 */
@Configuration
public class CorsConfig {

    @Value("${allowed.origins:}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        // 允许cookies跨域
        config.setAllowCredentials(true);
        // 不再使用通配来源。允许携带凭证时，通配来源会把任意站点带入信任边界。
        // 生产环境通过 allowed.origins 配置可信前端域名；未配置时仅允许同源访问。
        if (CollectionUtils.isNotEmpty(allowedOrigins)) {
            config.setAllowedOrigins(allowedOrigins);
        }
        // #允许访问的头信息,*表示全部
        config.addAllowedHeader("*");
        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        config.setMaxAge(18000L);
        // 允许提交请求的方法，*表示全部允许
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
