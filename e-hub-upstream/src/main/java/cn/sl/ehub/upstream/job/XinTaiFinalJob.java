package cn.sl.ehub.upstream.job;

import cn.sl.ehub.upstream.config.RedisLock;
import cn.sl.ehub.upstream.service.DeliveryServiceXinTai;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;

/**
 * 鑫泰华北电网上送定时任务
 * 改造说明：从rdfa-timer改为Spring @Scheduled
 *
 * @Author sl
 * @Date 2026-05-28
 **/
@Slf4j
@Component
public class XinTaiFinalJob {

    @Autowired
    private Environment environment;

    @Resource
    private DeliveryServiceXinTai deliveryServiceXinTai;

    @Resource
    private RedisTemplate redisTemplate;

    @Qualifier("jobThreadPool")
    @Autowired
    private ExecutorService jobThreadPool;

    /**
     * 聚合商ID，从配置文件读取
     */
    @Value("${xintai.aggregator.id:1711340903453614082}")
    private String aggregatorId;

    /**
     * 鑫泰华北电网上送定时任务
     * 执行频率：每分钟执行一次
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 * * * * ?")
    public void execute() {
        jobThreadPool.execute(() -> {
            try {
                // 当前时间 格式为分钟 格式为 yyyy-MM-dd HH:mm
                String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String lockKey = String.format("VPP:XINTAIHB:LOCK:%s:%s", aggregatorId, now);
                RedisLock lock = new RedisLock(redisTemplate, lockKey);
                log.info("鑫泰华北电网上送服务：lockKey:{}", lockKey);

                if (lock.lockV3()) {
                    log.info("鑫泰华北电网上送服务-获取定时任务锁成功");
                    deliveryServiceXinTai.totalDataDelivery(aggregatorId);
                } else {
                    log.info("鑫泰华北电网上送服务-未取到定时任务锁，已有其他节点执行");
                }
            } catch (Exception e) {
                log.error("鑫泰华北电网上送服务-获取定时任务锁异常:{}", ExceptionUtils.getStackTrace(e));
            }
        });
    }
}
