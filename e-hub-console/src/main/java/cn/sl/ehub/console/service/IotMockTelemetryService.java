package cn.sl.ehub.console.service;

import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.console.auth.LoadAggregationScopeService;
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
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private static final String INGEST_URL = "http://127.0.0.1:8080/data-collector/thirdPart/data/receive/originData";

    /** 默认 X-GW-AccessKey（从 iot_access_app 表查询） */
    private static final String HEADER_ACCESS_KEY = "X-GW-AccessKey";

    private final IotDeviceMapper iotDeviceMapper;
    private final IotDevicePointMapper iotDevicePointMapper;
    private final IotAccessAppMapper iotAccessAppMapper;
    private final LoadAggregationScopeService loadScopeService;


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

    private static class AccessApp {
        final String accessKey;
        AccessApp(String accessKey) {
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
        return new AccessApp(apps.get(0).getAccessKey());
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
                BigDecimal value = generateValue(basePower, hour, minute, minuteIdx, point);
                records.add(new TelemetryRecord(
                        dataTime.format(dtf),
                        device.getId().toString(),
                        point.getPropertyCode(),
                        value.toPlainString()
                ));
            }
        }
        return records;
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
        PushResult result = new PushResult();

        // 按 dataTime 分组
        Map<String, List<TelemetryRecord>> byTime = records.stream()
                .collect(Collectors.groupingBy(r -> r.dataTime));

        for (Map.Entry<String, List<TelemetryRecord>> entry : byTime.entrySet()) {
            // 构造请求体
            IotOriginDataPushReq req = new IotOriginDataPushReq();
            req.setDataList(entry.getValue());

            String json = JSON.toJSONString(req);
            String resp = httpPost(INGEST_URL, json, accessKey);

            if (StringUtils.isNotBlank(resp)) {
                try {
                    JSONObject jsonResp = JSON.parseObject(resp);
                    Integer code = jsonResp.getInteger("code");
                    if (code != null && code == 200) {
                        result.success += entry.getValue().size();
                    } else {
                        result.fail += entry.getValue().size();
                        log.warn("推送失败 [device={}, time={}]: {}", device.getDeviceCode(), entry.getKey(), resp);
                    }
                } catch (Exception e) {
                    result.fail += entry.getValue().size();
                    log.warn("解析响应异常 [device={}, time={}]: {}", device.getDeviceCode(), entry.getKey(), resp);
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

    private static class TelemetryRecord {
        String dataTime;
        String deviceId;
        String metric;
        String value;

        TelemetryRecord(String dataTime, String deviceId, String metric, String value) {
            this.dataTime = dataTime;
            this.deviceId = deviceId;
            this.metric = metric;
            this.value = value;
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
