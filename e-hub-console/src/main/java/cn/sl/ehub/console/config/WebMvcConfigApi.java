package cn.sl.ehub.console.config;

import com.fanneng.requestlog.config.RequestHandlerInterceptorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    @Bean
    RequestHandlerInterceptorAdapter requestHandlerInterceptorAdapter() {
        return new RequestHandlerInterceptorAdapter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestHandlerInterceptorAdapter());
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
