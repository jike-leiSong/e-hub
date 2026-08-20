package cn.sl.ehub.service.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.dto.tariff.AgentPriceQueryReq;
import cn.sl.ehub.service.dto.tariff.FpgjPointResp;
import cn.sl.ehub.service.dto.tariff.TariffAgentPriceDataInsertRow;
import cn.sl.ehub.service.dto.tariff.TariffAgentPriceInsertRow;
import cn.sl.ehub.service.dto.tariff.TariffAgentPriceStoredPoint;
import cn.sl.ehub.service.dto.tariff.TariffAgentPriceStoredRow;
import cn.sl.ehub.service.dto.tariff.TariffFpgjTypeDataInsertRow;
import cn.sl.ehub.service.dto.tariff.TariffFpgjTypeInsertRow;
import cn.sl.ehub.service.dto.tariff.TariffRuleCopyReq;
import cn.sl.ehub.service.dto.tariff.TariffRuleDeleteReq;
import cn.sl.ehub.service.dto.tariff.TariffRuleDeleteResp;
import cn.sl.ehub.service.dto.tariff.TariffRuleImportReq;
import cn.sl.ehub.service.dto.tariff.TariffRulePeriodReq;
import cn.sl.ehub.service.dto.tariff.TariffRulePointResp;
import cn.sl.ehub.service.dto.tariff.TariffRulePreviewResp;
import cn.sl.ehub.service.dto.tariff.TariffRulePricePreviewResp;
import cn.sl.ehub.service.dto.tariff.TariffRulePriceRowReq;
import cn.sl.ehub.service.dto.tariff.TariffRulePublishResp;
import cn.sl.ehub.service.mapper.TariffAgentPriceMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TariffAgentPriceImportService {

    private static final BigDecimal DB_PRICE_RATE = new BigDecimal("1000");
    private static final BigDecimal DISPLAY_PRICE_RATE = new BigDecimal("0.001");
    private static final DateTimeFormatter MONTH_VERSION_FORMATTER = DateTimeFormatter.ofPattern("yyMM");
    private static final DateTimeFormatter DAY_VERSION_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final List<String> PERIOD_ORDER = Arrays.asList("尖", "峰", "平", "谷", "深谷");
    private static final int POINTS_PER_DAY = 96;
    private static final int RANGE_MAX_DAYS = 62;

    private final TariffAgentPriceMapper tariffAgentPriceMapper;

    public TariffAgentPriceImportService(TariffAgentPriceMapper tariffAgentPriceMapper) {
        this.tariffAgentPriceMapper = tariffAgentPriceMapper;
    }

    public TariffRulePreviewResp preview(TariffRuleImportReq req) {
        return generate(req).getPreview();
    }

    @Transactional(rollbackFor = Exception.class)
    public TariffRuleDeleteResp delete(TariffRuleDeleteReq req) {
        if (req == null) {
            throwParam("删除参数不能为空");
        }
        String provinceCode = require(req.getProvinceCode(), "省份不能为空");
        String secondType = StringUtils.defaultIfBlank(req.getSecondType(), "不限");
        String thirdType = StringUtils.defaultIfBlank(req.getThirdType(), "不限");
        List<String> versions = resolveDeleteVersions(req);

        int fpgjRowCount = 0;
        int fpgjPointCount = 0;
        int priceRowCount = 0;
        int pricePointCount = 0;
        for (String version : versions) {
            pricePointCount += tariffAgentPriceMapper.deleteAgentPriceDataByScope(
                    version, provinceCode, secondType, thirdType);
            priceRowCount += tariffAgentPriceMapper.deleteAgentPricesByScope(
                    version, provinceCode, secondType, thirdType);

            int remainingPriceRows = tariffAgentPriceMapper.countAgentPricesBySecondScope(
                    version, provinceCode, secondType);
            if (remainingPriceRows == 0) {
                fpgjPointCount += tariffAgentPriceMapper.deleteFpgjTypeDataByScope(
                        version, provinceCode, secondType);
                fpgjRowCount += tariffAgentPriceMapper.deleteFpgjTypesByScope(
                        version, provinceCode, secondType);
            }
        }
        if (priceRowCount == 0 && pricePointCount == 0 && fpgjRowCount == 0 && fpgjPointCount == 0) {
            throwParam("未找到匹配的正式电价数据，请确认版本、省份、二级/三级区域是否一致");
        }

        TariffRuleDeleteResp resp = new TariffRuleDeleteResp();
        resp.setVersions(versions);
        resp.setFpgjRowCount(fpgjRowCount);
        resp.setFpgjPointCount(fpgjPointCount);
        resp.setPriceRowCount(priceRowCount);
        resp.setPricePointCount(pricePointCount);
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    public TariffRulePublishResp publish(TariffRuleImportReq req) {
        GeneratedRuleData data = generate(req);
        for (String version : data.getVersions()) {
            replaceFpgj(version, data);
            replaceAgentPrice(version, data);
        }

        TariffRulePublishResp resp = new TariffRulePublishResp();
        resp.setVersions(data.getVersions());
        resp.setFpgjRowCount(data.getVersions().size());
        resp.setFpgjPointCount(data.getVersions().size() * POINTS_PER_DAY);
        resp.setPriceRowCount(data.getVersions().size() * data.getPriceRows().size());
        resp.setPricePointCount(data.getVersions().size() * data.getPriceRows().size() * POINTS_PER_DAY);
        return resp;
    }

    public TariffRuleImportReq copy(TariffRuleCopyReq req) {
        if (req == null) {
            throwParam("复制参数不能为空");
        }
        String sourceVersion = resolveSourceVersion(req.getSourceVersion());
        String sourceProvinceCode = require(req.getSourceProvinceCode(), "来源省份不能为空");
        String sourceProvinceName = StringUtils.defaultIfBlank(req.getSourceProvinceName(), "");
        String sourceSecondType = StringUtils.defaultIfBlank(req.getSourceSecondType(), "不限");
        String sourceThirdType = StringUtils.defaultIfBlank(req.getSourceThirdType(), "不限");

        AgentPriceQueryReq fpgjQuery = new AgentPriceQueryReq();
        fpgjQuery.setYearMonth(sourceVersion);
        fpgjQuery.setProvinceCode(sourceProvinceCode);
        fpgjQuery.setSecondType(sourceSecondType);
        List<FpgjPointResp> fpgjPoints = tariffAgentPriceMapper.selectFpgjData(fpgjQuery);
        if (CollectionUtils.isEmpty(fpgjPoints)) {
            throwParam("未找到来源版本的峰谷时段数据");
        }

        List<TariffAgentPriceStoredRow> storedRows = tariffAgentPriceMapper.selectAgentPriceRowsByScope(
                sourceVersion, sourceProvinceCode, sourceSecondType, sourceThirdType);
        if (CollectionUtils.isEmpty(storedRows)) {
            throwParam("未找到来源版本的价格数据");
        }
        List<TariffAgentPriceStoredPoint> storedPoints = tariffAgentPriceMapper.selectAgentPricePointsByScope(
                sourceVersion, sourceProvinceCode, sourceSecondType, sourceThirdType);

        Map<String, String> periodByTime = toPeriodByTime(fpgjPoints);
        Map<String, List<TariffAgentPriceStoredPoint>> pointsByPriceId = groupStoredPoints(storedPoints);

        TariffRuleImportReq target = new TariffRuleImportReq();
        target.setEffectiveType(StringUtils.defaultIfBlank(req.getTargetEffectiveType(), "MONTH"));
        target.setYearMonth(req.getTargetYearMonth());
        target.setSelectedDate(req.getTargetSelectedDate());
        target.setStartDate(req.getTargetStartDate());
        target.setEndDate(req.getTargetEndDate());
        target.setProvinceCode(StringUtils.defaultIfBlank(req.getTargetProvinceCode(), sourceProvinceCode));
        target.setProvinceName(StringUtils.defaultIfBlank(req.getTargetProvinceName(), sourceProvinceName));
        target.setSecondType(StringUtils.defaultIfBlank(req.getTargetSecondType(), sourceSecondType));
        target.setThirdType(StringUtils.defaultIfBlank(req.getTargetThirdType(), sourceThirdType));
        target.setPeriods(buildPeriodRules(fpgjPoints));
        target.setPriceRows(buildPriceRules(storedRows, pointsByPriceId, periodByTime));
        return target;
    }

    private void replaceFpgj(String version, GeneratedRuleData data) {
        tariffAgentPriceMapper.deleteFpgjTypeDataByScope(version, data.getProvinceCode(), data.getSecondType());
        tariffAgentPriceMapper.deleteFpgjTypesByScope(version, data.getProvinceCode(), data.getSecondType());

        String fpgjTypeId = newId();
        TariffFpgjTypeInsertRow typeRow = new TariffFpgjTypeInsertRow();
        typeRow.setId(fpgjTypeId);
        typeRow.setVersion(version);
        typeRow.setProvinceCode(data.getProvinceCode());
        typeRow.setProvinceName(data.getProvinceName());
        typeRow.setSecondType(data.getSecondType());
        typeRow.setCreateBy(data.getOperatorName());
        tariffAgentPriceMapper.insertFpgjType(typeRow);

        List<TariffFpgjTypeDataInsertRow> rows = new ArrayList<>();
        for (TariffRulePointResp point : data.getFpgjPoints()) {
            TariffFpgjTypeDataInsertRow row = new TariffFpgjTypeDataInsertRow();
            row.setFpgjTypeId(fpgjTypeId);
            row.setBizTime(point.getBizTime());
            row.setFpgjType(point.getPeriodType());
            rows.add(row);
        }
        tariffAgentPriceMapper.batchInsertFpgjTypeData(rows);
    }

    private void replaceAgentPrice(String version, GeneratedRuleData data) {
        tariffAgentPriceMapper.deleteAgentPriceDataByPriceRows(
                version, data.getProvinceCode(), data.getSecondType(), data.getThirdType(), data.getPriceRows());
        tariffAgentPriceMapper.deleteAgentPricesByPriceRows(
                version, data.getProvinceCode(), data.getSecondType(), data.getThirdType(), data.getPriceRows());

        List<TariffAgentPriceInsertRow> priceRows = new ArrayList<>();
        List<TariffAgentPriceDataInsertRow> priceDataRows = new ArrayList<>();
        for (TariffRulePricePreviewResp previewRow : data.getPriceRows()) {
            String priceId = newId();
            TariffAgentPriceInsertRow row = new TariffAgentPriceInsertRow();
            row.setId(priceId);
            row.setVersion(version);
            row.setProvinceCode(data.getProvinceCode());
            row.setProvinceName(data.getProvinceName());
            row.setSecondType(data.getSecondType());
            row.setThirdType(data.getThirdType());
            row.setDyLevel(previewRow.getDyLevel());
            row.setUserType(previewRow.getUserType());
            row.setOtherType(previewRow.getSfType());
            row.setPriceType(previewRow.getPriceType());
            row.setCreateBy(data.getOperatorName());
            row.setCapacityElectricityPrice(scaleOptional(previewRow.getCapacityElectricityPrice()));
            row.setDemandElectricityPrice(scaleOptional(previewRow.getDemandElectricityPrice()));
            priceRows.add(row);

            for (int i = 0; i < POINTS_PER_DAY; i++) {
                String periodType = data.getPeriodByIndex()[i];
                BigDecimal price = priceOfPeriod(previewRow, periodType);
                TariffAgentPriceDataInsertRow detail = new TariffAgentPriceDataInsertRow();
                detail.setAgentPriceId(priceId);
                detail.setBizTime(indexTransBizTime(i));
                detail.setPrice(toDbPrice(price));
                priceDataRows.add(detail);
            }
        }
        tariffAgentPriceMapper.batchInsertAgentPrice(priceRows);
        tariffAgentPriceMapper.batchInsertAgentPriceData(priceDataRows);
    }

    private GeneratedRuleData generate(TariffRuleImportReq req) {
        if (req == null) {
            throwParam("导入参数不能为空");
        }
        String provinceCode = require(req.getProvinceCode(), "省份不能为空");
        String provinceName = require(req.getProvinceName(), "省份名称不能为空");
        String secondType = StringUtils.defaultIfBlank(req.getSecondType(), "不限");
        String thirdType = StringUtils.defaultIfBlank(req.getThirdType(), "不限");
        String operatorName = StringUtils.defaultIfBlank(req.getOperatorName(), "system");
        List<String> versions = resolveVersions(req);
        String[] periodByIndex = buildPeriodIndex(req.getPeriods());
        Set<String> usedPeriods = usedPeriods(periodByIndex);
        List<TariffRulePricePreviewResp> priceRows = buildPreviewPriceRows(req.getPriceRows(), usedPeriods);
        List<TariffRulePointResp> fpgjPoints = buildFpgjPoints(periodByIndex);

        TariffRulePreviewResp preview = new TariffRulePreviewResp();
        preview.setVersion(versions.get(0));
        preview.setVersions(versions);
        preview.setProvinceCode(provinceCode);
        preview.setProvinceName(provinceName);
        preview.setSecondType(secondType);
        preview.setThirdType(thirdType);
        preview.setValid(true);
        preview.setFpgjPoints(fpgjPoints);
        preview.setPriceRows(priceRows);
        preview.setFpgjPointCount(versions.size() * POINTS_PER_DAY);
        preview.setPriceRowCount(versions.size() * priceRows.size());
        preview.setPricePointCount(versions.size() * priceRows.size() * POINTS_PER_DAY);

        GeneratedRuleData data = new GeneratedRuleData();
        data.setVersions(versions);
        data.setProvinceCode(provinceCode);
        data.setProvinceName(provinceName);
        data.setSecondType(secondType);
        data.setThirdType(thirdType);
        data.setOperatorName(operatorName);
        data.setPeriodByIndex(periodByIndex);
        data.setFpgjPoints(fpgjPoints);
        data.setPriceRows(priceRows);
        data.setPreview(preview);
        return data;
    }

    private List<String> resolveVersions(TariffRuleImportReq req) {
        return resolveVersions(
                req.getVersion(),
                req.getEffectiveType(),
                req.getYearMonth(),
                req.getSelectedDate(),
                req.getStartDate(),
                req.getEndDate()
        );
    }

    private List<String> resolveVersions(String versionValue,
                                         String effectiveTypeValue,
                                         String yearMonthValue,
                                         String selectedDateValue,
                                         String startDateValue,
                                         String endDateValue) {
        if (StringUtils.isNotBlank(versionValue)) {
            String version = TariffAgentPriceService.resolveVersion(versionValue);
            if (StringUtils.isBlank(version)) {
                throwParam("版本格式不正确");
            }
            return Collections.singletonList(version);
        }

        String effectiveType = StringUtils.upperCase(StringUtils.defaultIfBlank(effectiveTypeValue, ""));
        if (StringUtils.isBlank(effectiveType)) {
            if (StringUtils.isNotBlank(startDateValue) || StringUtils.isNotBlank(endDateValue)) {
                effectiveType = "RANGE";
            } else if (StringUtils.isNotBlank(selectedDateValue)) {
                effectiveType = "DAY";
            } else {
                effectiveType = "MONTH";
            }
        }

        if ("MONTH".equals(effectiveType)) {
            YearMonth yearMonth = parseYearMonth(yearMonthValue, "电价月份不能为空");
            return Collections.singletonList(yearMonth.format(MONTH_VERSION_FORMATTER));
        }
        if ("DAY".equals(effectiveType)) {
            LocalDate date = parseDate(selectedDateValue, "单日日期不能为空");
            return Collections.singletonList(date.format(DAY_VERSION_FORMATTER));
        }
        if ("RANGE".equals(effectiveType)) {
            LocalDate start = parseDate(startDateValue, "区间开始日期不能为空");
            LocalDate end = parseDate(endDateValue, "区间结束日期不能为空");
            if (end.isBefore(start)) {
                throwParam("区间结束日期不能早于开始日期");
            }
            List<String> versions = new ArrayList<>();
            LocalDate cursor = start;
            while (!cursor.isAfter(end)) {
                versions.add(cursor.format(DAY_VERSION_FORMATTER));
                cursor = cursor.plusDays(1);
                if (versions.size() > RANGE_MAX_DAYS) {
                    throwParam("节假日区间最多支持 " + RANGE_MAX_DAYS + " 天");
                }
            }
            return versions;
        }
        throwParam("生效类型仅支持 MONTH、DAY、RANGE");
        return Collections.emptyList();
    }

    private List<String> resolveDeleteVersions(TariffRuleDeleteReq req) {
        if (StringUtils.isNotBlank(req.getVersion())) {
            String version = TariffAgentPriceService.resolveVersion(req.getVersion());
            if (StringUtils.isBlank(version)) {
                throwParam("版本格式不正确");
            }
            if (version.matches("\\d{4}")) {
                return expandMonthDeleteVersions(parseShortMonthVersion(version));
            }
            return Collections.singletonList(version);
        }

        String effectiveType = StringUtils.upperCase(StringUtils.defaultIfBlank(req.getEffectiveType(), "MONTH"));
        if ("MONTH".equals(effectiveType)) {
            return expandMonthDeleteVersions(parseYearMonth(req.getYearMonth(), "电价月份不能为空"));
        }
        return resolveVersions(
                null,
                effectiveType,
                req.getYearMonth(),
                req.getSelectedDate(),
                req.getStartDate(),
                req.getEndDate()
        );
    }

    private List<String> expandMonthDeleteVersions(YearMonth yearMonth) {
        List<String> versions = new ArrayList<>();
        versions.add(yearMonth.format(MONTH_VERSION_FORMATTER));
        LocalDate cursor = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        while (!cursor.isAfter(end)) {
            versions.add(cursor.format(DAY_VERSION_FORMATTER));
            cursor = cursor.plusDays(1);
        }
        return versions;
    }

    private YearMonth parseShortMonthVersion(String version) {
        try {
            int year = 2000 + Integer.parseInt(version.substring(0, 2));
            int month = Integer.parseInt(version.substring(2, 4));
            return YearMonth.of(year, month);
        } catch (DateTimeException | NumberFormatException | IndexOutOfBoundsException ex) {
            throwParam("版本格式不正确");
            return null;
        }
    }

    private String[] buildPeriodIndex(List<TariffRulePeriodReq> periods) {
        if (CollectionUtils.isEmpty(periods)) {
            throwParam("请至少配置一条时段规则");
        }
        String[] periodByIndex = new String[POINTS_PER_DAY];
        for (TariffRulePeriodReq period : periods) {
            if (period == null) {
                continue;
            }
            String periodType = normalizePeriodType(period.getPeriodType());
            if (StringUtils.isBlank(periodType)) {
                throwParam("时段类型仅支持 尖、峰、平、谷、深谷");
            }
            List<String> ranges = normalizeRanges(period);
            if (CollectionUtils.isEmpty(ranges)) {
                throwParam(periodType + "时段未配置时间范围");
            }
            for (String range : ranges) {
                int[] indexes = parseRange(range);
                for (int i = indexes[0]; i < indexes[1]; i++) {
                    if (StringUtils.isNotBlank(periodByIndex[i])) {
                        throwParam(indexTransBizTime(i) + " 存在重复时段配置");
                    }
                    periodByIndex[i] = periodType;
                }
            }
        }
        for (int i = 0; i < POINTS_PER_DAY; i++) {
            if (StringUtils.isBlank(periodByIndex[i])) {
                throwParam(indexTransBizTime(i) + " 未配置时段");
            }
        }
        return periodByIndex;
    }

    private List<TariffRulePricePreviewResp> buildPreviewPriceRows(List<TariffRulePriceRowReq> rows,
                                                                   Set<String> usedPeriods) {
        if (CollectionUtils.isEmpty(rows)) {
            throwParam("请至少配置一条价格行");
        }
        List<TariffRulePricePreviewResp> result = new ArrayList<>();
        int rowNo = 1;
        Set<String> rowKeys = new LinkedHashSet<>();
        for (TariffRulePriceRowReq row : rows) {
            if (row == null) {
                continue;
            }
            String userType = require(row.getUserType(), "第 " + rowNo + " 行企业用电类别不能为空");
            String dyLevel = require(row.getDyLevel(), "第 " + rowNo + " 行电压等级不能为空");
            String sfType = require(row.getSfType(), "第 " + rowNo + " 行收费类型不能为空");
            String priceType = StringUtils.defaultIfBlank(row.getPriceType(), "电度");
            String rowKey = userType + "|" + dyLevel + "|" + sfType + "|" + priceType;
            if (!rowKeys.add(rowKey)) {
                throwParam("第 " + rowNo + " 行存在重复价格对象");
            }
            for (String periodType : usedPeriods) {
                BigDecimal price = priceOfPeriod(row, periodType);
                if (price == null) {
                    throwParam("第 " + rowNo + " 行缺少" + periodType + "时价格");
                }
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    throwParam("第 " + rowNo + " 行" + periodType + "时价格不能为负数");
                }
            }

            TariffRulePricePreviewResp preview = new TariffRulePricePreviewResp();
            BeanUtils.copyProperties(row, preview);
            preview.setRowNo(rowNo);
            preview.setUserType(userType);
            preview.setDyLevel(dyLevel);
            preview.setSfType(sfType);
            preview.setPriceType(priceType);
            preview.setJianPrice(scaleOptional(row.getJianPrice()));
            preview.setFengPrice(scaleOptional(row.getFengPrice()));
            preview.setPingPrice(scaleOptional(row.getPingPrice()));
            preview.setGuPrice(scaleOptional(row.getGuPrice()));
            preview.setShenguPrice(scaleOptional(row.getShenguPrice()));
            preview.setCapacityElectricityPrice(scaleOptional(row.getCapacityElectricityPrice()));
            preview.setDemandElectricityPrice(scaleOptional(row.getDemandElectricityPrice()));
            preview.setPointCount(POINTS_PER_DAY);
            result.add(preview);
            rowNo++;
        }
        if (result.isEmpty()) {
            throwParam("请至少配置一条有效价格行");
        }
        return result;
    }

    private List<TariffRulePointResp> buildFpgjPoints(String[] periodByIndex) {
        List<TariffRulePointResp> points = new ArrayList<>();
        for (int i = 0; i < POINTS_PER_DAY; i++) {
            TariffRulePointResp point = new TariffRulePointResp();
            point.setBizTime(indexTransBizTime(i));
            point.setPeriodType(periodByIndex[i]);
            points.add(point);
        }
        return points;
    }

    private Set<String> usedPeriods(String[] periodByIndex) {
        Set<String> result = new LinkedHashSet<>();
        for (String periodType : PERIOD_ORDER) {
            for (String value : periodByIndex) {
                if (StringUtils.equals(periodType, value)) {
                    result.add(periodType);
                    break;
                }
            }
        }
        return result;
    }

    private List<String> normalizeRanges(TariffRulePeriodReq period) {
        List<String> result = new ArrayList<>();
        if (period.getRanges() != null) {
            for (String range : period.getRanges()) {
                if (StringUtils.isNotBlank(range)) {
                    result.add(StringUtils.trim(range));
                }
            }
        }
        if (!result.isEmpty()) {
            return result;
        }
        if (StringUtils.isNotBlank(period.getRangeText())) {
            String[] ranges = StringUtils.split(period.getRangeText(), ",，;；\n\r");
            if (ranges != null) {
                for (String range : ranges) {
                    if (StringUtils.isNotBlank(range)) {
                        result.add(StringUtils.trim(range));
                    }
                }
            }
        }
        return result;
    }

    private int[] parseRange(String range) {
        String normalized = StringUtils.replaceEach(StringUtils.deleteWhitespace(range),
                new String[]{"~", "—", "－", "至", "到"},
                new String[]{"-", "-", "-", "-", "-"});
        String[] parts = StringUtils.split(normalized, '-');
        if (parts == null || parts.length != 2) {
            throwParam("时间段格式不正确：" + range);
        }
        int start = parseTimeIndex(parts[0], false, range);
        int end = parseTimeIndex(parts[1], true, range);
        if (start >= end) {
            throwParam("时间段开始时间必须早于结束时间：" + range);
        }
        return new int[]{start, end};
    }

    private int parseTimeIndex(String time, boolean end, String range) {
        if (!StringUtils.equals(time, "24:00") && !time.matches("\\d{2}:\\d{2}")) {
            throwParam("时间格式必须为 HH:mm：" + range);
        }
        String[] parts = StringUtils.split(time, ':');
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        if (hour == 24 && minute == 0 && end) {
            return POINTS_PER_DAY;
        }
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || minute % 15 != 0) {
            throwParam("时间必须是 00:00 到 24:00 内的 15 分钟刻度：" + range);
        }
        return hour * 4 + minute / 15;
    }

    private List<TariffRulePeriodReq> buildPeriodRules(List<FpgjPointResp> fpgjPoints) {
        String[] periodByIndex = new String[POINTS_PER_DAY];
        for (FpgjPointResp point : fpgjPoints) {
            if (point == null || StringUtils.isBlank(point.getBizTime())) {
                continue;
            }
            int index = bizTimeTransIndex(point.getBizTime());
            if (index >= 0 && index < POINTS_PER_DAY) {
                periodByIndex[index] = normalizePeriodType(point.getFgvalue());
            }
        }
        Map<String, List<String>> rangesByPeriod = new LinkedHashMap<>();
        for (String periodType : PERIOD_ORDER) {
            rangesByPeriod.put(periodType, new ArrayList<String>());
        }
        int index = 0;
        while (index < POINTS_PER_DAY) {
            String periodType = periodByIndex[index];
            int start = index;
            while (index < POINTS_PER_DAY && StringUtils.equals(periodType, periodByIndex[index])) {
                index++;
            }
            if (StringUtils.isNotBlank(periodType) && rangesByPeriod.containsKey(periodType)) {
                rangesByPeriod.get(periodType).add(indexTransBizTime(start) + "-" + indexTransBizTime(index));
            }
        }
        List<TariffRulePeriodReq> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : rangesByPeriod.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            TariffRulePeriodReq period = new TariffRulePeriodReq();
            period.setPeriodType(entry.getKey());
            period.setRanges(entry.getValue());
            period.setRangeText(StringUtils.join(entry.getValue(), ", "));
            result.add(period);
        }
        return result;
    }

    private List<TariffRulePriceRowReq> buildPriceRules(List<TariffAgentPriceStoredRow> storedRows,
                                                        Map<String, List<TariffAgentPriceStoredPoint>> pointsByPriceId,
                                                        Map<String, String> periodByTime) {
        List<TariffRulePriceRowReq> result = new ArrayList<>();
        for (TariffAgentPriceStoredRow storedRow : storedRows) {
            TariffRulePriceRowReq row = new TariffRulePriceRowReq();
            row.setUserType(storedRow.getUserType());
            row.setDyLevel(storedRow.getDyLevel());
            row.setSfType(storedRow.getOtherType());
            row.setPriceType(StringUtils.defaultIfBlank(storedRow.getPriceType(), "电度"));
            row.setCapacityElectricityPrice(scaleOptional(storedRow.getCapacityElectricityPrice()));
            row.setDemandElectricityPrice(scaleOptional(storedRow.getDemandElectricityPrice()));

            List<TariffAgentPriceStoredPoint> points = pointsByPriceId.get(storedRow.getId());
            if (points != null) {
                for (TariffAgentPriceStoredPoint point : points) {
                    String periodType = periodByTime.get(point.getBizTime());
                    if (StringUtils.isBlank(periodType) || point.getPrice() == null) {
                        continue;
                    }
                    setPeriodPriceIfAbsent(row, periodType, point.getPrice().multiply(DISPLAY_PRICE_RATE).stripTrailingZeros());
                }
            }
            result.add(row);
        }
        return result;
    }

    private Map<String, String> toPeriodByTime(List<FpgjPointResp> points) {
        Map<String, String> result = new HashMap<>();
        for (FpgjPointResp point : points) {
            if (point != null && StringUtils.isNotBlank(point.getBizTime())) {
                result.put(point.getBizTime(), normalizePeriodType(point.getFgvalue()));
            }
        }
        return result;
    }

    private Map<String, List<TariffAgentPriceStoredPoint>> groupStoredPoints(List<TariffAgentPriceStoredPoint> points) {
        Map<String, List<TariffAgentPriceStoredPoint>> result = new LinkedHashMap<>();
        if (points == null) {
            return result;
        }
        for (TariffAgentPriceStoredPoint point : points) {
            if (point == null || StringUtils.isBlank(point.getAgentPriceId())) {
                continue;
            }
            List<TariffAgentPriceStoredPoint> rows = result.get(point.getAgentPriceId());
            if (rows == null) {
                rows = new ArrayList<>();
                result.put(point.getAgentPriceId(), rows);
            }
            rows.add(point);
        }
        return result;
    }

    private void setPeriodPriceIfAbsent(TariffRulePriceRowReq row, String periodType, BigDecimal price) {
        if ("尖".equals(periodType) && row.getJianPrice() == null) {
            row.setJianPrice(price);
        } else if ("峰".equals(periodType) && row.getFengPrice() == null) {
            row.setFengPrice(price);
        } else if ("平".equals(periodType) && row.getPingPrice() == null) {
            row.setPingPrice(price);
        } else if ("谷".equals(periodType) && row.getGuPrice() == null) {
            row.setGuPrice(price);
        } else if ("深谷".equals(periodType) && row.getShenguPrice() == null) {
            row.setShenguPrice(price);
        }
    }

    private BigDecimal priceOfPeriod(TariffRulePriceRowReq row, String periodType) {
        if ("尖".equals(periodType)) {
            return row.getJianPrice();
        }
        if ("峰".equals(periodType)) {
            return row.getFengPrice();
        }
        if ("平".equals(periodType)) {
            return row.getPingPrice();
        }
        if ("谷".equals(periodType)) {
            return row.getGuPrice();
        }
        if ("深谷".equals(periodType)) {
            return row.getShenguPrice();
        }
        return null;
    }

    private BigDecimal priceOfPeriod(TariffRulePricePreviewResp row, String periodType) {
        if ("尖".equals(periodType)) {
            return row.getJianPrice();
        }
        if ("峰".equals(periodType)) {
            return row.getFengPrice();
        }
        if ("平".equals(periodType)) {
            return row.getPingPrice();
        }
        if ("谷".equals(periodType)) {
            return row.getGuPrice();
        }
        if ("深谷".equals(periodType)) {
            return row.getShenguPrice();
        }
        return BigDecimal.ZERO;
    }

    private String normalizePeriodType(String value) {
        String text = StringUtils.trimToEmpty(value);
        if ("深".equals(text)) {
            return "深谷";
        }
        if (PERIOD_ORDER.contains(text)) {
            return text;
        }
        return "";
    }

    private String resolveSourceVersion(String sourceVersion) {
        String version = TariffAgentPriceService.resolveVersion(sourceVersion);
        if (StringUtils.isBlank(version)) {
            throwParam("来源版本不能为空或格式不正确");
        }
        return version;
    }

    private YearMonth parseYearMonth(String value, String emptyMessage) {
        if (StringUtils.isBlank(value)) {
            throwParam(emptyMessage);
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            throwParam("月份格式必须为 yyyy-MM");
            return null;
        }
    }

    private LocalDate parseDate(String value, String emptyMessage) {
        if (StringUtils.isBlank(value)) {
            throwParam(emptyMessage);
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throwParam("日期格式必须为 yyyy-MM-dd");
            return null;
        }
    }

    private String require(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throwParam(message);
        }
        return StringUtils.trim(value);
    }

    private BigDecimal toDbPrice(BigDecimal price) {
        return price.multiply(DB_PRICE_RATE).setScale(7, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleOptional(BigDecimal value) {
        return value == null ? null : value.setScale(7, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private String newId() {
        return UUID.randomUUID().toString();
    }

    private static int bizTimeTransIndex(String bizTime) {
        if (StringUtils.isBlank(bizTime) || !bizTime.contains(":")) {
            return 0;
        }
        String[] arr = bizTime.split(":");
        return Integer.parseInt(arr[0]) * 4 + Integer.parseInt(arr[1]) / 15;
    }

    private static String indexTransBizTime(int index) {
        int hour = index / 4;
        int minute = index % 4;
        return (hour >= 10 ? String.valueOf(hour) : "0" + hour)
                + ":"
                + (minute * 15 > 0 ? String.valueOf(minute * 15) : "00");
    }

    private void throwParam(String message) {
        throw new BaseException(StatusCode.C.getCode(), message);
    }

    private static class GeneratedRuleData {
        private List<String> versions;
        private String provinceCode;
        private String provinceName;
        private String secondType;
        private String thirdType;
        private String operatorName;
        private String[] periodByIndex;
        private List<TariffRulePointResp> fpgjPoints;
        private List<TariffRulePricePreviewResp> priceRows;
        private TariffRulePreviewResp preview;

        public List<String> getVersions() {
            return versions;
        }

        public void setVersions(List<String> versions) {
            this.versions = versions;
        }

        public String getProvinceCode() {
            return provinceCode;
        }

        public void setProvinceCode(String provinceCode) {
            this.provinceCode = provinceCode;
        }

        public String getProvinceName() {
            return provinceName;
        }

        public void setProvinceName(String provinceName) {
            this.provinceName = provinceName;
        }

        public String getSecondType() {
            return secondType;
        }

        public void setSecondType(String secondType) {
            this.secondType = secondType;
        }

        public String getThirdType() {
            return thirdType;
        }

        public void setThirdType(String thirdType) {
            this.thirdType = thirdType;
        }

        public String getOperatorName() {
            return operatorName;
        }

        public void setOperatorName(String operatorName) {
            this.operatorName = operatorName;
        }

        public String[] getPeriodByIndex() {
            return periodByIndex;
        }

        public void setPeriodByIndex(String[] periodByIndex) {
            this.periodByIndex = periodByIndex;
        }

        public List<TariffRulePointResp> getFpgjPoints() {
            return fpgjPoints;
        }

        public void setFpgjPoints(List<TariffRulePointResp> fpgjPoints) {
            this.fpgjPoints = fpgjPoints;
        }

        public List<TariffRulePricePreviewResp> getPriceRows() {
            return priceRows;
        }

        public void setPriceRows(List<TariffRulePricePreviewResp> priceRows) {
            this.priceRows = priceRows;
        }

        public TariffRulePreviewResp getPreview() {
            return preview;
        }

        public void setPreview(TariffRulePreviewResp preview) {
            this.preview = preview;
        }
    }
}
