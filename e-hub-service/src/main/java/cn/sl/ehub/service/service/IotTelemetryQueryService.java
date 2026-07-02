package cn.sl.ehub.service.service;

import cn.sl.ehub.service.dto.iot.*;
import cn.sl.ehub.service.mapper.IotTelemetryQueryMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class IotTelemetryQueryService {

    private static final int MAX_LIMIT = 10000;
    private static final int DEFAULT_LIMIT = 1000;
    private static final int DEFAULT_RAW_LIMIT = 100;

    private final IotTelemetryQueryMapper mapper;
    private final IotBusinessScopeResolver businessScopeResolver;

    public IotTelemetryQueryService(IotTelemetryQueryMapper mapper,
                                    IotBusinessScopeResolver businessScopeResolver) {
        this.mapper = mapper;
        this.businessScopeResolver = businessScopeResolver;
    }

    public IotTelemetryDataResult queryData(IotTelemetryQueryReq req) {
        applyDefaults(req);
        validateTimeRange(req);
        applyBusinessScope(req);

        IotTelemetryDataResult result = new IotTelemetryDataResult();

        List<IotTelemetryDataResp> list = mapper.selectMinuteData(req);
        Long total = mapper.countMinuteData(req);

        result.setList(list);
        result.setTotal(total);
        result.setAggType(req.getAggType());
        result.setStartTime(req.getStartTime());
        result.setEndTime(req.getEndTime());
        return result;
    }

    public IotTelemetryAggResult queryAgg(IotTelemetryQueryReq req) {
        applyDefaults(req);
        validateTimeRange(req);
        applyBusinessScope(req);

        IotTelemetryAggResult result = new IotTelemetryAggResult();

        List<IotTelemetryAggResp> list = mapper.selectMinuteAgg(req);
        Long total = mapper.countMinuteAgg(req);

        result.setList(list);
        result.setTotal(total);
        result.setAggType(req.getAggType());
        result.setAggFunc(req.getAggFunc());
        result.setStartTime(req.getStartTime());
        result.setEndTime(req.getEndTime());
        return result;
    }

    public IotTelemetryRawResult queryRaw(IotTelemetryRawQueryReq req) {
        applyRawDefaults(req);
        applyBusinessScope(req);

        List<IotTelemetryRawResp> list = mapper.selectRawData(req);
        Long total = mapper.countRawData(req);

        IotTelemetryRawResult result = new IotTelemetryRawResult();
        result.setList(list);
        result.setTotal(total);
        return result;
    }

    public java.util.List<IotDeviceSummaryResp> getDeviceSummary(IotDeviceSummaryReq req) {
        if (req.getLimit() == null || req.getLimit() <= 0) {
            req.setLimit(100);
        }
        if (req.getLimit() > 500) {
            req.setLimit(500);
        }
        applyBusinessScope(req);

        java.util.List<IotDeviceSummaryPointResp> rows = mapper.selectDeviceSummary(req);
        return buildDeviceSummary(rows);
    }

    private java.util.List<IotDeviceSummaryResp> buildDeviceSummary(
            java.util.List<IotDeviceSummaryPointResp> rows) {
        java.util.LinkedHashMap<Long, IotDeviceSummaryResp> map =
                new java.util.LinkedHashMap<>();
        for (IotDeviceSummaryPointResp row : rows) {
            map.computeIfAbsent(row.getDeviceId(), id -> {
                IotDeviceSummaryResp resp = new IotDeviceSummaryResp();
                resp.setDeviceId(row.getDeviceId());
                resp.setDeviceCode(row.getDeviceCode());
                resp.setDeviceName(row.getDeviceName());
                resp.setEntId(row.getEntId());
                resp.setPoints(new java.util.ArrayList<>());
                return resp;
            }).getPoints().add(row);
        }
        return new java.util.ArrayList<>(map.values());
    }

    private void applyDefaults(IotTelemetryQueryReq req) {
        if (req.getLimit() == null || req.getLimit() <= 0) {
            req.setLimit(DEFAULT_LIMIT);
        }
        if (req.getLimit() > MAX_LIMIT) {
            req.setLimit(MAX_LIMIT);
        }
        if (StringUtils.isBlank(req.getAggType())) {
            req.setAggType("minute");
        } else if (!"minute".equals(req.getAggType()) && !"hour".equals(req.getAggType())
                && !"day".equals(req.getAggType())) {
            throw new IllegalArgumentException("不支持的数据粒度：" + req.getAggType());
        }
        if (StringUtils.isBlank(req.getAggFunc())) {
            req.setAggFunc("avg");
        } else if (!"avg".equals(req.getAggFunc()) && !"max".equals(req.getAggFunc())
                && !"min".equals(req.getAggFunc()) && !"sum".equals(req.getAggFunc())) {
            throw new IllegalArgumentException("不支持的聚合函数：" + req.getAggFunc());
        }
        // Normalize time strings to Date strings for MyBatis
        req.setStartTime(normalizeTime(req.getStartTime()));
        req.setEndTime(normalizeTime(req.getEndTime()));
    }

    private void applyRawDefaults(IotTelemetryRawQueryReq req) {
        if (req.getLimit() == null || req.getLimit() <= 0) {
            req.setLimit(DEFAULT_RAW_LIMIT);
        }
        if (req.getLimit() > 1000) {
            req.setLimit(1000);
        }
        req.setStartTime(normalizeTime(req.getStartTime()));
        req.setEndTime(normalizeTime(req.getEndTime()));
    }

    private void validateTimeRange(IotTelemetryQueryReq req) {
        if (StringUtils.isNotBlank(req.getStartTime()) && StringUtils.isNotBlank(req.getEndTime())) {
            Date start = parseDate(req.getStartTime());
            Date end = parseDate(req.getEndTime());
            if (start != null && end != null && start.after(end)) {
                throw new IllegalArgumentException("开始时间不能大于结束时间");
            }
        }
    }

    private void applyBusinessScope(IotTelemetryQueryReq req) {
        ResolvedIotScope scope = businessScopeResolver.resolve(
                req.getAggregatorId(),
                req.getEntId(),
                req.getEnergyStationCode(),
                req.getDeviceIds());
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
        req.setEmptyScope(scope.isEmptyScope());
        if (scope.isDeviceRestricted()) {
            req.setDeviceIds(scope.getDeviceIds());
        }
    }

    private void applyBusinessScope(IotTelemetryRawQueryReq req) {
        java.util.List<Long> requestedDeviceIds = new java.util.ArrayList<>();
        if (req.getDeviceId() != null) {
            requestedDeviceIds.add(req.getDeviceId());
        }
        if (req.getDeviceIds() != null) {
            requestedDeviceIds.addAll(req.getDeviceIds());
        }
        ResolvedIotScope scope = businessScopeResolver.resolve(
                req.getAggregatorId(),
                req.getEntId(),
                req.getEnergyStationCode(),
                requestedDeviceIds);
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
        req.setEmptyScope(scope.isEmptyScope());
        if (scope.isDeviceRestricted()) {
            req.setDeviceId(null);
            req.setDeviceIds(scope.getDeviceIds());
        }
    }

    private void applyBusinessScope(IotDeviceSummaryReq req) {
        ResolvedIotScope scope = businessScopeResolver.resolve(
                req.getAggregatorId(),
                req.getEntId(),
                req.getEnergyStationCode(),
                req.getDeviceIds());
        req.setAggregatorId(scope.getAggregatorId());
        req.setEntId(scope.getEntId());
        req.setEmptyScope(scope.isEmptyScope());
        if (scope.isDeviceRestricted()) {
            req.setDeviceIds(scope.getDeviceIds());
        }
    }

    private String normalizeTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String text = StringUtils.trim(value);
        // If already in full format, return as-is
        if (text.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            return text;
        }
        // Try to parse and reformat to standard format
        Date date = parseDate(text);
        if (date != null) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        }
        return text;
    }

    private Date parseDate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-M-d H:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-M-d H:mm",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                format.setLenient(false);
                return format.parse(StringUtils.trim(value));
            } catch (ParseException ignored) {
                // try next
            }
        }
        return null;
    }

    // --- 内部结果对象 ---

    public static class IotTelemetryDataResult {
        private List<IotTelemetryDataResp> list;
        private Long total;
        private String aggType;
        private String startTime;
        private String endTime;

        public List<IotTelemetryDataResp> getList() { return list; }
        public void setList(List<IotTelemetryDataResp> list) { this.list = list; }
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        public String getAggType() { return aggType; }
        public void setAggType(String aggType) { this.aggType = aggType; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }

    public static class IotTelemetryAggResult {
        private List<IotTelemetryAggResp> list;
        private Long total;
        private String aggType;
        private String aggFunc;
        private String startTime;
        private String endTime;

        public List<IotTelemetryAggResp> getList() { return list; }
        public void setList(List<IotTelemetryAggResp> list) { this.list = list; }
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        public String getAggType() { return aggType; }
        public void setAggType(String aggType) { this.aggType = aggType; }
        public String getAggFunc() { return aggFunc; }
        public void setAggFunc(String aggFunc) { this.aggFunc = aggFunc; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }

    public static class IotTelemetryRawResult {
        private List<IotTelemetryRawResp> list;
        private Long total;

        public List<IotTelemetryRawResp> getList() { return list; }
        public void setList(List<IotTelemetryRawResp> list) { this.list = list; }
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
    }
}
