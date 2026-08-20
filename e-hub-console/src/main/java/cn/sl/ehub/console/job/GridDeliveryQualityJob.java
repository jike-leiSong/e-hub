package cn.sl.ehub.console.job;

import cn.sl.ehub.console.service.GridDeliveryQualityManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
public class GridDeliveryQualityJob {

    private final GridDeliveryQualityManagementService service;

    @Value("${grid.delivery.quality.enabled:true}")
    private boolean enabled;

    public GridDeliveryQualityJob(GridDeliveryQualityManagementService service) {
        this.service = service;
    }

    @Scheduled(cron = "${grid.delivery.quality.current-cron:0 7/15 * * * ?}")
    public void inspectCurrentDay() {
        inspect(LocalDate.now(), false);
    }

    @Scheduled(cron = "${grid.delivery.quality.final-cron:0 20 0 * * ?}")
    public void finalizePreviousDay() {
        inspect(LocalDate.now().minusDays(1), true);
    }

    private void inspect(LocalDate date, boolean includeEnded) {
        if (!enabled) {
            return;
        }
        for (Map<String, Object> scope : service.automaticScopes(date)) {
            String aggregatorId = String.valueOf(scope.get("aggregatorId"));
            String resourceTypeId = scope.get("resourceTypeId") == null ? null
                    : String.valueOf(scope.get("resourceTypeId"));
            try {
                service.inspectDay(aggregatorId, date, resourceTypeId, null, resourceTypeId);
            } catch (Exception ex) {
                log.error("电网上送质量自动核查失败, aggregatorId={}, resourceTypeId={}, date={}",
                        aggregatorId, resourceTypeId, date, ex);
            }
        }
    }
}
