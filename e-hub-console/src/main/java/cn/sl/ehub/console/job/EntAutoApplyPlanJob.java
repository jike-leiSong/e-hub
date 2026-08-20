package cn.sl.ehub.console.job;

import cn.sl.ehub.console.service.IDataSupportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 将已提交默认计划自动物化为次日企业申报数据。
 * 默认关闭，避免迁移期间在未校验历史计划数据时产生批量写入。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aggregator.ent-auto-apply", name = "enabled", havingValue = "true")
public class EntAutoApplyPlanJob {

    private final IDataSupportService dataSupportService;

    @Scheduled(cron = "${aggregator.ent-auto-apply.cron:0 5 0 * * ?}")
    public void execute() {
        log.info("企业默认计划自动申报开始");
        try {
            dataSupportService.addAutoApplyPlan(null);
        } catch (Exception ex) {
            log.error("企业默认计划自动申报失败", ex);
        }
    }
}
