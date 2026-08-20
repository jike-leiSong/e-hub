package cn.sl.ehub.console.controller.loadaggregation;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.console.auth.AuthContext;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.service.GridDeliveryQualityService;
import cn.sl.ehub.console.service.GridDeliveryQualityManagementService;
import cn.sl.ehub.console.service.GridDeliveryReportService;
import cn.sl.ehub.console.service.GridDeliveryOperationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/grid-interaction", "/grid-delivery-quality"})
@Api(tags = "电网交互与上送核查")
public class GridDeliveryQualityController {

    private final GridDeliveryQualityService service;
    private final GridDeliveryQualityManagementService managementService;
    private final GridDeliveryReportService reportService;
    private final LoadAggregationScopeService scopeService;
    private final GridDeliveryOperationService operationService;

    public GridDeliveryQualityController(GridDeliveryQualityService service,
                                         GridDeliveryQualityManagementService managementService,
                                         GridDeliveryReportService reportService,
                                         LoadAggregationScopeService scopeService,
                                         GridDeliveryOperationService operationService) {
        this.service = service;
        this.managementService = managementService;
        this.reportService = reportService;
        this.scopeService = scopeService;
        this.operationService = operationService;
    }

    @GetMapping("/daily-overview")
    @ApiOperation("查询每日上送完整性、对账和参与口径")
    public ResultVO<Map<String, Object>> dailyOverview(@RequestParam String aggregatorId,
                                                       @RequestParam String date,
                                                       @RequestParam(required = false) String channelNo,
                                                       @RequestParam String resourceTypeId) {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        Map<String, Object> result = service.dailyOverview(scopedAggregator, date, channelNo, resourceTypeId);
        result.putAll(managementService.marketStatus(scopedAggregator, resourceTypeId, date));
        return ResultVO.success(result);
    }

    @GetMapping("/operations/model-preview")
    @ApiOperation("预览当前单体模型参与口径")
    public ResultVO<Map<String, Object>> modelPreview(@RequestParam String aggregatorId,
                                                      @RequestParam String resourceTypeId) {
        return ResultVO.success(operationService.preview(scopeService.resolveAggregatorId(aggregatorId), resourceTypeId));
    }

    @PostMapping("/operations/send-model")
    @ApiOperation("人工上送所选能源的全量单体模型")
    public ResultVO<Map<String, Object>> sendModel(@RequestParam String aggregatorId,
                                                   @RequestParam String resourceTypeId) {
        denyEntManage();
        return ResultVO.success(operationService.sendModel(scopeService.resolveAggregatorId(aggregatorId), resourceTypeId,
                AuthContext.get() == null ? null : AuthContext.get().getUserId(),
                AuthContext.get() == null ? null : AuthContext.get().getDisplayName()));
    }

    @PostMapping("/operations/retry-single")
    @ApiOperation("人工补送指定15分钟时刻的单体量测")
    public ResultVO<Map<String, Object>> retrySingle(@RequestParam String aggregatorId,
                                                     @RequestParam String resourceTypeId,
                                                     @RequestParam Long time) {
        denyEntManage();
        if (time == null || time <= 0) throw new IllegalArgumentException("补送时刻不能为空");
        return ResultVO.success(operationService.retrySingle(scopeService.resolveAggregatorId(aggregatorId),
                resourceTypeId, time, AuthContext.get() == null ? null : AuthContext.get().getUserId(),
                AuthContext.get() == null ? null : AuthContext.get().getDisplayName()));
    }

    @PostMapping("/operations/retry-single-range")
    @ApiOperation("人工补送指定能源的连续15分钟量测批次")
    public ResultVO<Map<String, Object>> retrySingleRange(@RequestParam String aggregatorId,
                                                          @RequestParam String resourceTypeId,
                                                          @RequestParam String startTime,
                                                          @RequestParam String endTime) {
        denyEntManage();
        return ResultVO.success(operationService.retrySingleRange(scopeService.resolveAggregatorId(aggregatorId),
                resourceTypeId, startTime, endTime,
                AuthContext.get() == null ? null : AuthContext.get().getUserId(),
                AuthContext.get() == null ? null : AuthContext.get().getDisplayName()));
    }

    @GetMapping("/connection-overview")
    @ApiOperation("查询电网对接工作台状态")
    public ResultVO<Map<String, Object>> connectionOverview(@RequestParam String aggregatorId,
                                                             @RequestParam String resourceTypeId,
                                                             @RequestParam String dataDate) {
        return ResultVO.success(operationService.connectionOverview(
                scopeService.resolveAggregatorId(aggregatorId), resourceTypeId, dataDate));
    }

