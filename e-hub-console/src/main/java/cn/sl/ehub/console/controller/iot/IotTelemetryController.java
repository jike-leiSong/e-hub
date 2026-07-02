package cn.sl.ehub.console.controller.iot;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.dto.iot.*;
import cn.sl.ehub.service.service.IotTelemetryQueryService;
import cn.sl.ehub.service.service.IotTelemetryQueryService.IotTelemetryDataResult;
import cn.sl.ehub.service.service.IotTelemetryQueryService.IotTelemetryRawResult;
import cn.sl.ehub.service.service.IotTelemetryQueryService.IotTelemetryAggResult;
import cn.sl.ehub.common.utils.ExcelExportUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Api(tags = "IoT时序数据查询")
@RestController
@RequestMapping("/iot/telemetry")
public class IotTelemetryController {

    private final IotTelemetryQueryService telemetryQueryService;
    private final LoadAggregationScopeService loadScopeService;

    public IotTelemetryController(IotTelemetryQueryService telemetryQueryService,
                                  LoadAggregationScopeService loadScopeService) {
        this.telemetryQueryService = telemetryQueryService;
        this.loadScopeService = loadScopeService;
    }

    @GetMapping("/data")
    @ApiOperation("时序数据查询（支持分钟/聚合）")
    public ResultVO<PageResultVO<?>> queryData(
            @RequestParam(value = "entId", required = false) String entId,
            @RequestParam(value = "energyStationCode", required = false) String energyStationCode,
            @RequestParam(value = "deviceIds", required = false) List<Long> deviceIds,
            @RequestParam(value = "pointCodes", required = false) List<String> pointCodes,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "aggType", defaultValue = "minute") String aggType,
            @RequestParam(value = "aggFunc", defaultValue = "avg") String aggFunc,
            @RequestParam(value = "limit", defaultValue = "1000") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {

        IotTelemetryQueryReq req = new IotTelemetryQueryReq();
        applyScope(req, entId);
        req.setEnergyStationCode(energyStationCode);
        req.setDeviceIds(deviceIds);
        req.setPointCodes(pointCodes);
        req.setStartTime(startTime);
        req.setEndTime(endTime);
        req.setAggType(aggType);
        req.setAggFunc(aggFunc);
        req.setLimit(limit);
        req.setOffset(offset);

        List<?> list;
        Long total;
        if ("minute".equals(aggType)) {
            IotTelemetryDataResult result = telemetryQueryService.queryData(req);
            list = result.getList();
            total = result.getTotal();
        } else {
            IotTelemetryAggResult result = telemetryQueryService.queryAgg(req);
            list = result.getList();
            total = result.getTotal();
        }

        PageResultVO<Object> page = new PageResultVO<>();
        page.setList(new ArrayList<>(list));
        page.setTotal(total.intValue());
        page.setPageIndex(1);
        page.setPageSize(limit);

        return ResultVO.success(page);
    }

