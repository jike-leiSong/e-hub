package cn.sl.ehub.upstream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务配置
 * 启用Spring @Scheduled注解支持
 *
 * @author sl
 * @date 2026-05-28
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * 配置定时任务线程池
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 线程池大小
        scheduler.setPoolSize(10);
        // 线程名前缀
        scheduler.setThreadNamePrefix("scheduled-task-");
        // 等待任务完成后再关闭线程池
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间（秒）
        scheduler.setAwaitTerminationSeconds(60);
        // 初始化
        scheduler.initialize();
        return scheduler;
    }
}