    @GetMapping("/market-status")
    @ApiOperation("查询人工标记的当前市场参与状态")
    public ResultVO<Map<String, Object>> marketStatus(@RequestParam String aggregatorId,
                                                      @RequestParam String resourceTypeId,
                                                      @RequestParam(required = false) String date) {
        return ResultVO.success(managementService.marketStatus(scopeService.resolveAggregatorId(aggregatorId),
                resourceTypeId, date));
    }

    @PostMapping("/market-status")
    @ApiOperation("人工标记能源类型的当前市场参与状态（不影响上送及核查）")
    public ResultVO<Map<String, Object>> updateMarketStatus(@RequestParam String aggregatorId,
                                                            @RequestParam String resourceTypeId,
                                                            @RequestParam boolean enabled,
                                                            @RequestParam(required = false) String remark) {
        denyEntManage();
        return ResultVO.success(managementService.updateMarketStatus(
                scopeService.resolveAggregatorId(aggregatorId), resourceTypeId, enabled, remark,
                AuthContext.get() == null ? null : AuthContext.get().getUserId(),
                AuthContext.get() == null ? null : AuthContext.get().getDisplayName()));
    }

    @GetMapping("/peak-plan/status")
    @ApiOperation("检查调峰计划数据准备及最近上送状态")
    public ResultVO<Map<String, Object>> peakPlanStatus(@RequestParam String aggregatorId,
                                                        @RequestParam String resourceTypeId,
                                                        @RequestParam String dataDate) {
        return ResultVO.success(operationService.peakPlanStatus(
                scopeService.resolveAggregatorId(aggregatorId), resourceTypeId, dataDate));
    }

    @GetMapping("/operations")
    @ApiOperation("分页查询电网人工操作记录")
    public ResultVO<Map<String, Object>> operations(@RequestParam String aggregatorId,
                                                    @RequestParam String resourceTypeId,
                                                    @RequestParam(required = false) String operationType,
                                                    @RequestParam(defaultValue = "1") int pageIndex,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        return ResultVO.success(operationService.operationRecords(scopeService.resolveAggregatorId(aggregatorId),
                resourceTypeId, operationType, pageIndex, pageSize));
    }

