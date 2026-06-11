package cn.sl.ehub.upstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * E-Hub Console Application
 * 控制台服务启动类
 *
 * @author sl
 * @date 2026-05-28
 */
@SpringBootApplication
@MapperScan(basePackages = "cn.sl.ehub.upstream.mapper")
@EnableAsync
@EnableScheduling
public class EHubConsoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(EHubConsoleApplication.class, args);
    }
}
