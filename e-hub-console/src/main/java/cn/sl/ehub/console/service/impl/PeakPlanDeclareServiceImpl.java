package cn.sl.ehub.console.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.sl.ehub.console.enums.ResourceTypeEnum;
import cn.sl.ehub.service.mapper.MosPeakBepfDataMapper;
import cn.sl.ehub.service.mapper.MosPeakMpscDataMapper;
import cn.sl.ehub.service.mapper.MosPeakThirdPartyBidDataMapper;
import cn.sl.ehub.console.req.PeakBepfExcelImportReq;
import cn.sl.ehub.console.req.PeakBidDataExcelImportReq;
import cn.sl.ehub.console.req.PeakMpscExcelImportReq;
import cn.sl.ehub.console.req.PeakPlanDeclareImportReq;
import cn.sl.ehub.console.service.IPeakPlanDeclareService;
import cn.sl.ehub.service.vo.MosPeakBepfData;
import cn.sl.ehub.service.vo.MosPeakMpscData;
import cn.sl.ehub.service.vo.MosPeakThirdPartyBidData;
import lombok.extern.slf4j.Slf4j;

/**
 * 调峰计划申报Service实现
 *
 * @author sl
 * @date 2026-05-28
 */
@Slf4j
@Service
public class PeakPlanDeclareServiceImpl implements IPeakPlanDeclareService {

    @Resource
    private MosPeakBepfDataMapper bepfDataMapper;

    @Resource
    private MosPeakMpscDataMapper mpscDataMapper;

