package cn.sl.ehub.console.job;

import cn.sl.ehub.console.service.IDataSupportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 聚合商自动申报计划定时任务
 * 改造说明：从rdfa-timer改为Spring @Scheduled
 *
 * @author sl
 * @date 2026-05-29
 */
@Slf4j
@Component
public class AggregatorApplyPlanJob {

    @Resource
    private IDataSupportService dataSupportService;

    /**
     * 聚合商ID，从配置文件读取
     */
    @Value("${aggregator.apply.plan.aggregatorId:}")
    private String aggregatorId;

    /**
     * 聚合商自动申报计划
     * 执行时间：每天早上8点执行
     * cron表达式：0 0 8 * * ?
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void execute() {
        log.info("聚合商自动申报计划开始，aggregatorId: {}", aggregatorId);
        try {
            dataSupportService.autoApplyPlan(aggregatorId);
            log.info("聚合商自动申报计划结束");
        } catch (Exception e) {
            log.error("聚合商自动申报计划执行失败", e);
        }
    }
}