    @GetMapping("/raw")
    @ApiOperation("原始明细查询（仅用于问题追溯）")
    public ResultVO<PageResultVO<IotTelemetryRawResp>> queryRaw(
            @RequestParam(value = "entId", required = false) String entId,
            @RequestParam(value = "energyStationCode", required = false) String energyStationCode,
            @RequestParam(value = "deviceId", required = false) Long deviceId,
            @RequestParam(value = "externalDeviceId", required = false) String externalDeviceId,
            @RequestParam(value = "externalMetric", required = false) String externalMetric,
            @RequestParam(value = "matchStatus", required = false) String matchStatus,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "limit", defaultValue = "100") Integer limit,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset) {

        IotTelemetryRawQueryReq req = new IotTelemetryRawQueryReq();
        applyScope(req, entId);
        req.setEnergyStationCode(energyStationCode);
        req.setDeviceId(deviceId);
        req.setExternalDeviceId(externalDeviceId);
        req.setExternalMetric(externalMetric);
        req.setMatchStatus(matchStatus);
        req.setStartTime(startTime);
        req.setEndTime(endTime);
        req.setLimit(limit);
        req.setOffset(offset);

        IotTelemetryRawResult result = telemetryQueryService.queryRaw(req);

        PageResultVO<IotTelemetryRawResp> page = new PageResultVO<>();
        page.setList(result.getList());
        page.setTotal(result.getTotal().intValue());
        page.setPageIndex(1);
        page.setPageSize(limit);

        return ResultVO.success(page);
    }

    @GetMapping("/export")
    @ApiOperation("时序数据导出 Excel")
    public void exportData(
            @RequestParam(value = "entId", required = false) String entId,
            @RequestParam(value = "energyStationCode", required = false) String energyStationCode,
            @RequestParam(value = "deviceIds", required = false) List<Long> deviceIds,
            @RequestParam(value = "pointCodes", required = false) List<String> pointCodes,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "aggType", defaultValue = "minute") String aggType,
            @RequestParam(value = "aggFunc", defaultValue = "avg") String aggFunc,
            @RequestParam(value = "limit", defaultValue = "10000") Integer limit,
            HttpServletResponse response) throws Exception {

        IotTelemetryQueryReq req = new IotTelemetryQueryReq();
        applyScope(req, entId);
        req.setEnergyStationCode(energyStationCode);
        req.setDeviceIds(deviceIds);
        req.setPointCodes(pointCodes);
        req.setStartTime(startTime);
        req.setEndTime(endTime);
        req.setAggType(aggType);
        req.setAggFunc(aggFunc);
        req.setLimit(limit);

        String fileName = "iot_telemetry_" + aggType + "_" + System.currentTimeMillis();
        String sheetName = aggType.equals("minute") ? "分钟数据" : "聚合数据";

        List<String> headers = buildHeaders(aggType);
        List<String> columns = buildColumns(aggType);

        if ("minute".equals(aggType)) {
            IotTelemetryDataResult result = telemetryQueryService.queryData(req);
            ExcelExportUtil.export(headers, columns, result.getList(), fileName, sheetName, response);
        } else {
            IotTelemetryAggResult result = telemetryQueryService.queryAgg(req);
            ExcelExportUtil.export(headers, columns, result.getList(), fileName, sheetName, response);
        }
    }

    @GetMapping("/device-summary")
    @ApiOperation("设备概览（每个设备测点的最新值、前一值、趋势）")
    public ResultVO<List<IotDeviceSummaryResp>> getDeviceSummary(
            @RequestParam(value = "entId", required = false) String entId,
            @RequestParam(value = "energyStationCode", required = false) String energyStationCode,
            @RequestParam(value = "deviceIds", required = false) List<Long> deviceIds,
            @RequestParam(value = "pointCodes", required = false) List<String> pointCodes,
            @RequestParam(value = "limit", defaultValue = "100") Integer limit) {

        IotDeviceSummaryReq req = new IotDeviceSummaryReq();
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(null, entId);
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
        req.setEnergyStationCode(energyStationCode);
        req.setDeviceIds(deviceIds);
        req.setPointCodes(pointCodes);
        req.setLimit(limit);

        List<IotDeviceSummaryResp> data = telemetryQueryService.getDeviceSummary(req);
        return ResultVO.success(data);
    }

    private void applyScope(IotTelemetryQueryReq req, String entId) {
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(null, entId);
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
    }

    private void applyScope(IotTelemetryRawQueryReq req, String entId) {
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(null, entId);
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
    }

    private List<String> buildHeaders(String aggType) {
        if ("minute".equals(aggType)) {
            return Arrays.asList("设备ID", "设备编码", "设备名称", "测点编码", "测点名称",
                    "单位", "数据时间", "数据值", "数据质量");
        } else {
            return Arrays.asList("设备ID", "设备编码", "设备名称", "测点编码", "测点名称",
                    "单位", "区间开始", "区间结束", "平均值", "最大值", "最小值", "求和值", "有效条数", "数据质量");
        }
    }

    private List<String> buildColumns(String aggType) {
        if ("minute".equals(aggType)) {
            return Arrays.asList("deviceId", "deviceCode", "deviceName", "pointCode",
                    "pointName", "unit", "dataTime", "value", "quality");
        } else {
            return Arrays.asList("deviceId", "deviceCode", "deviceName", "pointCode",
                    "pointName", "unit", "startTime", "endTime", "avgValue",
                    "maxValue", "minValue", "sumValue", "count", "quality");
        }
    }
}
