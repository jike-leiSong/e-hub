package cn.sl.ehub.upstream.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.sl.ehub.upstream.config.ClientConfig;
import cn.sl.ehub.service.mapper.MosPeakBepfDataMapper;
import cn.sl.ehub.service.mapper.MosPeakMpscDataMapper;
import cn.sl.ehub.service.mapper.MosPeakThirdPartyBidDataMapper;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.service.vo.MosPeakBepfData;
import cn.sl.ehub.service.vo.MosPeakMpscData;
import cn.sl.ehub.service.vo.MosPeakThirdPartyBidData;
import cn.sl.ehub.service.vo.PeakPlanDeliveryLog;
import cn.sl.ehub.upstream.ws.Greeter;
import lombok.extern.slf4j.Slf4j;

/**
 * 调峰计划申报电网上送Service
 *
 * @author sl
 * @date 2026-05-28
 */
@Slf4j
@Service
public class PeakPlanDeliveryService {

    @Resource
    private MosPeakBepfDataMapper bepfDataMapper;

    @Resource
    private MosPeakMpscDataMapper mpscDataMapper;

    @Resource
    private MosPeakThirdPartyBidDataMapper bidDataMapper;

    @Resource
    private ClientConfig clientConfig;

    @Resource
    private PeakPlanDeliveryLogService peakPlanDeliveryLogService;

    @Resource
    private QueryService queryService;

    @Value("${nari.url.peakPlan}")
    private List<String> gridDeliveryUrl;

    /**
     * 上送96点数据到电网（包括BEPF和MPSC）
     *
     * @param aggregatorId 聚合商ID
     * @param sourceId     资源ID
     * @param dataDate     数据日期
     * @return 上送结果
     */
    public String delivery96PointData(String aggregatorId, String sourceId, Date dataDate) {
        try {
            log.info("开始上送96点数据到电网，聚合商ID：{}，资源ID：{}，数据日期：{}", aggregatorId, sourceId, dataDate);

            // 截断日期到凌晨0点，确保范围查询准确 (> 00:00 AND <= 次日 00:00)
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date truncatedDate = cal.getTime();

            // 查询BEPF数据
            List<MosPeakBepfData> bepfDataList = bepfDataMapper.selectByPhyunitIdAndDate(aggregatorId, sourceId,
                    truncatedDate);
            if (CollectionUtils.isEmpty(bepfDataList) || bepfDataList.size() != 96) {
                String error = "BEPF数据不足96点，实际：" + (bepfDataList == null ? 0 : bepfDataList.size());
                log.error("聚合商{}、资源{}：{}", aggregatorId, sourceId, error);
                addNoDataLog("96POINT", "declare", aggregatorId, sourceId, dataDate, error);
                return error;
            }

            // 查询MPSC数据
            List<MosPeakMpscData> mpscDataList = mpscDataMapper.selectByPhyunitIdAndDate(aggregatorId, sourceId,
                    truncatedDate);
            if (CollectionUtils.isEmpty(mpscDataList) || mpscDataList.size() != 96) {
                String error = "MPSC数据不足96点，实际：" + (mpscDataList == null ? 0 : mpscDataList.size());
                log.error("聚合商{}、资源{}：{}", aggregatorId, sourceId, error);
                addNoDataLog("96POINT", "declare", aggregatorId, sourceId, dataDate, error);
                return error;
            }

            // 构建上送数据
            JSONArray dataArray = new JSONArray();

            java.math.RoundingMode roundingMode = java.math.RoundingMode.HALF_UP;
            java.text.DecimalFormat twoDecimalFormat = new java.text.DecimalFormat("0.00");

            // 构建BEPF数据对象（组号格式：BEPF-资源ID-点位序号）
            JSONObject bepfObject = new JSONObject(new LinkedHashMap<>());
            for (int i = 0; i < bepfDataList.size(); i++) {
                MosPeakBepfData data = bepfDataList.get(i);
                // 组号：BEPF-15-1, BEPF-15-2, BEPF-44-1 等
                String key = "BEPF-" + sourceId + "-" + (i + 1);
                java.math.BigDecimal bepfValue = data.getValue() == null ? java.math.BigDecimal.ZERO : data.getValue();
                String value = twoDecimalFormat
                        .format(bepfValue.setScale(2, roundingMode))
                        + ":" + (data.getDataTime().getTime() / 1000);
                bepfObject.put(key, value);
            }
            dataArray.add(bepfObject);

            // 构建MPSC数据对象（组号格式：MPSC-资源ID-点位序号）
            JSONObject mpscObject = new JSONObject(new LinkedHashMap<>());
            for (int i = 0; i < mpscDataList.size(); i++) {
                MosPeakMpscData data = mpscDataList.get(i);
                // 组号：MPSC-15-1, MPSC-15-2, MPSC-44-1 等
                String key = "MPSC-" + sourceId + "-" + (i + 1);
                java.math.BigDecimal mpscValue = data.getValue() == null ? java.math.BigDecimal.ZERO : data.getValue();
                String value = twoDecimalFormat
                        .format(mpscValue.setScale(2, roundingMode))
                        + ":" + (data.getDataTime().getTime() / 1000);
                mpscObject.put(key, value);
            }
            dataArray.add(mpscObject);

            String cmdData = dataArray.toJSONString();
            log.info("96点数据上送内容：{}", cmdData);

            // 调用电网接口上送数据
            String response = deliveryToGrid("declare", cmdData, aggregatorId, sourceId, "96POINT", dataDate);

            return response;
        } catch (Exception e) {
            log.error("上送96点数据到电网异常", e);
            return "失败：" + e.getMessage();
        }
    }