    @GetMapping("/summary")
    @ApiOperation("查询考察期上送质量总览")
    public ResultVO<Map<String, Object>> summary(@RequestParam String aggregatorId,
                                                 @RequestParam String startDate,
                                                 @RequestParam String endDate,
                                                 @RequestParam(required = false) String channelNo,
                                                 @RequestParam(required = false) Long periodId,
                                                 @RequestParam String resourceTypeId) {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, startDate, endDate, channelNo, periodId, resourceTypeId);
        Map<String, Object> result = service.summary(scopedAggregator, startDate, endDate, channelNo, resourceTypeId);
        result.put("periodId", periodId);
        result.put("standardRate", periodId == null ? null
                : managementService.period(scopedAggregator, periodId).get("standardRate"));
        return ResultVO.success(result);
    }

    @GetMapping("/daily")
    @ApiOperation("查询单日总加或单体量测点位")
    public ResultVO<List<Map<String, Object>>> daily(@RequestParam String aggregatorId,
                                                     @RequestParam String date,
                                                     @RequestParam(defaultValue = "TOTAL") String type,
                                                     @RequestParam(required = false) String channelNo,
                                                     @RequestParam(required = false) Long periodId,
                                                     @RequestParam String resourceTypeId) {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, date, date, channelNo, periodId, resourceTypeId);
        return ResultVO.success(service.daily(scopedAggregator, date, type, channelNo, resourceTypeId));
    }

    @GetMapping("/daily-page")
    @ApiOperation("分页查询单日总加或单体量测点位")
    public ResultVO<Map<String, Object>> dailyPage(@RequestParam String aggregatorId,
                                                   @RequestParam String date,
                                                   @RequestParam(defaultValue = "TOTAL") String type,
                                                   @RequestParam(required = false) String channelNo,
                                                   @RequestParam(required = false) Long periodId,
                                                   @RequestParam String resourceTypeId,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String singleCode,
                                                   @RequestParam(defaultValue = "1") int pageIndex,
                                                   @RequestParam(defaultValue = "100") int pageSize) {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, date, date, channelNo, periodId, resourceTypeId);
        return ResultVO.success(service.dailyPage(scopedAggregator, date, type, channelNo, resourceTypeId,
                status, singleCode, pageIndex, pageSize));
    }

    @GetMapping("/reconciliation")
    @ApiOperation("查询总加单体对账")
    public ResultVO<List<Map<String, Object>>> reconciliation(@RequestParam String aggregatorId,
                                                              @RequestParam String date,
                                                              @RequestParam(required = false) String channelNo,
                                                              @RequestParam(required = false) Long periodId,
                                                              @RequestParam String resourceTypeId) {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, date, date, channelNo, periodId, resourceTypeId);
        return ResultVO.success(service.reconciliation(scopedAggregator, date, channelNo, resourceTypeId));
    }

    @GetMapping("/participation")
    @ApiOperation("查询历史参与范围")
    public ResultVO<List<Map<String, Object>>> participation(@RequestParam String aggregatorId,
                                                             @RequestParam String date,
                                                             @RequestParam String resourceTypeId) {
        return ResultVO.success(service.participation(scopeService.resolveAggregatorId(aggregatorId), date,
                resourceTypeId));
    }

    @PostMapping("/snapshot")
    @ApiOperation("固化当前参与范围快照")
    public ResultVO<Map<String, Object>> createSnapshot(@RequestParam String aggregatorId,
                                                        @RequestParam String effectiveStart,
                                                        @RequestParam(required = false) String snapshotName) {
        if (scopeService.isEntCustomer(AuthContext.get())) {
            throw new BaseException(StatusCode.C.getCode(), "企业账号无权变更聚合商参与范围");
        }
        return ResultVO.success(service.createSnapshot(scopeService.resolveAggregatorId(aggregatorId), effectiveStart, snapshotName));
    }

    @PostMapping("/recalculate")
    @ApiOperation("手动重算指定日期范围")
    public ResultVO<Map<String, Object>> recalculate(@RequestParam String aggregatorId,
                                                     @RequestParam String startDate,
                                                     @RequestParam String endDate,
                                                     @RequestParam(required = false) String channelNo,
                                                     @RequestParam(required = false) Long periodId,
                                                     @RequestParam String resourceTypeId) {
        denyEntManage();
        return ResultVO.success(managementService.recalculate(scopeService.resolveAggregatorId(aggregatorId),
                startDate, endDate, channelNo, periodId, resourceTypeId));
    }

    @GetMapping("/trend")
    @ApiOperation("查询每日质量趋势和连续达标天数")
    public ResultVO<Map<String, Object>> trend(@RequestParam String aggregatorId,
                                               @RequestParam String startDate,
                                               @RequestParam String endDate,
                                               @RequestParam(required = false) String channelNo,
                                               @RequestParam(required = false) Long periodId,
                                               @RequestParam String resourceTypeId) {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, startDate, endDate, channelNo, periodId, resourceTypeId);
        return ResultVO.success(managementService.trend(scopedAggregator, startDate, endDate, channelNo, periodId, resourceTypeId));
    }

    @GetMapping("/issues")
    @ApiOperation("分页查询质量问题")
    public ResultVO<Map<String, Object>> issues(@RequestParam String aggregatorId,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate,
                                                @RequestParam(required = false) String issueType,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String channelNo,
                                                @RequestParam(required = false) Long periodId,
                                                @RequestParam String resourceTypeId,
                                                @RequestParam(defaultValue = "1") int pageIndex,
                                                @RequestParam(defaultValue = "50") int pageSize) {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, startDate, endDate, channelNo, periodId, resourceTypeId);
        return ResultVO.success(managementService.issues(scopedAggregator, startDate,
                endDate, issueType, status, channelNo, periodId, resourceTypeId, pageIndex, pageSize));
    }

    @GetMapping("/periods")
    @ApiOperation("查询电网上送周期")
    public ResultVO<List<Map<String, Object>>> periods(@RequestParam String aggregatorId,
                                                       @RequestParam String resourceTypeId) {
        List<Map<String, Object>> periods = managementService.periods(scopeService.resolveAggregatorId(aggregatorId));
        if (resourceTypeId == null || resourceTypeId.trim().isEmpty()) {
            return ResultVO.success(periods);
        }
        periods.removeIf(item -> !resourceTypeId.equals(String.valueOf(item.get("resourceTypeId"))));
        return ResultVO.success(periods);
    }

    @PostMapping("/periods")
    @ApiOperation("创建电网上送周期")
    public ResultVO<Map<String, Object>> createPeriod(@RequestParam String aggregatorId,
                                                      @RequestParam String periodName,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate,
                                                      @RequestParam(required = false) String channelNo,
                                                      @RequestParam String resourceTypeId,
                                                      @RequestParam(defaultValue = "ACTIVE") String status,
                                                      @RequestParam(required = false) BigDecimal standardRate,
                                                      @RequestParam(defaultValue = "7") Integer requiredDays,
                                                      @RequestParam(required = false) String remark) {
        denyEntManage();
        return ResultVO.success(managementService.createPeriod(scopeService.resolveAggregatorId(aggregatorId),
                periodName, startDate, endDate, channelNo, status, standardRate, requiredDays, remark, resourceTypeId));
    }

    @PutMapping("/periods/{periodId}")
    @ApiOperation("维护或结束电网上送周期")
    public ResultVO<Boolean> updatePeriod(@RequestParam String aggregatorId,
                                          @PathVariable long periodId,
                                          @RequestParam String periodName,
                                          @RequestParam String startDate,
                                          @RequestParam String endDate,
                                          @RequestParam(required = false) String channelNo,
                                          @RequestParam String resourceTypeId,
                                          @RequestParam String status,
                                          @RequestParam(required = false) BigDecimal standardRate,
                                          @RequestParam(defaultValue = "7") Integer requiredDays,
                                          @RequestParam(required = false) String remark) {
        denyEntManage();
        managementService.updatePeriod(scopeService.resolveAggregatorId(aggregatorId), periodId, periodName,
                startDate, endDate, channelNo, status, standardRate, requiredDays, remark, resourceTypeId);
        return ResultVO.success(true);
    }

    @GetMapping("/issues/{issueId}")
    @ApiOperation("查询问题及设备级排查明细")
    public ResultVO<Map<String, Object>> issueDetail(@RequestParam String aggregatorId,
                                                     @PathVariable long issueId) {
        return ResultVO.success(managementService.issueDetail(scopeService.resolveAggregatorId(aggregatorId), issueId));
    }

    @PutMapping("/issues/{issueId}")
    @ApiOperation("更新问题处理状态和备注")
    public ResultVO<Boolean> updateIssue(@RequestParam String aggregatorId,
                                         @PathVariable long issueId,
                                         @RequestParam String status,
                                         @RequestParam(required = false) String remark) {
        denyEntManage();
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        managementService.updateIssue(scopedAggregator, issueId, status, remark,
                AuthContext.get() == null ? null : AuthContext.get().getUserId(),
                AuthContext.get() == null ? null : AuthContext.get().getDisplayName());
        return ResultVO.success(true);
    }

    @GetMapping("/snapshots")
    @ApiOperation("查询参与范围快照版本")
    public ResultVO<List<Map<String, Object>>> snapshots(@RequestParam String aggregatorId) {
        return ResultVO.success(managementService.snapshots(scopeService.resolveAggregatorId(aggregatorId)));
    }

    @GetMapping("/snapshots/{snapshotId}")
    @ApiOperation("查询参与范围快照详情")
    public ResultVO<Map<String, Object>> snapshotDetail(@RequestParam String aggregatorId,
                                                        @PathVariable long snapshotId) {
        return ResultVO.success(managementService.snapshotDetail(scopeService.resolveAggregatorId(aggregatorId), snapshotId));
    }

    @PutMapping("/snapshots/{snapshotId}")
    @ApiOperation("维护参与范围快照有效期")
    public ResultVO<Boolean> updateSnapshot(@RequestParam String aggregatorId,
                                            @PathVariable long snapshotId,
                                            @RequestParam(required = false) String snapshotName,
                                            @RequestParam String effectiveStart,
                                            @RequestParam(required = false) String effectiveEnd,
                                            @RequestParam String status) {
        denyEntManage();
        managementService.updateSnapshot(scopeService.resolveAggregatorId(aggregatorId), snapshotId, snapshotName,
                effectiveStart, effectiveEnd, status);
        return ResultVO.success(true);
    }

    @GetMapping("/report")
    @ApiOperation("同步导出 31 天内的多 Sheet 核查报告")
    public void report(@RequestParam String aggregatorId,
                       @RequestParam String startDate,
                       @RequestParam String endDate,
                       @RequestParam(required = false) String channelNo,
                       @RequestParam(required = false) Long periodId,
                       @RequestParam String resourceTypeId,
                       HttpServletResponse response) throws IOException {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, startDate, endDate, channelNo, periodId, resourceTypeId);
        byte[] bytes = reportService.report(scopedAggregator, startDate, endDate, channelNo, periodId, resourceTypeId);
        downloadHeaders(response, "电网上送核查报告-" + startDate + "-" + endDate + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.getOutputStream().write(bytes);
    }

    @PostMapping("/export-tasks")
    @ApiOperation("创建供暖季异步核查报告")
    public ResultVO<Map<String, Object>> createExportTask(@RequestParam String aggregatorId,
                                                          @RequestParam String startDate,
                                                          @RequestParam String endDate,
                                                          @RequestParam(required = false) String channelNo,
                                                          @RequestParam(required = false) Long periodId,
                                                          @RequestParam String resourceTypeId) {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, startDate, endDate, channelNo, periodId, resourceTypeId);
        return ResultVO.success(reportService.createTask(scopedAggregator, startDate, endDate, channelNo, periodId, resourceTypeId,
                AuthContext.get() == null ? null : AuthContext.get().getUserId()));
    }

    @GetMapping("/export-tasks")
    @ApiOperation("查询异步导出任务")
    public ResultVO<List<Map<String, Object>>> exportTasks(@RequestParam String aggregatorId,
                                                           @RequestParam(required = false) Long periodId,
                                                           @RequestParam String resourceTypeId) {
        return ResultVO.success(reportService.tasks(scopeService.resolveAggregatorId(aggregatorId), periodId, resourceTypeId));
    }

    @GetMapping("/export-tasks/{taskNo}/download")
    @ApiOperation("下载异步导出文件")
    public void downloadExportTask(@RequestParam String aggregatorId,
                                   @PathVariable String taskNo,
                                   HttpServletResponse response) throws IOException {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        Map<String, Object> task = reportService.task(taskNo, scopedAggregator);
        Path file = reportService.taskFile(taskNo, scopedAggregator);
        downloadHeaders(response, String.valueOf(task.get("fileName")),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        Files.copy(file, response.getOutputStream());
    }

    @GetMapping("/export")
    @ApiOperation("导出单日上送核查数据")
    public void export(@RequestParam String aggregatorId,
                       @RequestParam String date,
                       @RequestParam(defaultValue = "TOTAL") String type,
                       @RequestParam(required = false) String channelNo,
                       @RequestParam(required = false) Long periodId,
                       @RequestParam String resourceTypeId,
                       HttpServletResponse response) throws IOException {
        String scopedAggregator = scopeService.resolveAggregatorId(aggregatorId);
        channelNo = periodChannel(scopedAggregator, date, date, channelNo, periodId, resourceTypeId);
        List<Map<String, Object>> rows = service.daily(scopedAggregator, date, type, channelNo, resourceTypeId);
        StringBuilder csv = new StringBuilder("时间,单体编码,设备编码,值,状态\n");
        for (Map<String, Object> row : rows) {
            csv.append(csv(row.get("time"))).append(',')
                    .append(csv(row.get("singleCode"))).append(',')
                    .append(csv(row.get("deviceCode"))).append(',')
                    .append(csv(row.get("value"))).append(',')
                    .append(csv(row.get("status"))).append('\n');
        }
        String filename = URLEncoder.encode("电网上送核查-" + date + ".csv", StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=UTF-8''" + filename);
        response.getOutputStream().write(("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8));
    }

    private void denyEntManage() {
        if (scopeService.isEntCustomer(AuthContext.get())) {
            throw new BaseException(StatusCode.C.getCode(), "企业账号无权处理电网上送核查配置");
        }
    }

    private String periodChannel(String aggregatorId, String startDate, String endDate, String channelNo,
                                 Long periodId, String resourceTypeId) {
        if (periodId == null) {
            return channelNo;
        }
        Map<String, Object> period = managementService.period(aggregatorId, periodId);
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDate periodStart = LocalDate.parse(String.valueOf(period.get("startDate")).substring(0, 10));
        LocalDate periodEnd = LocalDate.parse(String.valueOf(period.get("endDate")).substring(0, 10));
        if (start.isBefore(periodStart) || end.isAfter(periodEnd)) {
            throw new IllegalArgumentException("查询日期必须位于所选电网上送周期内");
        }
        Object configured = period.get("channelNo");
        Object configuredResource = period.get("resourceTypeId");
        if (resourceTypeId != null && configuredResource != null
                && !resourceTypeId.equals(String.valueOf(configuredResource))) {
            throw new IllegalArgumentException("所选能源与电网上送周期不一致");
        }
        return configured == null ? channelNo : String.valueOf(configured);
    }

    private void downloadHeaders(HttpServletResponse response, String fileName, String contentType)
            throws IOException {
        String filename = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        response.setContentType(contentType);
        response.setHeader("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=UTF-8''" + filename);
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String raw = String.valueOf(value);
        String text = raw.replace("\"", "\"\"");
        return raw.contains(",") || raw.contains("\"") || raw.contains("\n")
                ? "\"" + text + "\"" : text;
    }
}
