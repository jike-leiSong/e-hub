package cn.sl.ehub.upstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * E-Hub Upstream Application
 * 电网上行服务启动类
 *
 * @author sl
 * @date 2026-05-28
 */
@SpringBootApplication
@ComponentScan(basePackages = {"cn.sl.ehub.upstream", "cn.sl.ehub.service"})
@MapperScan(basePackages = {"cn.sl.ehub.upstream.mapper", "cn.sl.ehub.service.mapper"})
@EnableAsync
public class EHubUpstreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(EHubUpstreamApplication.class, args);
    }

}
