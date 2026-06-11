package cn.sl.ehub.upstream.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.sl.ehub.upstream.config.RedisLock;
import cn.sl.ehub.upstream.service.PeakPlanDeliveryService;
import lombok.extern.slf4j.Slf4j;

/**
 * 调峰计划申报日数据电网上送Job（日运行指标上报）
 *
 * 参考总加上送实现，只传aggregatorId（聚合商ID）
 * 自动查询该聚合商下的所有资源类型，上送所有资源的次日数据
 *
 * 改造说明：从rdfa-timer改为Spring @Scheduled
 *
 * @author sl
 * @date 2026-05-28
 */
@Slf4j
@Component
public class PeakPlanDailyDataDeliveryJob {

    private final String[] ENVS = {"pro", "prod"};

    @Autowired
    private Environment environment;

    @Resource
    private PeakPlanDeliveryService peakPlanDeliveryService;

    @Resource
    private RedisTemplate redisTemplate;

    @Qualifier("jobThreadPool")
    @Autowired
    private ExecutorService jobThreadPool;

    /**
     * 聚合商ID，从配置文件读取
     */
    @Value("${peak.plan.aggregator.id:}")
    private String aggregatorId;

    private boolean isProdEnv() {
        String env = this.environment.getProperty("env");
        if (StringUtils.equalsAnyIgnoreCase(env, ENVS)) {
            return true;
        }
        return Boolean.FALSE.booleanValue();
    }

    /**
     * 调峰计划申报日数据电网上送定时任务
     * 执行频率：每天凌晨1点执行
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void execute() {
        // 如果未配置aggregatorId，则不执行
        if (StringUtils.isBlank(aggregatorId)) {
            log.warn("调峰计划申报日数据电网上送服务-未配置aggregatorId，跳过执行");
            return;
        }

        // 异步执行任务
        jobThreadPool.execute(() -> {
            try {
                // 当前时间 格式为分钟 格式为 yyyy-MM-dd HH:mm
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String lockKey = String.format("VPP:PEAK_PLAN:DAILY:DELIVERY:LOCK:%s:%s", aggregatorId, now);
                RedisLock lock = new RedisLock(redisTemplate, lockKey);
                log.info("调峰计划申报日数据电网上送服务：lockKey:{}", lockKey);

                if (lock.lockV3()) {
                    log.info("调峰计划申报日数据电网上送服务-获取定时任务锁成功");
                    // aggregatorId = 聚合商ID
                    boolean result = peakPlanDeliveryService.executeDailyDataDelivery(aggregatorId);
                    log.info("调峰计划申报日数据电网上送服务-执行结果：{}", result ? "成功" : "失败");
                } else {
                    log.info("调峰计划申报日数据电网上送服务-未取到定时任务锁，已有其他节点执行");
                }
            } catch (Exception e) {
                log.error("调峰计划申报日数据电网上送服务-执行异常：{}", ExceptionUtils.getStackTrace(e));
            }
        });
    }
}
