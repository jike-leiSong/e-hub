package cn.sl.ehub.console.config;

import cn.sl.ehub.console.auth.AuthInterceptor;
import cn.sl.ehub.console.auth.ConsoleAuthProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * 拦截器
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Configuration
public class WebMvcConfigApi implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final ConsoleAuthProperties authProperties;

    public WebMvcConfigApi(AuthInterceptor authInterceptor, ConsoleAuthProperties authProperties) {
        this.authInterceptor = authInterceptor;
        this.authProperties = authProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (Boolean.TRUE.equals(authProperties.getEnabled())) {
            registry.addInterceptor(authInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(authProperties.getExcludePaths());
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/console/**")
                .addResourceLocations("classpath:/static/console/")
                .resourceChain(false);

        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/console")
                .setViewName("redirect:/console/");
        registry.addViewController("/console/")
                .setViewName("forward:/console/index.html");

        registry.addViewController("/swagger-ui/")
                .setViewName("forward:/swagger-ui/index.html");
    }


}
