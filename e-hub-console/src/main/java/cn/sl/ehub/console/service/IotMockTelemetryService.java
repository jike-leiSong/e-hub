package cn.sl.ehub.console.service;

import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
import cn.sl.ehub.service.dto.iot.IotMockPowerDataReq;
import cn.sl.ehub.service.dto.iot.IotMockPowerDataResp;
import cn.sl.ehub.service.mapper.IotAccessAppMapper;
import cn.sl.ehub.service.mapper.IotDeviceMapper;
import cn.sl.ehub.service.mapper.IotDevicePointMapper;
import cn.sl.ehub.service.vo.IotAccessApp;
import cn.sl.ehub.service.vo.IotDevice;
import cn.sl.ehub.service.vo.IotDevicePoint;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模拟物联遥测数据推送服务
 * <p>
 * 从 iot_device + iot_device_point 查询真实设备数据，
 * 按正弦波模拟测点值，调用三方数据接入接口 POST /data-collector/thirdPart/data/receive/originData
 * 写入 iot_telemetry_minute 表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotMockTelemetryService {

    /** 三方数据接入端点（本机） */
    private static final String DEFAULT_INGEST_URL = "http://127.0.0.1:8080/data-collector/thirdPart/data/receive/originData";

    /** 默认 X-GW-AccessKey（从 iot_access_app 表查询） */
    private static final String HEADER_ACCESS_KEY = "X-GW-AccessKey";
    private static final String POWER_POINT_CODE = "P";
    private static final int DEFAULT_INTERVAL_SECONDS = 60;
    private static final int MAX_GENERATE_DAYS = 7;
    private static final int MAX_GENERATE_RECORDS = 100000;

    private final IotDeviceMapper iotDeviceMapper;
    private final IotDevicePointMapper iotDevicePointMapper;
    private final IotAccessAppMapper iotAccessAppMapper;
    private final LoadAggregationScopeService loadScopeService;

    @Value("${iot.mock.ingest-url:" + DEFAULT_INGEST_URL + "}")
    private String ingestUrl;

    /**
     * 推送模拟遥测数据
     *
     * @param aggregatorId       聚合商ID（可选，自动取数据范围）
     * @param entId             企业ID（可选，自动取数据范围）
     * @param days              推送天数
     * @param intervalSeconds   数据点间隔（秒），默认60（每分钟一个点）
     * @param maxPointsPerDevice 每个设备最多推送测点数（0表示全部）
     * @param accessKey         接入凭证（可选，自动从 iot_access_app 查）
     * @param hourOffset        数据时间相对当前时间的偏移小时数（负数往前推）
     * @return 推送结果摘要
     */
    public String pushMockTelemetry(String aggregatorId,
                                    String entId,
                                    int days,
                                    int intervalSeconds,
                                    int maxPointsPerDevice,
                                    String accessKey,
                                    int hourOffset) {
        // 1. 解析数据范围
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(aggregatorId, entId);
        String actualAggregatorId = StringUtils.defaultIfBlank(aggregatorId, scope.getAggregatorId());
        String actualEntId = StringUtils.defaultIfBlank(entId, scope.getEntId());

        if (StringUtils.isBlank(actualEntId)) {
            log.warn("[MockTelemetry] 未指定 entId，将推送聚合商[{}]下所有设备的数据", actualAggregatorId);
        }

        // 2. 查询设备
        List<IotDevice> devices = listDevices(actualAggregatorId, actualEntId);
        if (CollectionUtils.isEmpty(devices)) {
            throw new BaseException(400, "没有找到任何设备，请先导入设备数据");
        }

        // 3. 查询每个设备的测点，并按 deviceId 分组
        Map<Long, List<IotDevicePoint>> devicePointMap = listDevicePoints(devices);
        if (devicePointMap.isEmpty()) {
            throw new BaseException(400, "所选设备没有任何测点，请先在设备管理中添加测点");
        }

        // 4. 解析 accessKey：优先用入参，否则按聚合商匹配启用凭证
        String actualAccessKey = StringUtils.defaultIfBlank(
                accessKey,
                resolveAccessApp(StringUtils.defaultIfBlank(
                        actualAggregatorId,
                        CollectionUtils.isNotEmpty(devices) ? devices.get(0).getAggregatorId() : null
                )).accessKey
        );

        // 5. 生成数据点并推送
        LocalDateTime baseTime = LocalDateTime.now().plusHours(hourOffset);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        int totalDevices = 0;
        int totalPoints = 0;
        int totalPushSuccess = 0;
        int totalPushFail = 0;

        for (IotDevice device : devices) {
            List<IotDevicePoint> points = devicePointMap.get(device.getId());
            if (CollectionUtils.isEmpty(points)) {
                continue;
            }

            // 限制每个设备的测点数量
            if (maxPointsPerDevice > 0 && points.size() > maxPointsPerDevice) {
                points = points.subList(0, maxPointsPerDevice);
            }

            // 计算该设备的基准功率（用于正弦波参数），默认 200kW
            BigDecimal basePower = resolveBasePower(device);
            // 生成24小时数据（按 intervalSeconds 切分）
            List<TelemetryRecord> records = generateTelemetryRecords(
                    device, points, baseTime, days, intervalSeconds, basePower, dtf);

            if (records.isEmpty()) {
                continue;
            }

            // 批量推送（按数据时间聚合）
            PushResult pushResult = pushBatch(device, records, actualAccessKey);
            totalDevices++;
            totalPoints += points.size();
            totalPushSuccess += pushResult.success;
            totalPushFail += pushResult.fail;
        }

        return String.format(
                "推送完成：设备=%d，测点=%d，数据点=%d（成功=%d，失败=%d），时间范围=%s 至 %s",
                totalDevices, totalPoints, totalPushSuccess + totalPushFail,
                totalPushSuccess, totalPushFail,
                baseTime.format(dtf),
                baseTime.plusDays(days).format(dtf)
        );
    }

    /**
     * 手动生成现有设备 P 功率物联数据。
     */
    public IotMockPowerDataResp generatePowerData(IotMockPowerDataReq req) {
        if (req == null) {
            req = new IotMockPowerDataReq();
        }
        LoadAggregationScopeService.Scope scope = loadScopeService.resolveQueryScope(req.getAggregatorId(), req.getEntId());
        String actualAggregatorId = StringUtils.defaultIfBlank(req.getAggregatorId(), scope.getAggregatorId());
        String actualEntId = StringUtils.defaultIfBlank(req.getEntId(), scope.getEntId());

        int intervalSeconds = req.getIntervalSeconds() == null ? DEFAULT_INTERVAL_SECONDS : req.getIntervalSeconds();
        if (intervalSeconds < DEFAULT_INTERVAL_SECONDS) {
            throw new BaseException(400, "采样间隔不能小于60秒");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = parseMockTime(req.getStartTime(), LocalDate.now().atStartOfDay(), "开始时间");
        LocalDateTime endTime = parseMockTime(req.getEndTime(), now, "结束时间");
        if (endTime.isBefore(startTime)) {
            throw new BaseException(400, "结束时间不能早于开始时间");
        }
        long rangeSeconds = Duration.between(startTime, endTime).getSeconds();
        if (rangeSeconds > MAX_GENERATE_DAYS * 24L * 60 * 60) {
            throw new BaseException(400, "单次最多生成" + MAX_GENERATE_DAYS + "天数据");
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        IotMockPowerDataResp resp = new IotMockPowerDataResp();
        resp.setStartTime(startTime.format(dtf));
        resp.setEndTime(endTime.format(dtf));

        List<IotDevice> devices = listPowerDevices(actualAggregatorId, actualEntId,
                req.getEnergyStationCode(), req.getDeviceIds());
        if (CollectionUtils.isEmpty(devices)) {
            throw new BaseException(400, "当前条件下没有找到可生成数据的设备");
        }

        Map<Long, IotDevicePoint> powerPointMap = listPowerPointMap(devices);
        if (powerPointMap.isEmpty()) {
            throw new BaseException(400, "当前设备没有配置功率P测点，请先在设备点表中维护P点");
        }

        Map<String, AccessApp> accessAppMap = listAccessAppsBySource(actualAggregatorId);
        if (accessAppMap.isEmpty()) {
            throw new BaseException(400, "未找到有效的物联接入凭证，请先维护 iot_access_app");
        }
        Set<String> duplicateDeviceMappingKeys = findDuplicateDeviceMappingKeys(actualAggregatorId);

        List<PowerDataTask> tasks = new ArrayList<>();
        for (IotDevice device : devices) {
            IotDevicePoint point = powerPointMap.get(device.getId());
            if (point == null) {
                resp.setSkippedPointCount(resp.getSkippedPointCount() + 1);
                addWarning(resp, "设备[" + device.getDeviceName() + "]未配置P测点");
                continue;
            }
            if (StringUtils.isBlank(device.getThirdPartyApi()) || StringUtils.isBlank(device.getThirdPartyCode())) {
                resp.setSkippedDeviceCount(resp.getSkippedDeviceCount() + 1);
                addWarning(resp, "设备[" + device.getDeviceName() + "]缺少第三方API或第三方标识");
                continue;
            }
            if (duplicateDeviceMappingKeys.contains(buildDeviceMappingKey(device))) {
                resp.setSkippedDeviceCount(resp.getSkippedDeviceCount() + 1);
                addWarning(resp, "设备[" + device.getDeviceName() + "]三方设备标识重复："
                        + device.getThirdPartyApi() + "/" + device.getThirdPartyCode());
                continue;
            }
            if (StringUtils.isBlank(point.getThirdPartyCode())) {
                resp.setSkippedPointCount(resp.getSkippedPointCount() + 1);
                addWarning(resp, "设备[" + device.getDeviceName() + "]的P测点缺少第三方标识");
                continue;
            }
            AccessApp accessApp = accessAppMap.get(StringUtils.trim(device.getThirdPartyApi()));
            if (accessApp == null) {
                resp.setSkippedDeviceCount(resp.getSkippedDeviceCount() + 1);
                addWarning(resp, "设备[" + device.getDeviceName() + "]第三方API[" + device.getThirdPartyApi() + "]没有启用的接入凭证");
                continue;
            }
            tasks.add(new PowerDataTask(device, point, accessApp));
        }

        if (tasks.isEmpty()) {
            resp.setMessage("没有可生成的数据，请检查设备和P测点第三方映射");
            return resp;
        }

        long slots = countSlots(startTime, endTime, intervalSeconds);
        long estimate = slots * tasks.size();
        if (estimate > MAX_GENERATE_RECORDS) {
            throw new BaseException(400, "本次预计生成" + estimate + "条，超过上限" + MAX_GENERATE_RECORDS + "条，请缩小时间范围或设备数量");
        }

        Map<String, List<TelemetryRecord>> recordsByAccessKey = new LinkedHashMap<>();
        for (PowerDataTask task : tasks) {
            List<TelemetryRecord> records = generatePowerTelemetryRecords(
                    task.device, task.point, startTime, endTime, intervalSeconds, resolveBasePower(task.device), dtf);
            if (records.isEmpty()) {
                continue;
            }
            List<TelemetryRecord> accessRecords = recordsByAccessKey.get(task.accessApp.accessKey);
            if (accessRecords == null) {
                accessRecords = new ArrayList<>();
                recordsByAccessKey.put(task.accessApp.accessKey, accessRecords);
            }
            accessRecords.addAll(records);
        }

        int success = 0;
        int fail = 0;
        for (Map.Entry<String, List<TelemetryRecord>> entry : recordsByAccessKey.entrySet()) {
            PushResult pushResult = pushRecords(entry.getValue(), entry.getKey(), "P功率模拟数据");
            success += pushResult.success;
            fail += pushResult.fail;
        }

        resp.setDeviceCount(tasks.size());
        resp.setPointCount(tasks.size());
        resp.setSuccess(success);
        resp.setFail(fail);
        resp.setTotal(success + fail);
        resp.setMessage(String.format("生成完成：设备=%d，P测点=%d，数据点=%d（成功=%d，失败=%d）",
                resp.getDeviceCount(), resp.getPointCount(), resp.getTotal(), success, fail));
        return resp;
    }

    // ── 私有方法 ────────────────────────────────────────────────────────────────

    private List<IotDevice> listDevices(String aggregatorId, String entId) {
        Example ex = new Example(IotDevice.class);
        Example.Criteria criteria = ex.createCriteria()
                .andEqualTo("deleted", 0)
                .andEqualTo("status", 1)
                .andEqualTo("assetStatus", 1);
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", aggregatorId);
        }
        if (StringUtils.isNotBlank(entId)) {
            criteria.andEqualTo("entId", entId);
        }
        return iotDeviceMapper.selectByExample(ex);
    }

    private Map<Long, List<IotDevicePoint>> listDevicePoints(List<IotDevice> devices) {
        if (CollectionUtils.isEmpty(devices)) {
            return new ConcurrentHashMap<>();
        }
        List<Long> deviceIds = devices.stream().map(IotDevice::getId).collect(Collectors.toList());
        Example ex = new Example(IotDevicePoint.class);
        ex.createCriteria()
                .andIn("deviceId", deviceIds)
                .andEqualTo("deleted", 0)
                .andEqualTo("status", 1);
        List<IotDevicePoint> allPoints = iotDevicePointMapper.selectByExample(ex);
        return allPoints.stream().collect(Collectors.groupingBy(IotDevicePoint::getDeviceId));
    }

    private List<IotDevice> listPowerDevices(String aggregatorId, String entId,
                                             String energyStationCode, List<Long> deviceIds) {
        Example ex = new Example(IotDevice.class);
        Example.Criteria criteria = ex.createCriteria()
                .andEqualTo("deleted", 0)
                .andEqualTo("status", 1)
                .andEqualTo("assetStatus", 1);
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", StringUtils.trim(aggregatorId));
        }
        if (StringUtils.isNotBlank(entId)) {
            criteria.andEqualTo("entId", StringUtils.trim(entId));
        }
        if (StringUtils.isNotBlank(energyStationCode)) {
            criteria.andEqualTo("projectId", StringUtils.trim(energyStationCode));
        }
        if (CollectionUtils.isNotEmpty(deviceIds)) {
            criteria.andIn("id", deviceIds);
        }
        ex.orderBy("id").asc();
        return iotDeviceMapper.selectByExample(ex);
    }

    private Set<String> findDuplicateDeviceMappingKeys(String aggregatorId) {
        Set<String> duplicates = new HashSet<>();
        Example ex = new Example(IotDevice.class);
        Example.Criteria criteria = ex.createCriteria()
                .andEqualTo("deleted", 0)
                .andEqualTo("status", 1)
                .andEqualTo("assetStatus", 1);
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", StringUtils.trim(aggregatorId));
        }
        List<IotDevice> devices = iotDeviceMapper.selectByExample(ex);
        Map<String, Integer> counts = new HashMap<>();
        for (IotDevice device : devices) {
            String key = buildDeviceMappingKey(device);
            if (key == null) {
                continue;
            }
            Integer count = counts.get(key);
            counts.put(key, count == null ? 1 : count + 1);
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > 1) {
                duplicates.add(entry.getKey());
            }
        }
        return duplicates;
    }

    private String buildDeviceMappingKey(IotDevice device) {
        if (device == null || StringUtils.isBlank(device.getThirdPartyApi())
                || StringUtils.isBlank(device.getThirdPartyCode())) {
            return null;
        }
        return StringUtils.trim(device.getThirdPartyApi()) + "\n" + StringUtils.trim(device.getThirdPartyCode());
    }

    private Map<Long, IotDevicePoint> listPowerPointMap(List<IotDevice> devices) {
        Map<Long, IotDevicePoint> result = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(devices)) {
            return result;
        }
        List<Long> deviceIds = devices.stream().map(IotDevice::getId).collect(Collectors.toList());
        Example ex = new Example(IotDevicePoint.class);
        ex.createCriteria()
                .andIn("deviceId", deviceIds)
                .andEqualTo("propertyCode", POWER_POINT_CODE)
                .andEqualTo("deleted", 0)
                .andEqualTo("status", 1);
        ex.orderBy("deviceId").asc();
        List<IotDevicePoint> points = iotDevicePointMapper.selectByExample(ex);
        for (IotDevicePoint point : points) {
            if (point.getDeviceId() != null && !result.containsKey(point.getDeviceId())) {
                result.put(point.getDeviceId(), point);
            }
        }
        return result;
    }

    private static class AccessApp {
        final String sourceCode;
        final String accessKey;

        AccessApp(String sourceCode, String accessKey) {
            this.sourceCode = sourceCode;
            this.accessKey = accessKey;
        }
    }

    private AccessApp resolveAccessApp(String aggregatorId) {
        Example ex = new Example(IotAccessApp.class);
        Example.Criteria criteria = ex.createCriteria().andEqualTo("enabled", 1);
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", aggregatorId);
        }
        List<IotAccessApp> apps = iotAccessAppMapper.selectByExample(ex);
        if (CollectionUtils.isEmpty(apps) && StringUtils.isNotBlank(aggregatorId)) {
            ex = new Example(IotAccessApp.class);
            ex.createCriteria().andEqualTo("enabled", 1);
            apps = iotAccessAppMapper.selectByExample(ex);
        }
        if (CollectionUtils.isEmpty(apps)) {
            throw new BaseException(400, "未找到有效的接入凭证，请检查 iot_access_app 表，或传入 accessKey 参数");
        }
        IotAccessApp app = apps.get(0);
        return new AccessApp(app.getSourceCode(), app.getAccessKey());
    }

    private Map<String, AccessApp> listAccessAppsBySource(String aggregatorId) {
        Map<String, AccessApp> result = new LinkedHashMap<>();
        appendAccessApps(result, listEnabledAccessApps(aggregatorId));
        if (StringUtils.isNotBlank(aggregatorId)) {
            appendAccessApps(result, listEnabledAccessApps(null));
        }
        return result;
    }

    private void appendAccessApps(Map<String, AccessApp> result, List<IotAccessApp> apps) {
        for (IotAccessApp app : apps) {
            String sourceCode = StringUtils.trimToNull(app.getSourceCode());
            String accessKey = StringUtils.trimToNull(app.getAccessKey());
            if (sourceCode == null || accessKey == null || result.containsKey(sourceCode)) {
                continue;
            }
            result.put(sourceCode, new AccessApp(sourceCode, accessKey));
        }
    }

    private List<IotAccessApp> listEnabledAccessApps(String aggregatorId) {
        Example ex = new Example(IotAccessApp.class);
        Example.Criteria criteria = ex.createCriteria().andEqualTo("enabled", 1);
        if (StringUtils.isNotBlank(aggregatorId)) {
            criteria.andEqualTo("aggregatorId", StringUtils.trim(aggregatorId));
        }
        return iotAccessAppMapper.selectByExample(ex);
    }

    /**
     * 生成模拟遥测数据记录（正弦波 + 高斯噪声）
     * <p>
     * 典型日负荷曲线特征：
     * 谷时（00:00-07:00）：低功率
     * 峰时（08:00-11:00, 18:00-21:00）：高功率
     * 平时（其余时段）：中等功率
     */
    private List<TelemetryRecord> generateTelemetryRecords(
            IotDevice device,
            List<IotDevicePoint> points,
            LocalDateTime baseTime,
            int days,
            int intervalSeconds,
            BigDecimal basePower,
            DateTimeFormatter dtf) {

        List<TelemetryRecord> records = new ArrayList<>();
        long totalMinutes = days * 24L * 60;
        long step = intervalSeconds / 60; // 折算成分钟步长，最小1分钟

        for (long minuteIdx = 0; minuteIdx < totalMinutes; minuteIdx += Math.max(step, 1)) {
            LocalDateTime dataTime = baseTime.plusMinutes(minuteIdx);
            int hour = dataTime.getHour();
            int minute = dataTime.getMinute();

            for (IotDevicePoint point : points) {
                String externalDeviceId = StringUtils.trimToNull(device.getThirdPartyCode());
                String externalMetric = StringUtils.trimToNull(point.getThirdPartyCode());
                if (externalDeviceId == null || externalMetric == null) {
                    continue;
                }
                BigDecimal value = generateValue(basePower, hour, minute, minuteIdx, point);
                records.add(new TelemetryRecord(
                        dataTime.format(dtf),
                        externalDeviceId,
                        externalMetric,
                        value.toPlainString()
                ));
            }
        }
        return records;
    }

    private List<TelemetryRecord> generatePowerTelemetryRecords(IotDevice device,
                                                               IotDevicePoint point,
                                                               LocalDateTime startTime,
                                                               LocalDateTime endTime,
                                                               int intervalSeconds,
                                                               BigDecimal basePower,
                                                               DateTimeFormatter dtf) {
        List<TelemetryRecord> records = new ArrayList<>();
        String externalDeviceId = StringUtils.trimToNull(device.getThirdPartyCode());
        String externalMetric = StringUtils.trimToNull(point.getThirdPartyCode());
        if (externalDeviceId == null || externalMetric == null) {
            return records;
        }
        LocalDateTime cursor = startTime;
        while (!cursor.isAfter(endTime)) {
            long minuteIdx = Duration.between(startTime, cursor).toMinutes();
            BigDecimal value = generateValue(basePower, cursor.getHour(), cursor.getMinute(), minuteIdx, point);
            records.add(new TelemetryRecord(
                    cursor.format(dtf),
                    externalDeviceId,
                    externalMetric,
                    value.toPlainString()
            ));
            cursor = cursor.plusSeconds(intervalSeconds);
        }
        return records;
    }

    private LocalDateTime parseMockTime(String value, LocalDateTime defaultValue, String fieldName) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        String text = StringUtils.trim(value);
        String[] patterns = new String[]{
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-M-d H:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-M-d H:mm"
        };
        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
            } catch (RuntimeException ignored) {
                // try next pattern
            }
        }
        throw new BaseException(400, fieldName + "格式错误，请使用 yyyy-MM-dd HH:mm:ss");
    }

    private long countSlots(LocalDateTime startTime, LocalDateTime endTime, int intervalSeconds) {
        long seconds = Duration.between(startTime, endTime).getSeconds();
        return seconds / intervalSeconds + 1;
    }

    private void addWarning(IotMockPowerDataResp resp, String warning) {
        if (resp.getWarnings().size() < 20) {
            resp.getWarnings().add(warning);
        }
    }

    /**
     * 根据时段生成模拟值
     * 策略：基于设备基准功率，按24小时正弦波 + 随机噪声生成
     */
    private BigDecimal generateValue(BigDecimal basePower, int hour, int minute, long minuteIdx, IotDevicePoint point) {
        // 以分钟为单位的角度（周期 = 1440 分钟 = 1天）
        double angle = 2 * Math.PI * minuteIdx / 1440.0;

        // 时段系数
        double periodFactor;
        if (hour >= 0 && hour < 7) {
            // 谷时：基准的 0.4~0.6
            periodFactor = 0.5;
        } else if (hour >= 7 && hour < 11) {
            // 早峰：基准的 0.9~1.1
            periodFactor = 1.0;
        } else if (hour >= 11 && hour < 14) {
            // 平时：基准的 0.7~0.8
            periodFactor = 0.75;
        } else if (hour >= 14 && hour < 18) {
            // 下午平：基准的 0.8~0.9
            periodFactor = 0.85;
        } else if (hour >= 18 && hour < 21) {
            // 晚峰：基准的 1.0~1.2
            periodFactor = 1.1;
        } else {
            // 夜平时段：基准的 0.6~0.7
            periodFactor = 0.65;
        }

        // 正弦波动 ±10%
        double sineWave = 1.0 + 0.1 * Math.sin(angle);
        // 高斯噪声 ±2%
        double noise = 1.0 + (Math.random() - 0.5) * 0.04;

        double raw = basePower.doubleValue() * periodFactor * sineWave * noise;
        return BigDecimal.valueOf(raw).setScale(4, RoundingMode.HALF_UP);
    }

    /** 解析设备基准功率，从设备备注或型号中尝试解析，否则默认 200kW */
    private BigDecimal resolveBasePower(IotDevice device) {
        String text = StringUtils.defaultIfBlank(device.getRemark(), device.getModel());
        if (StringUtils.isNotBlank(text)) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:kW|KW|千瓦)?").matcher(text);
            if (m.find()) {
                return new BigDecimal(m.group(1));
            }
        }
        return new BigDecimal("200");
    }

    /**
     * 批量推送数据
     * 策略：每个数据时间点打包成一批，每批包含该时刻所有设备所有测点
     */
    private PushResult pushBatch(IotDevice device,
                                List<TelemetryRecord> records,
                                String accessKey) {
        return pushRecords(records, accessKey, device == null ? "unknown" : device.getDeviceCode());
    }

    private PushResult pushRecords(List<TelemetryRecord> records, String accessKey, String context) {
        PushResult result = new PushResult();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }

        // 按 dataTime 分组
        Map<String, List<TelemetryRecord>> byTime = records.stream()
                .collect(Collectors.groupingBy(r -> r.dataTime));

        for (Map.Entry<String, List<TelemetryRecord>> entry : byTime.entrySet()) {
            // 构造请求体
            IotOriginDataPushReq req = new IotOriginDataPushReq();
            req.setDataList(entry.getValue());

            String json = JSON.toJSONString(req);
            String resp = httpPost(ingestUrl, json, accessKey);

            if (StringUtils.isNotBlank(resp)) {
                try {
                    JSONObject jsonResp = JSON.parseObject(resp);
                    Integer code = jsonResp.getInteger("code");
                    if (code != null && code == 200) {
                        JSONObject data = jsonResp.getJSONObject("data");
                        if (data == null) {
                            result.success += entry.getValue().size();
                        } else {
                            Integer success = data.getInteger("success");
                            Integer fail = data.getInteger("fail");
                            result.success += success == null ? 0 : success;
                            result.fail += fail == null ? 0 : fail;
                        }
                    } else {
                        result.fail += entry.getValue().size();
                        log.warn("推送失败 [context={}, time={}]: {}", context, entry.getKey(), resp);
                    }
                } catch (Exception e) {
                    result.fail += entry.getValue().size();
                    log.warn("解析响应异常 [context={}, time={}]: {}", context, entry.getKey(), resp);
                }
            } else {
                result.fail += entry.getValue().size();
            }
        }
        return result;
    }

    private String httpPost(String url, String body, String accessKey) {
        try {
            HttpURLConnectionTool conn = new HttpURLConnectionTool(url);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty(HEADER_ACCESS_KEY, accessKey);
            conn.setDoOutput(true);
            conn.getOutputStream().write(body.getBytes("UTF-8"));
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            return conn.readResponse();
        } catch (Exception e) {
            log.error("HTTP POST 请求失败: url={}, error={}", url, e.getMessage());
            return null;
        }
    }

    // ── 内部类 ──────────────────────────────────────────────────────────────────

    private static class PowerDataTask {
        final IotDevice device;
        final IotDevicePoint point;
        final AccessApp accessApp;

        PowerDataTask(IotDevice device, IotDevicePoint point, AccessApp accessApp) {
            this.device = device;
            this.point = point;
            this.accessApp = accessApp;
        }
    }

    private static class TelemetryRecord {
        private final String dataTime;
        private final String deviceId;
        private final String metric;
        private final String value;

        TelemetryRecord(String dataTime, String deviceId, String metric, String value) {
            this.dataTime = dataTime;
            this.deviceId = deviceId;
            this.metric = metric;
            this.value = value;
        }

        public String getDataTime() {
            return dataTime;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getMetric() {
            return metric;
        }

        public String getValue() {
            return value;
        }
    }

    private static class IotOriginDataPushReq {
        private List<TelemetryRecord> dataList;

        public List<TelemetryRecord> getDataList() { return dataList; }
        public void setDataList(List<TelemetryRecord> dataList) { this.dataList = dataList; }
    }

    private static class PushResult {
        int success;
        int fail;
    }

    /**
     * 简化版 HttpURLConnection 封装
     */
    private static class HttpURLConnectionTool {
        private final HttpURLConnection conn;

        HttpURLConnectionTool(String urlStr) throws java.net.MalformedURLException, java.net.ProtocolException {
            try {
                java.net.URL url = new java.net.URL(urlStr);
                this.conn = (java.net.HttpURLConnection) url.openConnection();
                this.conn.setConnectTimeout(15000);
                this.conn.setReadTimeout(60000);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }

        void setRequestMethod(String method) {
            try {
                this.conn.setRequestMethod(method);
            } catch (java.net.ProtocolException e) {
                throw new RuntimeException(e);
            }
        }

        void setRequestProperty(String key, String value) {
            this.conn.setRequestProperty(key, value);
        }

        void setDoOutput(boolean b) {
            this.conn.setDoOutput(b);
        }

        java.io.OutputStream getOutputStream() throws java.io.IOException {
            return this.conn.getOutputStream();
        }

        String readResponse() throws java.io.IOException {
            int code = this.conn.getResponseCode();
            java.io.InputStream is = (code == 200) ? this.conn.getInputStream() : this.conn.getErrorStream();
            if (is == null) return "";
            java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            this.conn.disconnect();
            return sb.toString();
        }
    }
}
