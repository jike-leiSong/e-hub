package cn.sl.ehub.upstream.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class TreadPoolConfig {

    @Bean(value = "pvsBusinessThreadPool")
    public ExecutorService pvsBusinessThreadPool() {
        return new ThreadPoolExecutor(
                4,
                64,
                5L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("t-biz-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(value = "jobThreadPool")
    public ExecutorService jobThreadPool() {
        return new ThreadPoolExecutor(
                4,
                64,
                5L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("t-biz-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }


}