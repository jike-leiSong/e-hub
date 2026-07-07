package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.service.resp.IndexOverviewTimeColorResp;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class LoadAggregationChartSupport {

    static final String POWER_POINT_CODE = "P";
    static final String NO_POWER_POINT_CODE = "Q";
    static final String CURRENT_A_POINT_CODE = "Ia";
    static final String CURRENT_B_POINT_CODE = "Ib";
    static final String CURRENT_C_POINT_CODE = "Ic";
    static final String ZERO_POWER_POINT_CODE = "Eptp";
    static final String STORAGE_RESOURCE_TYPE_ID = "27";

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter SHORT_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("H:mm:ss");

    private LoadAggregationChartSupport() {
    }

    static Date startOfDay(LocalDate date, int minuteOffset) {
        return Date.from(date.atStartOfDay().plusMinutes(minuteOffset).atZone(ZONE_ID).toInstant());
    }

    static Date endOfDayInclusive(LocalDate date) {
        return Date.from(date.plusDays(1).atStartOfDay().atZone(ZONE_ID).toInstant());
    }

    static List<String> buildDayMinuteDateTimeAxis(LocalDate date) {
        List<String> result = new ArrayList<>(1440);
        LocalDateTime current = date.atStartOfDay().plusMinutes(1);
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        while (!current.isAfter(end)) {
            result.add(DATE_TIME_FORMATTER.format(current));
            current = current.plusMinutes(1);
        }
        return result;
    }

    static List<String> buildQuarterDateTimeAxis(List<String> dateList) {
        if (dateList == null || dateList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(dateList.size() * 96);
        for (String dateValue : dateList) {
            LocalDate date = LocalDate.parse(dateValue, DATE_FORMATTER);
            LocalDateTime current = date.atStartOfDay().plusMinutes(15);
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            while (!current.isAfter(end)) {
                result.add(DATE_TIME_FORMATTER.format(current));
                current = current.plusMinutes(15);
            }
        }
        return result;
    }

    static List<String> toShortTimeAxis(List<String> fullAxis) {
        if (fullAxis == null || fullAxis.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(fullAxis.size());
        for (String value : fullAxis) {
            result.add(toShortTime(value));
        }
        return result;
    }

    static String toShortTime(String value) {
        LocalDateTime dateTime = parseDateTime(value, null);
        if (dateTime != null) {
            return TIME_FORMATTER.format(dateTime);
        }
        return value;
    }

    static void collectTelemetryScope(List<AggregatorEntDevice> deviceList, Set<Long> deviceIds, Set<String> deviceCodes) {
        if (deviceList == null || deviceList.isEmpty()) {
            return;
        }
        for (AggregatorEntDevice device : deviceList) {
            if (device == null) {
                continue;
            }
            addTelemetryDeviceId(deviceIds, device.getIotDeviceBaseId());
            String deviceCode = normalizeTelemetryDeviceCode(device.getDeviceId());
            if (StringUtils.isNotBlank(deviceCode)) {
                deviceCodes.add(deviceCode);
            }
        }
    }

    static Set<Long> newDeviceIdSet() {
        return new LinkedHashSet<>();
    }

    static Set<String> newDeviceCodeSet() {
        return new LinkedHashSet<>();
    }

    static Map<String, Double> toValueMap(List<DataResp> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Double> result = new LinkedHashMap<>();
        for (DataResp item : dataList) {
            if (item == null || StringUtils.isBlank(item.getTime())) {
                continue;
            }
            result.put(normalizeFullMinute(item.getTime()), item.getValue());
        }
        return result;
    }

    static Map<String, Double> parseCurveJson(String recordDate, String chartJson) {
        if (StringUtils.isBlank(chartJson)) {
            return Collections.emptyMap();
        }
        JSONArray array;
        try {
            array = JSONArray.parseArray(chartJson);
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
        if (array == null || array.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Double> result = new LinkedHashMap<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            if (item == null) {
                continue;
            }
            LocalDateTime dateTime = parseCurvePointDateTime(recordDate, extractTime(item));
            Double value = extractValue(item);
            if (dateTime == null || value == null) {
                continue;
            }
            result.put(DATE_TIME_FORMATTER.format(dateTime.withSecond(0).withNano(0)), MathUtils.doublePoint(value, 2));
        }
        return result;
    }

    static Map<String, Double> mergeCurveMaps(List<Map<String, Double>> mapList) {
        if (mapList == null || mapList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map<String, Double> map : mapList) {
            if (map == null || map.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                result.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        return result;
    }

    static List<DataResp> alignMinuteAxis(List<String> fullAxis, Map<String, Double> valueMap, String resourceTypeId) {
        if (fullAxis == null || fullAxis.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Double> finalMap = valueMap == null ? Collections.emptyMap() : valueMap;
        List<DataResp> result = new ArrayList<>(fullAxis.size());
        for (String time : fullAxis) {
            Double value = finalMap.get(time);
            result.add(new DataResp(toShortTime(time), adjustPowerValue(value, resourceTypeId)));
        }
        return result;
    }

    static List<DataResp> alignQuarterAxis(List<String> axis, Map<String, Double> valueMap) {
        if (axis == null || axis.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Double> finalMap = valueMap == null ? Collections.emptyMap() : valueMap;
        List<DataResp> result = new ArrayList<>(axis.size());
        for (String time : axis) {
            result.add(new DataResp(time, finalMap.get(time)));
        }
        return result;
    }

    static List<DataResp> expandQuarterMapToMinuteAxis(List<String> fullAxis,
                                                       Map<String, Double> quarterValueMap,
                                                       String resourceTypeId) {
        if (fullAxis == null || fullAxis.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Double> finalMap = quarterValueMap == null ? Collections.emptyMap() : quarterValueMap;
        List<DataResp> result = new ArrayList<>(fullAxis.size());
        for (String axisTime : fullAxis) {
            LocalDateTime current = LocalDateTime.parse(axisTime, DATE_TIME_FORMATTER);
            LocalDateTime bucket = resolveQuarterBucket(current);
            Double value = finalMap.get(DATE_TIME_FORMATTER.format(bucket));
            result.add(new DataResp(toShortTime(axisTime), adjustPowerValue(value, resourceTypeId)));
        }
        return result;
    }

    static List<List<IndexOverviewTimeColorResp>> buildColorRanges(List<DataResp> issueChart,
                                                                   List<DataResp> powerChart) {
        if (issueChart == null || powerChart == null || issueChart.isEmpty() || powerChart.isEmpty()) {
            return Collections.emptyList();
        }
        int blockCount = Math.min(issueChart.size(), powerChart.size()) / 15;
        if (blockCount <= 0) {
            return Collections.emptyList();
        }
        List<List<IndexOverviewTimeColorResp>> result = new ArrayList<>();
        for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
            int startIndex = blockIndex * 15;
            int endIndex = startIndex + 14;
            Double issueValue = issueChart.get(endIndex).getValue();
            if (issueValue == null) {
                continue;
            }
            double total = 0D;
            int count = 0;
            for (int i = startIndex; i <= endIndex && i < powerChart.size(); i++) {
                Double value = powerChart.get(i).getValue();
                if (value != null) {
                    total += value;
                    count++;
                }
            }
            if (count == 0) {
                continue;
            }
            Double averagePower = MathUtils.doublePoint(total / count, 2);
            Double threshold = MathUtils.mulDoubleNull(issueValue, 0.7D, 2);
            if (threshold == null || averagePower == null || averagePower >= threshold) {
                continue;
            }
            IndexOverviewTimeColorResp start = new IndexOverviewTimeColorResp();
            start.setxAxis(issueChart.get(startIndex).getTime());
            IndexOverviewTimeColorResp end = new IndexOverviewTimeColorResp();
            end.setxAxis(issueChart.get(endIndex).getTime());
            List<IndexOverviewTimeColorResp> range = new ArrayList<>(2);
            range.add(start);
            range.add(end);
            result.add(range);
        }
        return result;
    }

    static Double firstValue(Map<String, Double> valueMap, String time) {
        if (valueMap == null || StringUtils.isBlank(time)) {
            return null;
        }
        return valueMap.get(normalizeFullMinute(time));
    }

    private static String extractTime(JSONObject item) {
        String[] fields = new String[]{"time", "date", "dateTime", "readTime"};
        for (String field : fields) {
            String value = item.getString(field);
            if (StringUtils.isNotBlank(value)) {
                return StringUtils.trim(value);
            }
        }
        return null;
    }

    private static Double extractValue(JSONObject item) {
        String[] fields = new String[]{"value", "quantity", "dateValue", "useQuantity"};
        for (String field : fields) {
            Object rawValue = item.get(field);
            if (rawValue == null) {
                continue;
            }
            if (rawValue instanceof Number) {
                return ((Number) rawValue).doubleValue();
            }
            String text = StringUtils.trimToNull(String.valueOf(rawValue));
            if (text == null) {
                continue;
            }
            try {
                return Double.valueOf(text);
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        return null;
    }

    private static LocalDateTime resolveQuarterBucket(LocalDateTime current) {
        if (current.getMinute() % 15 == 0) {
            return current.withSecond(0).withNano(0);
        }
        int add = 15 - current.getMinute() % 15;
        return current.plusMinutes(add).withSecond(0).withNano(0);
    }

    private static Double adjustPowerValue(Double value, String resourceTypeId) {
        if (value == null) {
            return null;
        }
        if (STORAGE_RESOURCE_TYPE_ID.equals(resourceTypeId)) {
            return MathUtils.doublePoint(0D - value, 2);
        }
        return MathUtils.doublePoint(value, 2);
    }

    private static void addTelemetryDeviceId(Set<Long> deviceIds, String rawDeviceId) {
        if (deviceIds == null || StringUtils.isBlank(rawDeviceId)) {
            return;
        }
        try {
            deviceIds.add(Long.valueOf(StringUtils.trim(rawDeviceId)));
        } catch (NumberFormatException ignored) {
            // ignore invalid device id
        }
    }

    private static String normalizeTelemetryDeviceCode(String rawDeviceCode) {
        String deviceCode = StringUtils.trimToNull(rawDeviceCode);
        if (deviceCode == null) {
            return null;
        }
        int separatorIndex = deviceCode.indexOf('_');
        if (separatorIndex < 0 || separatorIndex == deviceCode.length() - 1) {
            return deviceCode;
        }
        return deviceCode.substring(separatorIndex + 1);
    }

    private static String normalizeFullMinute(String value) {
        LocalDateTime dateTime = parseDateTime(value, null);
        if (dateTime != null) {
            return DATE_TIME_FORMATTER.format(dateTime.withSecond(0).withNano(0));
        }
        return StringUtils.trim(value);
    }

    private static LocalDateTime parseCurvePointDateTime(String recordDate, String rawTime) {
        if (StringUtils.isBlank(rawTime)) {
            return null;
        }
        String value = StringUtils.trim(rawTime);
        LocalDateTime fullDateTime = parseDateTime(value, recordDate);
        if (fullDateTime != null) {
            return fullDateTime;
        }
        LocalTime localTime = parseLocalTime(value);
        if (localTime == null || StringUtils.isBlank(recordDate)) {
            return null;
        }
        LocalDate date = LocalDate.parse(recordDate, DATE_FORMATTER);
        return LocalDateTime.of(date, localTime);
    }

    private static LocalDateTime parseDateTime(String rawValue, String defaultDate) {
        if (StringUtils.isBlank(rawValue)) {
            return null;
        }
        String value = StringUtils.trim(rawValue).replace('/', '-');
        if (value.matches("\\d{4}-\\d{1,2}-\\d{1,2}\\s24:00(:00)?")) {
            String[] parts = value.split("\\s+");
            LocalDate date = LocalDate.parse(parts[0], DATE_FORMATTER);
            return LocalDateTime.of(date.plusDays(1), LocalTime.MIN);
        }
        if (defaultDate != null && value.matches("24:00(:00)?")) {
            LocalDate date = LocalDate.parse(defaultDate, DATE_FORMATTER);
            return LocalDateTime.of(date.plusDays(1), LocalTime.MIN);
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                DATE_TIME_FORMATTER,
                DATE_TIME_SECOND_FORMATTER,
                DateTimeFormatter.ofPattern("yyyy-M-d H:mm"),
                DateTimeFormatter.ofPattern("yyyy-M-d H:mm:ss"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        if (defaultDate != null) {
            LocalTime localTime = parseLocalTime(value);
            if (localTime != null) {
                return LocalDateTime.of(LocalDate.parse(defaultDate, DATE_FORMATTER), localTime);
            }
        }
        return null;
    }

    private static LocalTime parseLocalTime(String value) {
        DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                TIME_FORMATTER,
                TIME_SECOND_FORMATTER,
                SHORT_TIME_FORMATTER,
                SHORT_TIME_SECOND_FORMATTER
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        return null;
    }
}