    /**
     * 上送日数据到电网
     *
     * @param aggregatorId 聚合商ID
     * @param sourceId     资源ID
     * @param dataDate     数据日期（次日）
     * @return 上送结果
     */
    public String deliveryDailyData(String aggregatorId, String sourceId, Date dataDate) {
        try {
            log.info("开始上送日数据到电网，聚合商ID：{}，资源ID：{}，数据日期：{}", aggregatorId, sourceId, dataDate);

            // 截断日期到凌晨0点
            Calendar cal = Calendar.getInstance();
            cal.setTime(dataDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date truncatedDate = cal.getTime();

            // 查询日数据
            List<MosPeakThirdPartyBidData> bidDataList = bidDataMapper.selectByPhyunitIdAndDateRange(
                    aggregatorId, sourceId, truncatedDate, truncatedDate);

            if (CollectionUtils.isEmpty(bidDataList)) {
                String error = "日数据为空";
                log.error("聚合商{}、资源{}：{}", aggregatorId, sourceId, error);
                addNoDataLog("DAILY", "declareBid", aggregatorId, sourceId, dataDate, error);
                return error;
            }

            java.math.RoundingMode roundingMode = java.math.RoundingMode.HALF_UP;
            java.text.DecimalFormat twoDecimalFormat = new java.text.DecimalFormat("0.00");

            // 构建上送数据
            JSONArray dataArray = new JSONArray();
            for (MosPeakThirdPartyBidData data : bidDataList) {
                JSONObject dataObject = new JSONObject(new LinkedHashMap<>());
                // GROUP_NUM使用资源ID（sourceId）
                dataObject.put("GROUP_NUM", sourceId);
                dataObject.put("DATA_TIME", String.valueOf(data.getDataTime().getTime() / 1000));
                java.math.BigDecimal zero = java.math.BigDecimal.ZERO;
                java.math.BigDecimal bidPrice = data.getBidPrice() == null ? zero : data.getBidPrice();
                java.math.BigDecimal maxInPower = data.getMaxInPower() == null ? zero : data.getMaxInPower();
                java.math.BigDecimal maxOutPower = data.getMaxOutPower() == null ? zero : data.getMaxOutPower();
                java.math.BigDecimal maxInTimes = data.getMaxInTimes() == null ? zero : data.getMaxInTimes();
                java.math.BigDecimal maxOutTimes = data.getMaxOutTimes() == null ? zero : data.getMaxOutTimes();
                java.math.BigDecimal inRate = data.getInRate() == null ? zero : data.getInRate();
                java.math.BigDecimal outRate = data.getOutRate() == null ? zero : data.getOutRate();
                java.math.BigDecimal soc = data.getSoc() == null ? zero : data.getSoc();

                dataObject.put("BID_PRICE",
                        twoDecimalFormat.format(bidPrice.setScale(2, roundingMode)));

                dataObject.put("MAX_IN_POWER",
                        twoDecimalFormat.format(maxInPower.setScale(2, roundingMode)));
                dataObject.put("MAX_OUT_POWER",
                        twoDecimalFormat.format(maxOutPower.setScale(2, roundingMode)));

                dataObject.put("MAX_IN_TIMES",
                        maxInTimes.stripTrailingZeros().toPlainString());
                dataObject.put("MAX_OUT_TIMES",
                        maxOutTimes.stripTrailingZeros().toPlainString());

                dataObject.put("IN_RATE",
                        twoDecimalFormat.format(inRate.setScale(2, roundingMode)));
                dataObject.put("OUT_RATE",
                        twoDecimalFormat.format(outRate.setScale(2, roundingMode)));

                dataObject.put("SOC",
                        twoDecimalFormat.format(soc.setScale(2, roundingMode)));
                dataArray.add(dataObject);
            }

            String cmdData = dataArray.toJSONString();
            log.info("日数据上送内容：{}", cmdData);

            // 调用电网接口上送数据
            String response = deliveryToGrid("declareBid", cmdData, aggregatorId, sourceId, "DAILY", dataDate);

            return response;
        } catch (Exception e) {
            log.error("上送日数据到电网异常", e);
            return "失败：" + e.getMessage();
        }
    }

    /**
     * 上送数据到电网
     *
     * @param method       方法名（declare或chandleBid）
     * @param cmdData      数据内容
     * @param aggregatorId 聚合商ID
     * @param sourceId     资源ID
     * @param dataType     数据类型（96POINT或DAILY）
     * @param dataDate     数据日期
     * @return 上送结果
     */
    private String deliveryToGrid(String method, String cmdData, String aggregatorId, String sourceId, String dataType,
            Date dataDate) {
        String response = null;
        try {
            log.info("开始上送{}到电网，聚合商ID：{}，资源ID：{}，方法：{}", dataType, aggregatorId, sourceId, method);

            // 使用总加上报的WebService服务
            Greeter greeter = clientConfig.greeter(gridDeliveryUrl);

            if ("declare".equals(method)) {
                response = greeter.declare(cmdData);
            } else if ("declareBid".equals(method)) {
                response = greeter.declareBid(cmdData);
            } else {
                response = "失败：未知的方法名：" + method;
            }

            log.info("上送{}到电网成功，聚合商ID：{}，资源ID：{}，响应：{}", dataType, aggregatorId, sourceId, response);
        } catch (Exception e) {
            log.error("上送{}到电网失败，聚合商ID：{}，资源ID：{}，异常信息：{}", dataType, aggregatorId, sourceId,
                    ExceptionUtils.getMessage(e.getCause()));
            e.printStackTrace();
            response = "失败：" + e.getMessage();
        } finally {
            // 记录上送日志到专用的调峰计划申报日志表
            try {
                PeakPlanDeliveryLog deliveryLog = new PeakPlanDeliveryLog();
                deliveryLog.setAggregatorId(aggregatorId);
                deliveryLog.setSourceId(sourceId);
                deliveryLog.setDataType(dataType);
                deliveryLog.setMethod(method);
                deliveryLog.setDeliveryData(cmdData);
                deliveryLog.setDeliveryStatus(response);
                deliveryLog.setDataDate(dataDate);
                deliveryLog.setCreateTime(new Date());
                deliveryLog.setRemark("调峰计划申报-" + ("96POINT".equals(dataType) ? "96点数据" : "日数据") +
                        "，聚合商ID：" + aggregatorId + "，资源ID：" + sourceId);

                peakPlanDeliveryLogService.addLog(deliveryLog);
                log.info("上送{}日志已记录到peak_plan_delivery_log表，聚合商ID：{}，资源ID：{}", dataType, aggregatorId, sourceId);
            } catch (Exception e) {
                log.error("记录上送日志到数据库失败：{}", ExceptionUtils.getStackTrace(e));
            }
        }
        return response;
    }

    private void addNoDataLog(String dataType, String method, String aggregatorId, String sourceId, Date dataDate,
            String deliveryStatus) {
        try {
            PeakPlanDeliveryLog deliveryLog = new PeakPlanDeliveryLog();
            deliveryLog.setAggregatorId(aggregatorId);
            deliveryLog.setSourceId(sourceId);
            deliveryLog.setDataType(dataType);
            deliveryLog.setMethod(method);
            deliveryLog.setDeliveryData(null);
            deliveryLog.setDeliveryStatus(deliveryStatus);
            deliveryLog.setDataDate(dataDate);
            deliveryLog.setCreateTime(new Date());
            deliveryLog.setRemark("无数据");
            peakPlanDeliveryLogService.addLog(deliveryLog);
            log.info("上送{}无数据日志已记录到peak_plan_delivery_log表，聚合商ID：{}，资源ID：{}", dataType, aggregatorId, sourceId);
        } catch (Exception e) {
            log.error("记录无数据上送日志到数据库失败：{}", ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 执行96点数据上送任务（基础用电上报+可调能力上报）
     * 参考总加上送实现，只传aggregatorId，自动查询该聚合商下的所有资源并上送次日数据
     *
     * @param aggregatorId 聚合商ID
     * @return 执行结果
     */
    public boolean execute96PointDelivery(String aggregatorId) {
        return execute96PointDeliveryByDate(aggregatorId, null, null);
    }

    /**
     * 执行日数据上送任务（日运行指标上报）
     * 参考总加上送实现，只传aggregatorId，自动查询该聚合商下的所有资源并上送次日数据
     *
     * @param aggregatorId 聚合商ID
     * @return 执行结果
     */
    public boolean executeDailyDataDelivery(String aggregatorId) {
        return executeDailyDataDeliveryByDate(aggregatorId, null, null);
    }

    public boolean execute96PointDeliveryByDate(String aggregatorId, Date dataDate, String resourceTypeId) {
        try {
            log.info("开始执行调峰计划申报96点数据指定日期上送任务，聚合商ID：{}，数据日期：{}", aggregatorId, dataDate);

            if (StringUtils.isBlank(aggregatorId)) {
                log.warn("聚合商ID为空，无法执行上送任务");
                return false;
            }

            List<AggregatorResourceType> aggregatorResourceTypeList = queryService
                    .getAggregatorResourceTypeListByAggregatorId(aggregatorId);

            if (StringUtils.isNotBlank(resourceTypeId)) {
                aggregatorResourceTypeList.removeIf(
                        resourceType -> resourceType == null || !resourceTypeId.equals(resourceType.getId()));
            }

            if (CollectionUtils.isEmpty(aggregatorResourceTypeList)) {
                log.warn("聚合商{}下没有资源类型，无需上送", aggregatorId);
                return true;
            }

            log.info("聚合商{}下共有{}个资源类型，开始遍历上送", aggregatorId, aggregatorResourceTypeList.size());

            Date targetDate = dataDate;
            if (targetDate == null) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 1);
                targetDate = cal.getTime();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            log.info("上送指定日期96点数据，日期：{}", sdf.format(targetDate));

            boolean allSuccess = true;
            for (AggregatorResourceType resourceType : aggregatorResourceTypeList) {
                String sourceId = resourceType.getId();
                try {
                    log.info("上送聚合商{}、资源{}的96点数据，日期：{}", aggregatorId, sourceId, sdf.format(targetDate));
                    String result96 = delivery96PointData(aggregatorId, sourceId, targetDate);
                    if (!result96.contains("成功") && !result96.contains("success")) {
                        log.error("聚合商{}、资源{}上送96点数据失败：{}", aggregatorId, sourceId, result96);
                        allSuccess = false;
                    }
                } catch (Exception e) {
                    log.error("聚合商{}、资源{}上送96点数据异常", aggregatorId, sourceId, e);
                    allSuccess = false;
                }
            }

            log.info("调峰计划申报96点数据指定日期上送任务执行完成，聚合商：{}，结果：{}", aggregatorId,
                    allSuccess ? "成功" : "部分失败");
            return allSuccess;
        } catch (Exception e) {
            log.error("执行调峰计划申报96点数据指定日期上送任务异常，聚合商：{}", aggregatorId, e);
            return false;
        }
    }

    public boolean executeDailyDataDeliveryByDate(String aggregatorId, Date dataDate, String resourceTypeId) {
        try {
            log.info("开始执行调峰计划申报日数据指定日期上送任务，聚合商ID：{}，数据日期：{}", aggregatorId, dataDate);

            if (StringUtils.isBlank(aggregatorId)) {
                log.warn("聚合商ID为空，无法执行上送任务");
                return false;
            }

            List<AggregatorResourceType> aggregatorResourceTypeList = queryService
                    .getAggregatorResourceTypeListByAggregatorId(aggregatorId);

            if (StringUtils.isNotBlank(resourceTypeId)) {
                aggregatorResourceTypeList.removeIf(
                        resourceType -> resourceType == null || !resourceTypeId.equals(resourceType.getId()));
            }

            if (CollectionUtils.isEmpty(aggregatorResourceTypeList)) {
                log.warn("聚合商{}下没有资源类型，无需上送", aggregatorId);
                return true;
            }

            log.info("聚合商{}下共有{}个资源类型，开始遍历上送", aggregatorId, aggregatorResourceTypeList.size());

            Date targetDate = dataDate;
            if (targetDate == null) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 1);
                targetDate = cal.getTime();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            log.info("上送指定日期日数据，日期：{}", sdf.format(targetDate));

            boolean allSuccess = true;
            for (AggregatorResourceType resourceType : aggregatorResourceTypeList) {
                String sourceId = resourceType.getId();
                try {
                    log.info("上送聚合商{}、资源{}的日数据，日期：{}", aggregatorId, sourceId, sdf.format(targetDate));
                    String resultDaily = deliveryDailyData(aggregatorId, sourceId, targetDate);
                    if (!resultDaily.contains("成功") && !resultDaily.contains("success")) {
                        log.error("聚合商{}、资源{}上送日数据失败：{}", aggregatorId, sourceId, resultDaily);
                        allSuccess = false;
                    }
                } catch (Exception e) {
                    log.error("聚合商{}、资源{}上送日数据异常", aggregatorId, sourceId, e);
                    allSuccess = false;
                }
            }

            log.info("调峰计划申报日数据指定日期上送任务执行完成，聚合商：{}，结果：{}", aggregatorId,
                    allSuccess ? "成功" : "部分失败");
            return allSuccess;
        } catch (Exception e) {
            log.error("执行调峰计划申报日数据指定日期上送任务异常，聚合商：{}", aggregatorId, e);
            return false;
        }
    }

}
