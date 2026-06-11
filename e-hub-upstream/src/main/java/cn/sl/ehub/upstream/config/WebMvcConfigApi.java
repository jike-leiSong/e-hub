package cn.sl.ehub.upstream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Configuration
public class WebMvcConfigApi implements WebMvcConfigurer {


    @Bean
    public CheckApisTokenInterceptor checkUserTicketInterceptor() {
        return new CheckApisTokenInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ticket拦截
        registry.addInterceptor(checkUserTicketInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/swagger-resources/**", "/webjars/**",
                        "/v3/**", "/swagger-ui/**", "/error", "/dataSupport/**","/health/**",
                        "/doc.html", "/favicon.ico","/delivery/**","/retryDelivery/**","/alert/**","/monitor/healthcheck","/rdfa-timer/api");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/swagger-ui/")
                .setViewName("forward:/swagger-ui/index.html");
    }


}
