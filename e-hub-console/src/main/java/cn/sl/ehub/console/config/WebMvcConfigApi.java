package cn.sl.ehub.console.config;

import cn.sl.ehub.console.config.CheckApisTokenInterceptor;

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
    public CheckApisTokenInterceptor checkUserTicketInterceptor() {
        return new CheckApisTokenInterceptor();
    }

    @Bean
    RequestHandlerInterceptorAdapter requestHandlerInterceptorAdapter() {
        return new RequestHandlerInterceptorAdapter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ticket拦截
        registry.addInterceptor(checkUserTicketInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/swagger-resources/**", "/webjars/**", "/webSocket/**", "/guangzhouDataSupport/**", "/externalData/**", "/guangzhouDataTimer/**",
                        "/v3/**", "/swagger-ui/**", "/error", "/dataSupport/**", "/data/**", "/bigScreen/**", "/bigScreenInsert/**", "/health/**", "/doc.html", "/favicon.ico", "/tripartData/**", "/sms/**", "/rdfa-timer/api");
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