    @Resource
    private MosPeakThirdPartyBidDataMapper bidDataMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importPeakPlanDeclare(PeakPlanDeclareImportReq req) {
        try {
            MultipartFile file = req.getFile();
            Date startDate = req.getStartDate();
            Date endDate = req.getEndDate();
            Integer resourceType = req.getResourceType();

            log.info("开始导入调峰计划申报数据，开始日期：{}，结束日期：{}，资源类型：{}，文件名：{}，文件大小：{}",
                    DateFormatUtils.format(startDate, "yyyy-MM-dd"),
                    DateFormatUtils.format(endDate, "yyyy-MM-dd"),
                    resourceType,
                    file != null ? file.getOriginalFilename() : "null",
                    file != null ? file.getSize() : 0);

            // 校验资源类型
            if (resourceType == null) {
                return "资源类型不能为空";
            }

            if (!ResourceTypeEnum.isValid(resourceType)) {
                return "资源类型不支持：" + resourceType + "，当前支持的资源类型为：电采暖(15)、工业负荷(44)";
            }

            // 从请求参数中获取aggregatorId或使用默认值
            String aggregatorId = StringUtils.isNotBlank(req.getAggregatorId()) ? req.getAggregatorId()
                    : "DEFAULT_AGGREGATOR";
            String sourceId = String.valueOf(resourceType); // 资源ID = 资源类型code（电采暖:15, 工业负荷:44）
            log.info("资源类型：{}({})，对应聚合商ID：{}，资源ID：{}",
                    resourceType, ResourceTypeEnum.getNameByCode(resourceType), aggregatorId, sourceId);

            // 校验日期范围
            if (startDate.after(endDate)) {
                return "开始日期不能大于结束日期";
            }

            // 校验文件
            if (file == null || file.isEmpty()) {
                return "Excel文件不能为空";
            }

            // 导入基础用电上报 - Sheet 0
            ImportParams bepfParams = new ImportParams();
            bepfParams.setStartSheetIndex(0);
            bepfParams.setSheetNum(1);
            bepfParams.setHeadRows(2); // 前2行是表头(第1行说明+第2行列名)
            List<PeakBepfExcelImportReq> bepfDataList = ExcelImportUtil.importExcel(
                    file.getInputStream(), PeakBepfExcelImportReq.class, bepfParams);
            log.info("基础用电上报数据条数：{}", bepfDataList == null ? 0 : bepfDataList.size());

            // 导入可调能力上报 - Sheet 1
            ImportParams mpscParams = new ImportParams();
            mpscParams.setStartSheetIndex(1);
            mpscParams.setSheetNum(1);
            mpscParams.setHeadRows(2); // 前2行是表头(第1行说明+第2行列名)
            List<PeakMpscExcelImportReq> mpscDataList = ExcelImportUtil.importExcel(
                    file.getInputStream(), PeakMpscExcelImportReq.class, mpscParams);
            log.info("可调能力上报数据条数：{}", mpscDataList == null ? 0 : mpscDataList.size());

            // 导入日运行指标上报 - Sheet 2
            ImportParams bidParams = new ImportParams();
            bidParams.setStartSheetIndex(2);
            bidParams.setSheetNum(1);
            bidParams.setHeadRows(2); // 前2行是表头(第1行说明+第2行列名)
            List<PeakBidDataExcelImportReq> bidDataList = ExcelImportUtil.importExcel(
                    file.getInputStream(), PeakBidDataExcelImportReq.class, bidParams);
            log.info("日运行指标上报数据条数：{}", bidDataList == null ? 0 : bidDataList.size());

            // 删除旧数据
            log.info("删除旧数据，资源类型：{}，聚合商ID：{}，资源ID：{}，日期范围：{} - {}",
                    resourceType, aggregatorId, sourceId,
                    DateFormatUtils.format(startDate, "yyyy-MM-dd"),
                    DateFormatUtils.format(endDate, "yyyy-MM-dd"));
            bepfDataMapper.deleteByPhyunitIdAndDateRange(aggregatorId, sourceId, startDate, endDate);
            mpscDataMapper.deleteByPhyunitIdAndDateRange(aggregatorId, sourceId, startDate, endDate);
            bidDataMapper.deleteByPhyunitIdAndDateRange(aggregatorId, sourceId, startDate, endDate);

            // 处理基础用电上报数据
            String bepfResult = processBepfData(bepfDataList, aggregatorId, sourceId, startDate, endDate);
            if (!"success".equals(bepfResult)) {
                return "基础用电上报数据处理失败：" + bepfResult;
            }

            // 处理可调能力上报数据
            String mpscResult = processMpscData(mpscDataList, aggregatorId, sourceId, startDate, endDate);
            if (!"success".equals(mpscResult)) {
                return "可调能力上报数据处理失败：" + mpscResult;
            }

            // 日运行指标上报数据
            String bidResult = processBidData(bidDataList, aggregatorId, sourceId, resourceType, startDate, endDate);
            if (!"success".equals(bidResult)) {
                return "日运行指标上报数据处理失败：" + bidResult;
            }

            log.info("调峰计划申报数据导入成功");
            return "success";
        } catch (Exception e) {
            log.error("导入调峰计划申报数据异常", e);
            throw new RuntimeException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 处理基础用电上报数据
     */
    private String processBepfData(List<PeakBepfExcelImportReq> dataList, String aggregatorId, String sourceId,
            Date startDate, Date endDate) throws ParseException {
        if (CollectionUtils.isEmpty(dataList)) {
            return "基础用电上报数据为空，请检查Excel第1个sheet页";
        }

        // 调试日志：打印前10条原始数据
        log.info("====== 基础用电上报Excel原始数据（前10条）======");
        int debugCount = Math.min(10, dataList.size());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");
        for (int i = 0; i < debugCount; i++) {
            PeakBepfExcelImportReq item = dataList.get(i);
            String dateStr = item.getDate() != null ? sdf.format(item.getDate()) : "null";
            String timeStr = item.getTime() != null ? timeSdf.format(item.getTime()) : "null";
            log.info("第{}条: date=[{}], time=[{}], value=[{}]",
                    i + 1, dateStr, timeStr, item.getValue());
        }
        log.info("====== 总共{}条数据 ======", dataList.size());

        // 按日期分组（过滤掉日期为null的记录，避免NullPointerException）
        // 如果点位时间是00:00:00，则将其逻辑日期视为前一天（Excel中24:00通常显示为次日00:00）
        Map<String, List<PeakBepfExcelImportReq>> dateGroupMap = dataList.stream()
                .filter(item -> item.getDate() != null && item.getTime() != null)
                .collect(Collectors.groupingBy(item -> {
                    Calendar c = Calendar.getInstance();
                    c.setTime(item.getTime());
                    if (c.get(Calendar.HOUR_OF_DAY) == 0 && c.get(Calendar.MINUTE) == 0) {
                        c.setTime(item.getDate());
                        c.add(Calendar.DAY_OF_MONTH, -1);
                        return sdf.format(c.getTime());
                    }
                    return sdf.format(item.getDate());
                }));

        // 获取日期范围内的所有日期
        List<String> dateRange = getDateRange(startDate, endDate);

        log.info("基础用电上报数据校验：期望天数={}，实际Excel中的天数={}，日期范围：{} 至 {}",
                dateRange.size(), dateGroupMap.size(), dateRange.get(0), dateRange.get(dateRange.size() - 1));

        // 校验是否所有日期都存在
        for (String dateStr : dateRange) {
            if (!dateGroupMap.containsKey(dateStr)) {
                return "基础用电上报数据缺少日期：" + dateStr + "，请确保日期连续且完整（共需" + dateRange.size() + "天）";
            }
        }

        // 校验每天是否有96点数据
        int totalPoints = 0;
        for (String dateStr : dateRange) {
            List<PeakBepfExcelImportReq> dayData = dateGroupMap.get(dateStr);
            if (CollectionUtils.isEmpty(dayData)) {
                return "日期 " + dateStr + " 的基础用电上报数据为空";
            }
            if (dayData.size() != 96) {
                return "日期 " + dateStr + " 的基础用电上报数据不足96点，实际：" + dayData.size() + "点，缺失：" + (96 - dayData.size()) + "点";
            }

            java.util.Set<String> timeSet = new java.util.HashSet<>();

            for (int i = 0; i < dayData.size(); i++) {
                PeakBepfExcelImportReq item = dayData.get(i);
                String timeKey = item.getTime() != null ? timeSdf.format(item.getTime()) : "null";
                if (!timeSet.add(timeKey)) {
                    return "【基础用电上报-第" + (dateRange.indexOf(dateStr) + 1) + "天】日期 " + dateStr
                            + " 存在重复时间点：" + timeKey + "，请检查是否有重复的96点数据";
                }
                if (item.getValue() == null) {
                    return "【基础用电上报-第" + (dateRange.indexOf(dateStr) + 1) + "天】日期 " + dateStr + " 时间 " + timeKey
                            + " 的基础用电预测值为空，请填写完整数据";
                }
                if (item.getValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
                    return "【基础用电上报-第" + (dateRange.indexOf(dateStr) + 1) + "天】日期 " + dateStr + " 时间 " + timeKey
                            + " 的基础用电预测值为负数：" + item.getValue() + "，请填写正确数据";
                }
            }
            totalPoints += dayData.size();
        }

        log.info("基础用电上报数据校验通过：共{}天，每天96点，总计{}个数据点", dateRange.size(), totalPoints);

        // 转换为实体并保存
        List<MosPeakBepfData> entityList = new ArrayList<>();
        // 过滤掉日期为空的数据
        List<PeakBepfExcelImportReq> validDataList = dataList.stream()
                .filter(item -> item.getDate() != null)
                .collect(Collectors.toList());

        for (PeakBepfExcelImportReq req : validDataList) {
            MosPeakBepfData entity = new MosPeakBepfData();
            entity.setAggregatorId(aggregatorId);
            entity.setSourceId(sourceId);

            // 合并日期和时间
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(req.getDate());

            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(req.getTime());

            // 将时间部分设置到日期上
            dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            dateCal.set(Calendar.SECOND, 0);
            dateCal.set(Calendar.MILLISECOND, 0);

            Date dataTime = dateCal.getTime();
            entity.setDataTime(dataTime);

            // 计算点位序号(从1开始，00:15是第1个点，00:00是第96个点)
            int hour = timeCal.get(Calendar.HOUR_OF_DAY);
            int minute = timeCal.get(Calendar.MINUTE);
            int pointIndex;
            if (hour == 0 && minute == 0) {
                pointIndex = 96;
            } else {
                pointIndex = (hour * 60 + minute) / 15;
            }
            entity.setPointIndex(pointIndex);

            entity.setValue(req.getValue());

            // 生成秒级时间戳
            entity.setTimestamp(dataTime.getTime() / 1000);

            entity.setUpdateTime(new Date());
            entityList.add(entity);
        }

        // 批量保存
        if (CollectionUtils.isNotEmpty(entityList)) {
            bepfDataMapper.batchInsertOrUpdate(entityList);
            log.info("基础用电上报数据保存成功，共{}条", entityList.size());
        }

        return "success";
    }

    /**
     * 处理可调能力上报数据
     */
    private String processMpscData(List<PeakMpscExcelImportReq> dataList, String aggregatorId, String sourceId,
            Date startDate, Date endDate) throws ParseException {
        if (CollectionUtils.isEmpty(dataList)) {
            return "可调能力上报数据为空，请检查Excel第2个sheet页";
        }

        // 按日期分组
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");
        // 处理00:00偏移逻辑
        Map<String, List<PeakMpscExcelImportReq>> dateGroupMap = dataList.stream()
                .filter(item -> item.getDate() != null && item.getTime() != null)
                .collect(Collectors.groupingBy(item -> {
                    Calendar c = Calendar.getInstance();
                    c.setTime(item.getTime());
                    if (c.get(Calendar.HOUR_OF_DAY) == 0 && c.get(Calendar.MINUTE) == 0) {
                        c.setTime(item.getDate());
                        c.add(Calendar.DAY_OF_MONTH, -1);
                        return sdf.format(c.getTime());
                    }
                    return sdf.format(item.getDate());
                }));

        // 获取日期范围内的所有日期
        List<String> dateRange = getDateRange(startDate, endDate);

        log.info("可调能力上报数据校验：期望天数={}，实际Excel中的天数={}，日期范围：{} 至 {}",
                dateRange.size(), dateGroupMap.size(), dateRange.get(0), dateRange.get(dateRange.size() - 1));

        // 校验是否所有日期都存在
        for (String dateStr : dateRange) {
            if (!dateGroupMap.containsKey(dateStr)) {
                return "可调能力上报数据缺少日期：" + dateStr + "，请确保日期连续且完整（共需" + dateRange.size() + "天）";
            }
        }

        // 校验每天是否有96点数据
        int totalPoints = 0;
        for (String dateStr : dateRange) {
            List<PeakMpscExcelImportReq> dayData = dateGroupMap.get(dateStr);
            if (CollectionUtils.isEmpty(dayData)) {
                return "日期 " + dateStr + " 的可调能力上报数据为空";
            }
            if (dayData.size() != 96) {
                return "日期 " + dateStr + " 的可调能力上报数据不足96点，实际：" + dayData.size() + "点，缺失：" + (96 - dayData.size()) + "点";
            }

            java.util.Set<String> timeSet = new java.util.HashSet<>();

            for (int i = 0; i < dayData.size(); i++) {
                PeakMpscExcelImportReq item = dayData.get(i);
                String timeKey = item.getTime() != null ? timeSdf.format(item.getTime()) : "null";
                if (!timeSet.add(timeKey)) {
                    return "【可调能力上报-第" + (dateRange.indexOf(dateStr) + 1) + "天】日期 " + dateStr
                            + " 存在重复时间点：" + timeKey + "，请检查是否有重复的96点数据";
                }
                if (item.getValue() == null) {
                    return "【可调能力上报-第" + (dateRange.indexOf(dateStr) + 1) + "天】日期 " + dateStr + " 时间 " + timeKey
                            + " 的最大调峰能力为空，请填写完整数据";
                }
                if (item.getValue().compareTo(java.math.BigDecimal.ZERO) < 0) {
                    return "【可调能力上报-第" + (dateRange.indexOf(dateStr) + 1) + "天】日期 " + dateStr + " 时间 " + timeKey
                            + " 的最大调峰能力为负数：" + item.getValue() + "，请填写正确数据";
                }
            }
            totalPoints += dayData.size();
        }

        log.info("可调能力上报数据校验通过：共{}天，每天96点，总计{}个数据点", dateRange.size(), totalPoints);

        // 转换为实体并保存
        List<MosPeakMpscData> entityList = new ArrayList<>();
        // 过滤掉日期为空的数据
        List<PeakMpscExcelImportReq> validDataList = dataList.stream()
                .filter(item -> item.getDate() != null)
                .collect(Collectors.toList());

        for (PeakMpscExcelImportReq req : validDataList) {
            MosPeakMpscData entity = new MosPeakMpscData();
            entity.setAggregatorId(aggregatorId);
            entity.setSourceId(sourceId);

            // 合并日期和时间
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(req.getDate());
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(req.getTime());
            dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            dateCal.set(Calendar.SECOND, 0);
            dateCal.set(Calendar.MILLISECOND, 0);
            Date dataTime = dateCal.getTime();
            entity.setDataTime(dataTime);

            // 计算点位序号
            int hour = timeCal.get(Calendar.HOUR_OF_DAY);
            int minute = timeCal.get(Calendar.MINUTE);
            int pointIndex;
            if (hour == 0 && minute == 0) {
                pointIndex = 96;
            } else {
                pointIndex = (hour * 60 + minute) / 15;
            }
            entity.setPointIndex(pointIndex);

            entity.setValue(req.getValue());

            // 生成秒级时间戳
            entity.setTimestamp(dataTime.getTime() / 1000);

            entity.setUpdateTime(new Date());
            entityList.add(entity);
        }

        // 批量保存
        if (CollectionUtils.isNotEmpty(entityList)) {
            mpscDataMapper.batchInsertOrUpdate(entityList);
            log.info("可调能力上报数据保存成功，共{}条", entityList.size());
        }

        return "success";
    }

    /**
     * 处理日运行指标上报数据
     */
    private String processBidData(List<PeakBidDataExcelImportReq> dataList, String aggregatorId, String sourceId,
            Integer resourceType, Date startDate, Date endDate) throws ParseException {
        if (CollectionUtils.isEmpty(dataList)) {
            return "日运行指标上报数据为空，请检查Excel第3个sheet页";
        }

        // 获取日期范围内的所有日期
        List<String> dateRange = getDateRange(startDate, endDate);

        // 按日期分组
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<PeakBidDataExcelImportReq>> dateGroupMap = dataList.stream()
                .filter(item -> item.getDate() != null)
                .collect(Collectors.groupingBy(item -> sdf.format(item.getDate())));

        log.info("日运行指标上报数据校验：期望天数={}，实际Excel中的天数={}，日期范围：{} 至 {}",
                dateRange.size(), dateGroupMap.size(), dateRange.get(0), dateRange.get(dateRange.size() - 1));

        // 校验日期是否连续且完整
        if (dateGroupMap.size() != dateRange.size()) {
            // 找出缺失的日期
            List<String> missingDates = new ArrayList<>();
            for (String dateStr : dateRange) {
                if (!dateGroupMap.containsKey(dateStr)) {
                    missingDates.add(dateStr);
                }
            }
            return "日运行指标上报数据日期不完整，期望：" + dateRange.size() + "天，实际：" + dateGroupMap.size()
                    + "天，缺失日期：" + String.join(", ", missingDates);
        }

        // 再次校验每个日期都存在
        for (String dateStr : dateRange) {
            if (!dateGroupMap.containsKey(dateStr)) {
                return "日运行指标上报数据缺少日期：" + dateStr + "，请确保日期连续且完整";
            }

            List<PeakBidDataExcelImportReq> dayDataList = dateGroupMap.get(dateStr);
            if (dayDataList.size() > 1) {
                return "日运行指标上报数据日期 " + dateStr + " 存在重复记录，每个日期只能有1条数据";
            }
        }

        log.info("日运行指标上报数据校验通过：共{}天，每天1条记录", dateRange.size());

        // 转换为实体并保存
        List<MosPeakThirdPartyBidData> entityList = new ArrayList<>();
        // 过滤掉日期为空的数据
        List<PeakBidDataExcelImportReq> validDataList = dataList.stream()
                .filter(item -> item.getDate() != null)
                .collect(Collectors.toList());

        boolean nonStorageResource = resourceType != null
                && (ResourceTypeEnum.ELECTRIC_HEATING.getCode().equals(resourceType)
                        || ResourceTypeEnum.INDUSTRIAL_LOAD.getCode().equals(resourceType));

        for (PeakBidDataExcelImportReq req : validDataList) {
            MosPeakThirdPartyBidData entity = new MosPeakThirdPartyBidData();
            entity.setAggregatorId(aggregatorId);
            entity.setSourceId(sourceId);

            // 解析日期
            Calendar cal = Calendar.getInstance();
            cal.setTime(req.getDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            entity.setDataTime(cal.getTime());

            java.math.BigDecimal bidPrice = req.getBidPrice();
            java.math.BigDecimal maxInPower = req.getMaxInPower();
            java.math.BigDecimal maxOutPower = req.getMaxOutPower();
            java.math.BigDecimal maxInTimes = req.getMaxInTimes();
            java.math.BigDecimal maxOutTimes = req.getMaxOutTimes();
            java.math.BigDecimal inRate = req.getInRate();
            java.math.BigDecimal outRate = req.getOutRate();
            java.math.BigDecimal soc = req.getSoc();

            if (nonStorageResource) {
                maxInPower = java.math.BigDecimal.ZERO;
                maxInTimes = java.math.BigDecimal.ZERO;
                soc = java.math.BigDecimal.ZERO;
            }

            java.math.BigDecimal zero = java.math.BigDecimal.ZERO;

            entity.setBidPrice(bidPrice == null || bidPrice.compareTo(zero) < 0 ? zero : bidPrice);
            entity.setMaxInPower(maxInPower == null || maxInPower.compareTo(zero) < 0 ? zero : maxInPower);
            entity.setMaxOutPower(maxOutPower == null || maxOutPower.compareTo(zero) < 0 ? zero : maxOutPower);
            entity.setMaxInTimes(maxInTimes == null || maxInTimes.compareTo(zero) < 0 ? zero : maxInTimes);
            entity.setMaxOutTimes(maxOutTimes == null || maxOutTimes.compareTo(zero) < 0 ? zero : maxOutTimes);
            entity.setInRate(inRate == null || inRate.compareTo(zero) < 0 ? zero : inRate);
            entity.setOutRate(outRate == null || outRate.compareTo(zero) < 0 ? zero : outRate);
            entity.setSoc(soc == null || soc.compareTo(zero) < 0 ? zero : soc);
            entity.setValue2(req.getValue2());
            entity.setValue3(req.getValue3());
            entity.setValue4(req.getValue4());
            entity.setUpdateTime(new Date());

            entityList.add(entity);
        }

        // 批量保存
        if (CollectionUtils.isNotEmpty(entityList)) {
            bidDataMapper.batchInsertOrUpdate(entityList);
            log.info("日运行指标上报数据保存成功，共{}条", entityList.size());
        }

        return "success";
    }

    /**
     * 获取日期范围内的所有日期
     */
    private List<String> getDateRange(Date startDate, Date endDate) {
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        while (!cal.getTime().after(endDate)) {
            dateList.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dateList;
    }
}
