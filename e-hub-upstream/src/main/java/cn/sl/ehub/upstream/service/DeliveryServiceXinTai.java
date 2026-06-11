package cn.sl.ehub.upstream.service;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.sl.ehub.upstream.config.ClientConfig;
import cn.sl.ehub.upstream.dto.BigDataHistoryResp;
import cn.sl.ehub.upstream.dto.BigDataRealTimeResp;
import cn.sl.ehub.upstream.dto.HistoryReq;
import cn.sl.ehub.upstream.dto.OpentsdbReq;
import cn.sl.ehub.upstream.dto.RealTimeReq;
import cn.sl.ehub.upstream.dto.TagVO;
import cn.sl.ehub.common.enums.EnergyModelEnumNew;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.enums.TemplateNameNewEnum;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.req.SingleMeasDeliveryReq;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.common.utils.RedisUtil;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorInfo;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import cn.sl.ehub.service.vo.ControlIssueLog;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.SingleMeasDeliveryLog;
import cn.sl.ehub.service.vo.TotalDeliveryLog;
import cn.sl.ehub.upstream.ws.Greeter;
import cn.sl.ehub.service.service.AggregatorEntDeviceService;
import cn.sl.ehub.service.service.TotalDeliveryLogService;
import cn.sl.ehub.service.service.ControlIssueLogService;
import cn.sl.ehub.service.service.SingleMeasDeliveryLogService;
import cn.sl.ehub.service.service.AggregatorEntService;
import cn.sl.ehub.service.service.AggregatorSingleModelDataService;
import cn.sl.ehub.upstream.service.BigDataHandlerService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author sl
 * @Date 2026-05-28
 **/

@Service
@Slf4j
public class DeliveryServiceXinTai {

    private static final String STATE_GRID_BEIJING = "BEIJING";
    private static final String STATE_GRID_TIANJIN = "TIANJIN";
    private static final String STATE_GRID_HEBEI = "HEBEI";
    private static final String STATE_GRID_SHANXI = "SHANXI";
    private static final String STATE_GRID_SHANDONG = "SHANDONG";
    private static final String STATE_GRID_JIBEI = "JIBEI";

    private static final String DATE_TIME_MIN = "yyyy-MM-dd-HH-mm";
    private static final String ONE_MIN_LAST_NONE = "1m-last-none";
    private static final String ONE_MIN_LAST_NULL = "1m-last-null";

    private static final String DATE_FORMATTER_SEC = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMATTER_MIN = "yyyy-MM-dd HH:mm:00";
    private static final String DATE_FORMATTER_MIN_LAST = "yyyy-MM-dd HH:mm:59";
    private static final String DATE_FORMATTER_FILE_NAME = "yyyy-MM-dd-HH-mm";

    @Resource
    private BigDataHandlerService bigDataHandlerService;

    @Autowired
    private QueryService queryService;
    @Resource
    private AggregatorEntDeviceService aggregatorEntDeviceService;

    @Resource
    private ClientConfig clientConfig;

    @Resource
    private HuabeiUrlService huabeiUrlService;

    @Value("${nari.url.total}")
    private List<String> totalAndDeliveryUrl;

    @Resource
    private TotalDeliveryLogService totalDeliveryLogService;

    @Resource
    private ControlIssueLogService controlIssueLogService;

    @Value("${delivery.max.retries:1}")
    private int maxRetries;

    @Value("${delivery.timeout.millis:58000}")
    private long deliveryTimeoutMillis; // 上送超时时间（毫秒），默认55秒，留5秒缓冲时间

    /**
     * 大数据实时查询有效性校验间隔秒数，默认5分钟-300秒
     */
    @Value("${bigdata.realtime.interval:300}")
    private int bigdataRealtimeInterval;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SingleMeasDeliveryLogService singleMeasDeliveryLogService;

    @Value("${nari.url.single}")
    private List<String> singleModelAndMeasUrl;
    @Resource
    private FreemarkerService freemarkerService;

    @Value("${file.separate}")
    private String fileSeparate;
    @Value("${file.point}")
    private String filePoint;

    @Value("${file.suffix}")
    private String fileSuffix;
    @Value("${device.no.up.data}")
    private String noUpDeviceStationIds;
    @Value("${model.no.up.data}")
    private String noUpModelEnergyStationCode;

    @Resource
    private AggregatorEntService aggregatorEntService;
    @Resource
    private AggregatorSingleModelDataService aggregatorSingleModelDataService;
    @Qualifier("pvsBusinessThreadPool")
    @Autowired
    private ExecutorService businessExecutorService;

    public ResultVO<String> totalDataDelivery(String aggregatorId) {
        ResultVO<String> resultVO = ResultVO.success();
        List<AggregatorResourceType> aggregatorResourceTypeListByAggregatorId = queryService.getAggregatorResourceTypeListByAggregatorId(aggregatorId);
//       资源id-资源类型map
        Map<String, String> resourTypeAndNameMap = aggregatorResourceTypeListByAggregatorId.stream().collect(Collectors.toMap(AggregatorResourceType::getId, AggregatorResourceType::getName));
//        获取聚合商数量
        // modify by sl 2024-10-24 不上送设备站
        List<String> stationIds = Arrays.asList(noUpDeviceStationIds.split(","));
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineEntDeviceListByAggregatorId(aggregatorId, stationIds);
//        key为资源类型id
        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));
        if (MapUtil.isEmpty(configMapByResourceType)) {
            return resultVO;
        }

        StringBuilder allResponseMsg = new StringBuilder();
        Map<String, String> resourTypeAndCodeMap = EnergyModelEnumNew.getEnergyMap();

        // 分别收集电采暖和工业负荷的所有资源类型，各自独立并行处理
        List<Future<String>> ehFutures = new ArrayList<>();
        List<Future<String>> vppFutures = new ArrayList<>();

        for (Map.Entry<String, List<AggregatorEntDevice>> entry : configMapByResourceType.entrySet()) {
            String resourceId = entry.getKey();
            String resourType = resourTypeAndNameMap.get(resourceId);
            String resourceCode = resourTypeAndCodeMap.get(resourType);
            List<AggregatorEntDevice> configs = entry.getValue();

            if (StrUtil.equals(resourType, EnergyModelEnumNew.ELECTRIC_HEATING.getName())) {
                // 电采暖并行处理（独立，不等待其他资源）
                Future<String> ehFuture = businessExecutorService.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return processElectricHeating(aggregatorId, resourceId, resourceCode, configs, resourTypeAndCodeMap);
                    }
                });
                ehFutures.add(ehFuture);
            } else if (StrUtil.equals(resourType, EnergyModelEnumNew.INDUSTRIAL_LOAD.getName())) {
                // 工业负荷并行处理（独立，不等待其他资源）
                Future<String> vppFuture = businessExecutorService.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return processIndustrialLoad(aggregatorId, resourceId, resourceCode, configs, resourTypeAndCodeMap);
                    }
                });
                vppFutures.add(vppFuture);
            }
        }

        // 电采暖和工业负荷完全并行，各自完成各自的，不互相等待
        // 合并所有Future，统一处理（它们已经在并行执行了）
        List<Future<String>> allFutures = new ArrayList<>();
        allFutures.addAll(ehFutures);
        allFutures.addAll(vppFutures);

        // 记录开始时间，确保所有任务在1分钟内完成
        long startTime = System.currentTimeMillis();
        long timeoutMillis = deliveryTimeoutMillis; // 从配置中心获取超时时间

        // 遍历所有Future，哪个先完成就先处理哪个，不互相等待
        for (Future<String> future : allFutures) {
            try {
                // 计算剩余时间
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTime = timeoutMillis - elapsedTime;

                if (remainingTime <= 0) {
                    log.error("并行处理数据上送超时，已用时: {}ms，超过55秒限制", elapsedTime);
                    allResponseMsg.append("处理超时:超过1分钟限制; ");
                    future.cancel(true); // 取消任务
                    continue;
                }

                // future.get() 会等待该任务完成，但不同任务之间是并行的
                // 如果电采暖先完成，就先处理电采暖；如果工业负荷先完成，就先处理工业负荷
                // 添加超时控制，确保在1分钟内完成
                String response = future.get(remainingTime, TimeUnit.MILLISECONDS);
                if (StringUtils.isNotBlank(response)) {
                    allResponseMsg.append(response).append("; ");
                }
            } catch (TimeoutException e) {
                log.error("并行处理数据上送超时: {}", ExceptionUtils.getStackTrace(e));
                allResponseMsg.append("处理超时:超过1分钟限制; ");
            } catch (Exception e) {
                log.error("并行处理数据上送异常: {}", ExceptionUtils.getStackTrace(e));
                allResponseMsg.append("处理异常:").append(e.getMessage()).append("; ");
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("总加数据上送总耗时: {}ms ({}秒)", totalTime, totalTime / 1000);

        resultVO.setData(allResponseMsg.toString());
        return resultVO;
    }

    /**
     * 处理电采暖数据
     */
    private String processElectricHeating(String aggregatorId, String resourceId, String resourceCode,
                                          List<AggregatorEntDevice> ehConfigs, Map<String, String> resourTypeAndCodeMap) {
        log.info("================== 开始处理电采暖数据 ==================");
        log.info("电采暖-聚合商ID: {}, 资源类型ID: {}", aggregatorId, resourceId);

        String keyEHTotal = "loadAggregatorDelivery"+aggregatorId + ":" + EnergyModelEnumNew.ELECTRIC_HEATING.getCode() + ":total";
        log.info("电采暖-设备数量: {}", ehConfigs.size());

        RealTimeReq totalRealTimeParam = getParamTotal(ehConfigs);
        List<BigDataRealTimeResp> pRealTime = null;
        try {
            pRealTime = bigDataHandlerService.getRealTime(totalRealTimeParam, "0");
            log.info("电采暖-查询实时数据成功，数据条数: {}", CollectionUtils.isEmpty(pRealTime) ? 0 : pRealTime.size());
            // 查询成功且非空，按【聚合商企业id:能源编码】写入缓存,有效期五分钟
            if (CollectionUtils.isNotEmpty(pRealTime)) {
                redisUtil.set(keyEHTotal, pRealTime, bigdataRealtimeInterval);
            }
        } catch (Exception e) {
            log.warn("电采暖-查询实时总加数据异常:参数{}", JSONObject.toJSONString(totalRealTimeParam));
            log.warn("电采暖-查询实时总加数据异常:异常信息{}", ExceptionUtils.getStackTrace(e));
            Object totalRedis = redisUtil.get(keyEHTotal);
            if(Objects.isNull(totalRedis)){
                pRealTime = new ArrayList<>();
                log.warn("电采暖-从缓存获取总加最新值为空");
            }else{
                pRealTime = (List<BigDataRealTimeResp>) redisUtil.get(keyEHTotal);
                log.info("电采暖-从缓存获取总加最新值成功，数据条数: {}", pRealTime.size());
            }
        }

        //总加功率计算
        Map<String, String> ehCmdData = processEHCmdData(ehConfigs, resourceId, pRealTime);
        log.info("电采暖-总加数据处理完成，上送数据: {}", JSONObject.toJSONString(ehCmdData));

        // 上送电采暖数据到电网（带重试）
        String ehResponse = deliveryEHDataToGridWithRetry(ehCmdData, aggregatorId, resourceId);
        String result = "电采暖:" + ehResponse;

        //量测推送判断
        LocalDateTime now = LocalDateTime.now();
        log.info("电采暖-当前时间: {}", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (now.getMinute() % 15 == 0) {
            log.info("电采暖-触发单体量测上送定时任务");
            //量测数据推送（带重试）
            String measResponse = deliveryMeasDataEHWithRetry(ehConfigs, resourceId, resourceCode, aggregatorId, pRealTime);
            log.info("电采暖-量测数据上送结果: {}", measResponse);
            result += " 量测:" + measResponse;
        }
        log.info("================== 电采暖数据处理完成 ==================");
        return result;
    }

    /**
     * 处理工业负荷数据
     */
    private String processIndustrialLoad(String aggregatorId, String resourceId, String resourceCode,
                                        List<AggregatorEntDevice> vppConfigs, Map<String, String> resourTypeAndCodeMap) {
        log.info("================== 开始处理工业负荷数据 ==================");
        log.info("工业负荷-聚合商ID: {}, 资源类型ID: {}", aggregatorId, resourceId);

        String keyVPPTotal = "loadAggregatorDelivery"+aggregatorId + ":" + EnergyModelEnumNew.INDUSTRIAL_LOAD.getCode() + ":total";
        log.info("工业负荷-设备数量: {}", vppConfigs.size());

        RealTimeReq totalRealTimeParam = getParamTotal(vppConfigs);
        List<BigDataRealTimeResp> pRealTime = null;
        try {
            pRealTime = bigDataHandlerService.getRealTime(totalRealTimeParam, "0");
            log.info("工业负荷-查询实时数据成功，数据条数: {}", CollectionUtils.isEmpty(pRealTime) ? 0 : pRealTime.size());
            // 查询成功且非空，按【聚合商企业id:能源编码】写入缓存,有效期五分钟
            if (CollectionUtils.isNotEmpty(pRealTime)) {
                redisUtil.set(keyVPPTotal, pRealTime, bigdataRealtimeInterval);
            }
        } catch (Exception e) {
            log.warn("工业负荷-查询实时总加数据异常:参数{}", JSONObject.toJSONString(totalRealTimeParam));
            log.warn("工业负荷-查询实时总加数据异常:异常信息{}", ExceptionUtils.getStackTrace(e));
            Object totalRedis = redisUtil.get(keyVPPTotal);
            if(Objects.isNull(totalRedis)){
                pRealTime = new ArrayList<>();
                log.warn("工业负荷-从缓存获取总加最新值为空");
            }else{
                pRealTime = (List<BigDataRealTimeResp>) redisUtil.get(keyVPPTotal);
                log.info("工业负荷-从缓存获取总加最新值成功，数据条数: {}", pRealTime.size());
            }
        }

        //总加功率计算
        Map<String, String> vppCmdData = processVPPCmdData(vppConfigs, resourceId, pRealTime);
        log.info("工业负荷-总加数据处理完成，上送数据: {}", JSONObject.toJSONString(vppCmdData));

        // 上送工业负荷数据到电网（带重试）
        String vppResponse = deliveryVPPDataToGridWithRetry(vppCmdData, aggregatorId, resourceId);
        String result = "工业负荷:" + vppResponse;

        //量测推送判断
        LocalDateTime now = LocalDateTime.now();
        log.info("工业负荷-当前时间: {}", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (now.getMinute() % 15 == 0) {
            log.info("工业负荷-触发单体量测上送定时任务");
            //量测数据推送（带重试）
            String measResponse = deliveryMeasDataVPPWithRetry(vppConfigs, resourceId, resourceCode, aggregatorId, pRealTime);
            log.info("工业负荷-量测数据上送结果: {}", measResponse);
            result += " 量测:" + measResponse;
        }
        log.info("================== 工业负荷数据处理完成 ==================");
        return result;
    }

    /**
     * 上送电采暖数据到电网（带重试）
     */
    private String deliveryEHDataToGridWithRetry(Map<String, String> ehCmdData, String aggregatorId, String resourceId) {
        String response = null;
        // 记录开始时间，确保重试也在当前分钟内完成（1分钟内）
        long startTime = System.currentTimeMillis();
        long timeoutMillis = deliveryTimeoutMillis; // 从配置中心获取超时时间
        final String[] lastFailedUrl = {null}; // 记录上次失败的URL，重试时尝试其他URL

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 检查是否超时，如果超时则不再重试
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= timeoutMillis) {
                    log.error("电采暖-上送电网超时，已用时: {}ms，超过55秒限制，不再重试", elapsedTime);
                    response = "失败:超过1分钟时间限制";
                    break;
                }

                if (attempt == 1) {
                    log.info("电采暖-开始上送电网（初始尝试），聚合商ID: {}, 资源类型ID: {}, 上送数据: {}", aggregatorId, resourceId, JSONObject.toJSONString(ehCmdData));
                } else {
                    long remainingTime = timeoutMillis - elapsedTime;
                    log.info("电采暖-开始上送电网（第{}次重试），聚合商ID: {}, 资源类型ID: {}, 剩余时间: {}ms, 上送数据: {}",
                            attempt - 1, aggregatorId, resourceId, remainingTime, JSONObject.toJSONString(ehCmdData));
                }

                // 如果是重试且上次是超时异常，尝试使用其他URL
                Greeter greeter;
                if (attempt > 1 && StringUtils.isNotBlank(lastFailedUrl[0])) {
                    // 获取所有可用URL，排除上次失败的URL
                    List<String> availableUrls = huabeiUrlService.getAllAvailableUrl(totalAndDeliveryUrl);
                    final String failedUrl = lastFailedUrl[0];
                    availableUrls.removeIf(url -> url.equals(failedUrl));
                    if (CollectionUtils.isNotEmpty(availableUrls)) {
                        String nextUrl = availableUrls.get(0);
                        log.info("电采暖-上次URL超时，切换到新URL: {}", nextUrl);
                        greeter = clientConfig.greeter(totalAndDeliveryUrl, nextUrl);
                    } else {
                        log.warn("电采暖-没有其他可用URL，继续使用原URL列表");
                        greeter = clientConfig.greeter(totalAndDeliveryUrl);
                    }
                } else {
                    greeter = clientConfig.greeter(totalAndDeliveryUrl);
                }

                response = greeter.cmd(JSONObject.toJSONString(ehCmdData));
                long totalTime = System.currentTimeMillis() - startTime;
                if (attempt == 1) {
                    log.info("电采暖-上送电网成功（初始尝试），耗时: {}ms, 响应: {}", totalTime, response);
                } else {
                    log.info("电采暖-上送电网成功（第{}次重试），耗时: {}ms, 响应: {}", attempt - 1, totalTime, response);
                }
                // 成功则跳出循环
                break;
            } catch (Exception e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                boolean isTimeout = isTimeoutException(e);

                // 记录失败的URL（如果是超时异常）
                if (isTimeout && attempt == 1) {
                    try {
                        String currentUrl = huabeiUrlService.getAvailableUrl(totalAndDeliveryUrl);
                        lastFailedUrl[0] = currentUrl;
                        log.warn("电采暖-检测到超时异常，记录失败URL: {}", lastFailedUrl[0]);
                    } catch (Exception ex) {
                        log.warn("电采暖-获取当前URL失败", ex);
                    }
                }

                if (attempt == 1) {
                    String errorMsg = isTimeout ? "连接超时" : ExceptionUtils.getMessage(e.getCause());
                    log.warn("电采暖-上送电网失败（初始尝试），已用时: {}ms, 异常类型: {}, 异常信息: {}",
                            elapsedTime, isTimeout ? "超时" : "其他", errorMsg);
                    // 检查剩余时间是否足够重试
                    if (elapsedTime < timeoutMillis && attempt < maxRetries) {
                        log.info("电采暖-准备进行第1次重试，剩余时间: {}ms, 将使用第一次准备的数据: {}",
                                timeoutMillis - elapsedTime, JSONObject.toJSONString(ehCmdData));
                    } else {
                        // 剩余时间不足或已达到最大重试次数，设置失败响应
                        if (elapsedTime >= timeoutMillis) {
                            log.error("电采暖-剩余时间不足，不再重试");
                            response = "失败:超过1分钟时间限制";
                        } else {
                            log.error("电采暖-已达到最大重试次数，不再重试");
                            response = "失败:" + (isTimeout ? "连接超时" : e.getMessage());
                        }
                        break;
                    }
                } else {
                    log.error("电采暖-上送电网失败（第{}次重试），已用时: {}ms, 异常类型: {}, 异常信息: {}",
                            attempt - 1, elapsedTime, isTimeout ? "超时" : "其他", ExceptionUtils.getStackTrace(e));
                    response = "失败:" + (isTimeout ? "连接超时" : e.getMessage());
                }
            }
        }

        // 确保response有值（防止所有尝试都失败但response仍为null的情况）
        if (response == null) {
            response = "失败:未知错误";
            log.error("电采暖-上送电网失败，response为null，设置为默认失败状态");
        }

        // 记录日志（无论成功或失败都保存）
        try {
            TotalDeliveryLog totalDeliveryLog = new TotalDeliveryLog();
            totalDeliveryLog.setCreateTime(new Date());
            totalDeliveryLog.setValue(JSONObject.toJSONString(ehCmdData));
            totalDeliveryLog.setDeliveryStatus(response);
            totalDeliveryLog.setGroupNo(resourceId);
            totalDeliveryLogService.addLog(totalDeliveryLog);
            log.info("电采暖-上送日志已记录到数据库，groupNo: {}, status: {}", resourceId, response);
        } catch (Exception e) {
            log.error("电采暖-记录上送日志到数据库失败: {}", ExceptionUtils.getStackTrace(e));
        }

        return response;
    }

    /**
     * 上送电采暖数据到电网
     */
    private String deliveryEHDataToGrid(Map<String, String> ehCmdData, String aggregatorId, String resourceId) {
        String response = null;
        try {
            log.info("电采暖-开始上送电网，聚合商ID: {}, 资源类型ID: {}", aggregatorId, resourceId);
            Greeter greeter = clientConfig.greeter(totalAndDeliveryUrl);
            response = greeter.cmd(JSONObject.toJSONString(ehCmdData));
            log.info("电采暖-上送电网成功，响应: {}", response);
        } catch (Exception e) {
            log.warn("电采暖-上送电网失败，异常信息: {}", ExceptionUtils.getMessage(e.getCause()));
            e.printStackTrace();
            response = "失败:" + e.getMessage();
        } finally {
            TotalDeliveryLog totalDeliveryLog = new TotalDeliveryLog();
            totalDeliveryLog.setCreateTime(new Date());
            totalDeliveryLog.setValue(JSONObject.toJSONString(ehCmdData));
            totalDeliveryLog.setDeliveryStatus(response);
            totalDeliveryLog.setGroupNo(resourceId);
            totalDeliveryLogService.addLog(totalDeliveryLog);
            log.info("电采暖-上送日志已记录，groupNo: {}", resourceId);
        }
        return response;
    }

    /**
     * 判断是否是超时异常
     */
    private boolean isTimeoutException(Exception e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SocketTimeoutException || cause instanceof ConnectException) {
                return true;
            }
            String message = cause.getMessage();
            if (message != null && (message.contains("timed out") || message.contains("connect timed out")
                    || message.contains("Read timed out") || message.contains("Connection timed out"))) {
                return true;
            }
            cause = cause.getCause();
        }
        String message = e.getMessage();
        return message != null && (message.contains("timed out") || message.contains("connect timed out")
                || message.contains("Read timed out") || message.contains("Connection timed out"));
    }

    /**
     * 上送工业负荷数据到电网（带重试）
     */
    private String deliveryVPPDataToGridWithRetry(Map<String, String> vppCmdData, String aggregatorId, String resourceId) {
        String response = null;
        // 记录开始时间，确保重试也在当前分钟内完成（1分钟内）
        long startTime = System.currentTimeMillis();
        long timeoutMillis = deliveryTimeoutMillis; // 从配置中心获取超时时间
        final String[] lastFailedUrl = {null}; // 记录上次失败的URL，重试时尝试其他URL

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 检查是否超时，如果超时则不再重试
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= timeoutMillis) {
                    log.error("工业负荷-上送电网超时，已用时: {}ms，超过55秒限制，不再重试", elapsedTime);
                    response = "失败:超过1分钟时间限制";
                    break;
                }

                if (attempt == 1) {
                    log.info("工业负荷-开始上送电网（初始尝试），聚合商ID: {}, 资源类型ID: {}, 上送数据: {}", aggregatorId, resourceId, JSONObject.toJSONString(vppCmdData));
                } else {
                    long remainingTime = timeoutMillis - elapsedTime;
                    log.info("工业负荷-开始上送电网（第{}次重试），聚合商ID: {}, 资源类型ID: {}, 剩余时间: {}ms, 上送数据: {}",
                            attempt - 1, aggregatorId, resourceId, remainingTime, JSONObject.toJSONString(vppCmdData));
                }

                // 如果是重试且上次是超时异常，尝试使用其他URL
                Greeter greeter;
                if (attempt > 1 && StringUtils.isNotBlank(lastFailedUrl[0])) {
                    // 获取所有可用URL，排除上次失败的URL
                    List<String> availableUrls = huabeiUrlService.getAllAvailableUrl(totalAndDeliveryUrl);
                    final String failedUrl = lastFailedUrl[0];
                    availableUrls.removeIf(url -> url.equals(failedUrl));
                    if (CollectionUtils.isNotEmpty(availableUrls)) {
                        String nextUrl = availableUrls.get(0);
                        log.info("工业负荷-上次URL超时，切换到新URL: {}", nextUrl);
                        greeter = clientConfig.greeter(totalAndDeliveryUrl, nextUrl);
                    } else {
                        log.warn("工业负荷-没有其他可用URL，继续使用原URL列表");
                        greeter = clientConfig.greeter(totalAndDeliveryUrl);
                    }
                } else {
                    greeter = clientConfig.greeter(totalAndDeliveryUrl);
                }

                response = greeter.cmd(JSONObject.toJSONString(vppCmdData));
                long totalTime = System.currentTimeMillis() - startTime;
                if (attempt == 1) {
                    log.info("工业负荷-上送电网成功（初始尝试），耗时: {}ms, 响应: {}", totalTime, response);
                } else {
                    log.info("工业负荷-上送电网成功（第{}次重试），耗时: {}ms, 响应: {}", attempt - 1, totalTime, response);
                }
                // 成功则跳出循环
                break;
            } catch (Exception e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                boolean isTimeout = isTimeoutException(e);

                // 记录失败的URL（如果是超时异常）
                if (isTimeout && attempt == 1) {
                    try {
                        String currentUrl = huabeiUrlService.getAvailableUrl(totalAndDeliveryUrl);
                        lastFailedUrl[0] = currentUrl;
                        log.warn("工业负荷-检测到超时异常，记录失败URL: {}", lastFailedUrl[0]);
                    } catch (Exception ex) {
                        log.warn("工业负荷-获取当前URL失败", ex);
                    }
                }

                if (attempt == 1) {
                    String errorMsg = isTimeout ? "连接超时" : ExceptionUtils.getMessage(e.getCause());
                    log.warn("工业负荷-上送电网失败（初始尝试），已用时: {}ms, 异常类型: {}, 异常信息: {}",
                            elapsedTime, isTimeout ? "超时" : "其他", errorMsg);
                    // 检查剩余时间是否足够重试
                    if (elapsedTime < timeoutMillis && attempt < maxRetries) {
                        log.info("工业负荷-准备进行第1次重试，剩余时间: {}ms, 将使用第一次准备的数据: {}",
                                timeoutMillis - elapsedTime, JSONObject.toJSONString(vppCmdData));
                    } else {
                        // 剩余时间不足或已达到最大重试次数，设置失败响应
                        if (elapsedTime >= timeoutMillis) {
                            log.error("工业负荷-剩余时间不足，不再重试");
                            response = "失败:超过1分钟时间限制";
                        } else {
                            log.error("工业负荷-已达到最大重试次数，不再重试");
                            response = "失败:" + (isTimeout ? "连接超时" : e.getMessage());
                        }
                        break;
                    }
                } else {
                    log.error("工业负荷-上送电网失败（第{}次重试），已用时: {}ms, 异常类型: {}, 异常信息: {}",
                            attempt - 1, elapsedTime, isTimeout ? "超时" : "其他", ExceptionUtils.getStackTrace(e));
                    response = "失败:" + (isTimeout ? "连接超时" : e.getMessage());
                }
            }
        }

        // 确保response有值（防止所有尝试都失败但response仍为null的情况）
        if (response == null) {
            response = "失败:未知错误";
            log.error("工业负荷-上送电网失败，response为null，设置为默认失败状态");
        }

        // 记录日志（无论成功或失败都保存）
        try {
            TotalDeliveryLog totalDeliveryLog = new TotalDeliveryLog();
            totalDeliveryLog.setCreateTime(new Date());
            totalDeliveryLog.setValue(JSONObject.toJSONString(vppCmdData));
            totalDeliveryLog.setDeliveryStatus(response);
            totalDeliveryLog.setGroupNo(resourceId);
            totalDeliveryLogService.addLog(totalDeliveryLog);
            log.info("工业负荷-上送日志已记录到数据库，groupNo: {}, status: {}", resourceId, response);
        } catch (Exception e) {
            log.error("工业负荷-记录上送日志到数据库失败: {}", ExceptionUtils.getStackTrace(e));
        }

        return response;
    }

    /**
     * 上送工业负荷数据到电网
     */
    private String deliveryVPPDataToGrid(Map<String, String> vppCmdData, String aggregatorId, String resourceId) {
        String response = null;
        try {
            log.info("工业负荷-开始上送电网，聚合商ID: {}, 资源类型ID: {}", aggregatorId, resourceId);
            Greeter greeter = clientConfig.greeter(totalAndDeliveryUrl);
            response = greeter.cmd(JSONObject.toJSONString(vppCmdData));
            log.info("工业负荷-上送电网成功，响应: {}", response);
        } catch (Exception e) {
            log.warn("工业负荷-上送电网失败，异常信息: {}", ExceptionUtils.getMessage(e.getCause()));
            e.printStackTrace();
            response = "失败:" + e.getMessage();
        } finally {
            TotalDeliveryLog totalDeliveryLog = new TotalDeliveryLog();
            totalDeliveryLog.setCreateTime(new Date());
            totalDeliveryLog.setValue(JSONObject.toJSONString(vppCmdData));
            totalDeliveryLog.setDeliveryStatus(response);
            totalDeliveryLog.setGroupNo(resourceId);
            totalDeliveryLogService.addLog(totalDeliveryLog);
            log.info("工业负荷-上送日志已记录，groupNo: {}", resourceId);
        }
        return response;
    }


    private Map<String, String> processEHCmdData(List<AggregatorEntDevice> ehConfigs, String channelNo, List<BigDataRealTimeResp> realTimeData) {

        if (CollectionUtils.isEmpty(ehConfigs)) {
            return Maps.newHashMap();
        }

        // 从control_issue_log 获取最近的控制下发信息
        String issueValue = "0.000000";
        String issueStatus = "1";
        String issueSign = "1";
        ControlIssueLog controlIssueLog = controlIssueLogService.getLastLogByGroupNo(channelNo);

        if (null != controlIssueLog && null != controlIssueLog.getCmdData()) {
            JSONObject cmdData = JSONObject.parseObject(controlIssueLog.getCmdData());
            issueValue = StringUtils.isBlank(cmdData.getString(channelNo + "-1")) ? issueValue : cmdData.getString(channelNo + "-1");
            issueStatus = StringUtils.isBlank(cmdData.getString(channelNo + "-3")) ? issueStatus : cmdData.getString(channelNo + "-3");
            issueSign = StringUtils.isBlank(cmdData.getString(channelNo + "-4")) ? issueSign : cmdData.getString(channelNo + "-4");
        }

        //  根据子企业获取所有对应资源的数据
        Map<String, String> cmdData = new LinkedHashMap<>();
        // 电采暖26
        // 京津唐电采暖AGC投退状态
        cmdData.put(channelNo + "-1", issueStatus);
        // 京津唐电采暖有功实发命令（返回值）
        cmdData.put(channelNo + "-2", issueValue);
        // 华北系统中京津唐电采暖AGC正控信号（返回值）
        cmdData.put(channelNo + "-3", issueSign);
        // 可参与调节的京津唐电采暖实时数量
        String ehSize = String.valueOf(ehConfigs.size());
        cmdData.put(channelNo + "-4", ehSize);
        // 参与调节的京津唐电采暖实时有功（单位MW，以用电为﹢）
        String activePowerForEH = getActivePowerForEH(ehConfigs, realTimeData);
        // modify by sl 2024-11-08 聚合参与模型总加功率
        String activePowerForEHForParticipation = getActivePowerForEHForParticipation(ehConfigs, realTimeData);
        cmdData.put(channelNo + "-5", activePowerForEHForParticipation);
        // 参与调节的京津唐电采暖功率可维持最大时间
        cmdData.put(channelNo + "-6", "0");
        // 参与调节的京津唐电采暖有功上限（最大用电）
        cmdData.put(channelNo + "-7", "0");
        // 参与调节的京津唐电采暖下限（最小用电）
        cmdData.put(channelNo + "-8", "0");
        // 参与调节京津唐电采暖功率最大允许命令步长
        cmdData.put(channelNo + "-9", "0");
        // 可调节的京津唐电采暖实时有功（单位MW，以用电为﹢）
        cmdData.put(channelNo + "-10", activePowerForEH);

        return cmdData;
    }

    /**
     *
     * <聚合参与模型总加功率><功能具体实现>
     *
     * @create：2024/11/8 10:42
     * @author sl
     * @param ehConfigs
     * @param realTimeData
     * @return java.lang.String
     */
    private String getActivePowerForEHForParticipation(List<AggregatorEntDevice> ehConfigs, List<BigDataRealTimeResp> realTimeData) {
        if (CollectionUtils.isEmpty(realTimeData)) {
            return "0.0000";
        }

        if (CollectionUtils.isEmpty(ehConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }
        // 不上送模型企业
        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(ehConfigs.get(0).getAggregatorId(), ehConfigs.get(0).getResourceTypeId(), noUpModelEnergyStationCodes);
        // 参与模型energy_station_code 集合
        if (CollectionUtil.isEmpty(modelInfoList)) {
            log.warn("查询参与模型为空, 能源类型：{}", ehConfigs.get(0).getResourceTypeId());
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }
        List<String> energyStationCodes = modelInfoList.stream().filter(o -> "1".equals(o.getControll())).map(AggregatorSingleModelData::getEnergyStationCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(energyStationCodes)) {
            log.warn("参与模型能源站为空，返回0");
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }
        // modify by sl 修复参与功率计算：只计算属于参与模型能源站的设备，而不是整个站点的所有设备
        // 构建参与模型能源站的Map，用于快速查找（只包含controll="1"的能源站）
        Map<String, AggregatorSingleModelData> participationEnergyStationMap = modelInfoList.stream()
                .filter(o -> "1".equals(o.getControll()))
                .collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));
        // 获取参与模型能源站下的所有设备ID列表（确保设备所属的能源站在参与模型Map中存在，且energyStationCode不为空）
        // modify by sl 修复：设备ID需要拼接stationId，避免不同企业的相同设备ID重复
        List<String> participationDeviceIds = ehConfigs.stream()
                .filter(aggregatorEntDevice -> {
                    String energyStationCode = aggregatorEntDevice.getEnergyStationCode();
                    // 确保energyStationCode不为空，且在参与模型Map中存在（controll="1"）
                    return StringUtils.isNotBlank(energyStationCode) && participationEnergyStationMap.containsKey(energyStationCode);
                })
                .map(aggregatorEntDevice -> aggregatorEntDevice.getStationId() + "_" + aggregatorEntDevice.getDeviceId())
                .distinct()
                .collect(Collectors.toList());
        log.info("参与模型能源站数量: {}, 总设备数量: {}, 参与设备数量: {}", energyStationCodes.size(), ehConfigs.size(), participationDeviceIds.size());
        // realTimeData 过滤：只保留属于参与模型能源站的设备数据（使用stationId_equipMK_equipID格式匹配）
        realTimeData = realTimeData.stream()
                .filter(resp -> participationDeviceIds.contains(resp.getStaId() + "_" + resp.getEquipMK() + "_" + resp.getEquipID()))
                .collect(Collectors.toList());
        // 按站点和设备排序
        Map<String, BigDataRealTimeResp> map = realTimeData.stream()
                .collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));
        int mapSize = CollectionUtil.size(map);
        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> dataRespList = v.getDataResp();
            if (!CollectionUtils.isEmpty(dataRespList)) {
                DataResp powerData = dataRespList.get(dataRespList.size() - 1); // 取最后一个数据点
                BigDecimal value = processTotalPowerData(powerData);
                activePower[0] = MathUtils.add(activePower[0], value);
            }
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }


    private String getActivePowerForEH(List<AggregatorEntDevice> ehConfigs, List<BigDataRealTimeResp> realTimeData) {

        if (CollectionUtils.isEmpty(realTimeData)) {
            return "0.0000";
        }

        if (CollectionUtils.isEmpty(ehConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }
        // 按站点和设备排序
        Map<String, BigDataRealTimeResp> map = realTimeData.stream()
                .collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));
        int mapSize = CollectionUtil.size(map);
        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> dataRespList = v.getDataResp();
            if (!CollectionUtils.isEmpty(dataRespList)) {
                DataResp powerData = dataRespList.get(dataRespList.size() - 1); // 取最后一个数据点
                BigDecimal value = processTotalPowerData(powerData);
                activePower[0] = MathUtils.add(activePower[0], value);
            }
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }

    private BigDecimal processTotalPowerData(List<DataResp> powerData) {

        BigDecimal value = BigDecimal.ZERO;
        powerData = powerData.stream().filter(x -> null != x.getValue()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(powerData)) {
            return value;
        }
        //排序
//        List<DataResp> powerDataSort =
//                powerData.stream().filter(e->StrUtil.isNotEmpty(e.getTime())).sorted(Comparator.comparingLong(e -> Long.parseLong(e.getTime()))).collect(Collectors.toList());
        List<DataResp> powerDataSort = powerData.stream().filter(e -> StrUtil.isNotEmpty(e.getTime()))
                .sorted(Comparator.comparing(e -> DateUtil.parse(e.getTime())))
                .collect(Collectors.toList());
        Double lastValue = powerDataSort.get(powerData.size() - 1).getValue();
        value = new BigDecimal(String.valueOf(lastValue));

        return value;
    }

    /**
     * 获取总加有功P
     *
     * @param ehConfigs
     * @return
     */
    public RealTimeReq getParamTotal(List<AggregatorEntDevice> ehConfigs) {
        RealTimeReq realTimeReq = new RealTimeReq();
        realTimeReq.setDataSource("EMS");
        realTimeReq.setDays(1);
        realTimeReq.setIsClean(false);
        if (CollectionUtil.isEmpty(ehConfigs)) {
            return realTimeReq;
        }
        List<OpentsdbReq> listQueries = Lists.newArrayList();
        ehConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });
        realTimeReq.setListQueries(listQueries);
        return realTimeReq;
    }

    /**
     * 获取量测入参-不包含P
     *
     * @param ehConfigs
     * @return
     */
    public RealTimeReq getParamSingle(List<AggregatorEntDevice> ehConfigs) {
        RealTimeReq realTimeReq = new RealTimeReq();
        realTimeReq.setDataSource("EMS");
        realTimeReq.setDays(1);
        realTimeReq.setIsClean(false);
        if (CollectionUtil.isEmpty(ehConfigs)) {
            return realTimeReq;
        }
        List<OpentsdbReq> listQueries = Lists.newArrayList();
        ehConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setMetric("EMS.Q");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);

            OpentsdbReq opentsdbReq1 = new OpentsdbReq();
            opentsdbReq1.setMetric("EMS.Ia");
            TagVO tag1 = new TagVO();
            tag1.setStaId(config.getStationId());
            tag1.setEquipMK(config.getDeviceType());
            tag1.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq1.setTags(tag1);
            listQueries.add(opentsdbReq1);

            OpentsdbReq opentsdbReq2 = new OpentsdbReq();
            opentsdbReq2.setMetric("EMS.Eptp");
            TagVO tag2 = new TagVO();
            tag2.setStaId(config.getStationId());
            tag2.setEquipMK(config.getDeviceType());
            tag2.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq2.setTags(tag2);
            listQueries.add(opentsdbReq2);
        });
        realTimeReq.setListQueries(listQueries);
        return realTimeReq;
    }

    /**
     * 大数据实测点实时接口处理
     *
     * @param powerData
     * @return
     */
    private BigDecimal processTotalPowerData(DataResp powerData) {
        // 秒级触发事件
        LocalDateTime triggerTime = LocalDateTime.now();
        BigDecimal value = BigDecimal.ZERO;

        if (Objects.isNull(powerData) || Objects.isNull(powerData.getValue()) || StringUtils.isBlank(powerData.getTime())) {
            return value;
        }
        //格式为yyyy-MM-dd HH:mm:ss
        String lastTimeStr = powerData.getTime();
        LocalDateTime lastTime = LocalDateTime.parse(lastTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 大于有效性时间-置零
        if (LocalDateTimeUtil.between(lastTime,triggerTime, ChronoUnit.SECONDS) > bigdataRealtimeInterval) {
            return value;
        } else {
            // 符合有效性直接返回
            Double lastValue = powerData.getValue();
            value = new BigDecimal(String.valueOf(lastValue));
        }

        return value;
    }


    private String deliveryMeasDataEH(List<AggregatorEntDevice> ehConfigs, String channelNo, String resourceCode, String aggregatorId, List<BigDataRealTimeResp> pRealTimeData) {

        if (CollectionUtils.isEmpty(ehConfigs)) {
            return "EH has no config data";
        }
        // 获取数据库配置企业信息
        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntListByResourTypeId(channelNo);
        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
        //singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.ELECTRIC_HEATING);

        List<Object> singleMeasData = Lists.newArrayList();
        RealTimeReq paramSingle = getParamSingle(ehConfigs);
//        List<OpentsdbReq> listQueries = paramSingle.getListQueries();
//        int maxTaskSize = 30;
//        List<List<OpentsdbReq>> subDeviceLists = Lists.partition(listQueries, maxTaskSize);
//        List<RealTimeReq> totalRealTimeParamGroup = new ArrayList<>();
//        for (List<OpentsdbReq> subDeviceList : subDeviceLists) {
//            RealTimeReq realTimeReq = new RealTimeReq();
//            realTimeReq.setDataSource("EMS");
//            realTimeReq.setDays(1);
//            realTimeReq.setIsClean(false);
//            realTimeReq.setListQueries(subDeviceList);
//            totalRealTimeParamGroup.add(realTimeReq);
//        }
//        List<BigDataRealTimeResp> pRealTime = new ArrayList<>();
//        final List<Future<List<BigDataRealTimeResp>>> futures = new ArrayList<>(totalRealTimeParamGroup.size());
//        for (RealTimeReq realTimeReq : totalRealTimeParamGroup) {
//            futures.add(businessExecutorService.submit(() ->bigDataHandlerService.getRealTime(realTimeReq, "0") ));
//        }
//        final List<List<BigDataRealTimeResp>> valuesResultList = new ArrayList<>(futures.size());
//        for (Future<List<BigDataRealTimeResp>> future : futures) {
//            try {
//                if(CollectionUtil.isNotEmpty(future.get())){
//                    pRealTime.addAll(future.get());
//                }
//            } catch (InterruptedException | ExecutionException e) {
//                log.warn("报表查询，企业{}的子查询任务被中断", "a", e);
//            }
//        }
//        log.info("总加大数据多线程查询结果为"+JSONObject.toJSONString(pRealTime));
        List<BigDataRealTimeResp> singleRealTimeData = null;
        String keyEhMeas = "loadAggregatorDelivery"+aggregatorId + ":EH:MEAS";
        try {
            singleRealTimeData = bigDataHandlerService.getRealTime(paramSingle, "0");
            if (CollectionUtil.isNotEmpty(pRealTimeData)) {
                if (singleRealTimeData == null) {
                    singleRealTimeData = new ArrayList<>();
                }
                singleRealTimeData.addAll(pRealTimeData);
            }
            if (CollectionUtils.isNotEmpty(singleRealTimeData)) {
                redisUtil.set(keyEhMeas, singleRealTimeData, bigdataRealtimeInterval);
            }
        } catch (Exception e) {
            log.warn("电采暖-单体量测-实时数据查询异常:参数{}", JSONObject.toJSONString(paramSingle));
            log.warn("电采暖-单体量测-实时数据查询异常:异常信息{}", ExceptionUtils.getStackTrace(e));
            Object singleResult = redisUtil.get(keyEhMeas);
            if(Objects.isNull(singleResult)){
                singleRealTimeData = new ArrayList<>();
                log.warn("电采暖-从缓存获取最新量测值为空");
            }else{
                singleRealTimeData = (List<BigDataRealTimeResp>) redisUtil.get(keyEhMeas);
                log.warn("电采暖-从缓存获取最新量测值:{}", JSONObject.toJSONString(singleRealTimeData));
            }
        }

        // 确保 singleRealTimeData 不为 null
        if (singleRealTimeData == null) {
            singleRealTimeData = new ArrayList<>();
            log.warn("电采暖-实时数据为空，初始化为空列表");
        }

        // modify by sl 024-10-24 增加不上送模型
        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, channelNo, noUpModelEnergyStationCodes);
        Map<String, AggregatorSingleModelData> energyStationMap = modelInfoList.stream().collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));
        // 按systemCode 归并
        Map<String, List<BigDataRealTimeResp>> mapGroupingByStationId = singleRealTimeData.stream().collect(Collectors.groupingBy(BigDataRealTimeResp::getStaId));
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        //按能源站合并
        Map<String, List<BigDataRealTimeResp>> mapGroupingByEnergyStationId = new HashMap<>();
        for (Map.Entry<String, List<BigDataRealTimeResp>> systemCodeResult : mapGroupingByStationId.entrySet()) {
            String systemCodea = systemCodeResult.getKey();
            List<BigDataRealTimeResp> value = systemCodeResult.getValue();
            List<String> energyStationCodeLists = ehConfigs.stream().filter(e -> StrUtil.equals(e.getStationId(), systemCodea)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = ehConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }

                List<BigDataRealTimeResp> energyStationResult = value.stream().filter(e -> energyStationDeviceIds.contains(e.getEquipMK() + "_" + e.getEquipID())).collect(Collectors.toList());
                mapGroupingByEnergyStationId.put(energyStationCode, energyStationResult);
            }
        }
        mapGroupingByEnergyStationId.forEach((k, v) -> {
            Map<String, String> map = new HashMap<>(16);

            // P 总有功功率
            List<BigDataRealTimeResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            String totalActivePower = processMeasureDataFromRealTimeResp(bigDataHistoryRespListP);

            // Q 总无功功率
            List<BigDataRealTimeResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            String totalReactivePower = processMeasureDataFromRealTimeResp(bigDataHistoryRespListQ);

            // Ia A相电流
            List<BigDataRealTimeResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            String userElecCurrent = processMeasureDataFromRealTimeResp(bigDataHistoryRespListIa);

            // Eptp 有功电度正向量（）
            List<BigDataRealTimeResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            String todayZeroElecQuantity = processMeasureDataFromRealTimeResp(bigDataHistoryRespListEPTP);
            //            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();

            if (energyStationMap.get(k) != null) {
                map.put("username", energyStationMap.get(k).getEnergyStation());
                map.put("userActivePower", totalActivePower);
                map.put("userReactivePower", totalReactivePower);
                map.put("userElecCurrent", userElecCurrent);
                map.put("todayZeroElecQuantity", todayZeroElecQuantity);
                map.put("innerStationId", energyStationMap.get(k).getEnergyStationCode());

                singleMeasData.add(map);
                singleMeasDataMap.put(k, map);
            }
        });

        energyStationMap.forEach((energyStationCode, energyStationInfo) -> {
            // 大数据平台数据缺失，无返回时
            if (!singleMeasDataMap.containsKey(energyStationCode)) {
                Map<String, String> map = new HashMap<>(8);
                map.put("username", energyStationInfo.getEnergyStation());
                map.put("userActivePower", "0.0000");
                map.put("userReactivePower", "0.0000");
                map.put("userElecCurrent", "0.0000");
                map.put("todayZeroElecQuantity", "0.0000");
                map.put("innerStationId", energyStationInfo.getEnergyStationCode());

                singleMeasData.add(map);
            }
        });
        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);

//        String filename = getFileName(singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
        String filename = getFileName(resourceCode, "MEAS", aggregatorAliasName);
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MEAS", resourceCode).getName();
        Map<String, Object> map = new HashMap<>(16);
        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
        map.put("company", aggregatorAliasName);
        String response = null;
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();
            log.info("电采暖-单体量测数据-templateData:{}", templateData);
            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
            String encodeString = Base64.getEncoder().encodeToString(bytes);
            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
            response = greeter.commitFile(filename, encodeString);
            log.info("聚合商" + aggregatorId + "资源Id" + channelNo + "单体量测数据上送成功");
        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        } finally {
            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
            singleMeasDeliveryLog.setFileName(filename);
            // 日志入库为原始请求报文
            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
//            singleMeasDeliveryLog.setFileByte("");
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
        }

        return response;
    }

    /**
     * 电采暖量测数据推送（带重试）
     */
    private String deliveryMeasDataEHWithRetry(List<AggregatorEntDevice> ehConfigs, String channelNo, String resourceCode, String aggregatorId, List<BigDataRealTimeResp> pRealTimeData) {
        if (CollectionUtils.isEmpty(ehConfigs)) {
            return "EH has no config data";
        }

        // 准备数据（这部分不需要重试）
        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntListByResourTypeId(channelNo);
        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
        List<Object> singleMeasData = Lists.newArrayList();
        RealTimeReq paramSingle = getParamSingle(ehConfigs);

        List<BigDataRealTimeResp> singleRealTimeData = null;
        String keyEhMeas = "loadAggregatorDelivery"+aggregatorId + ":EH:MEAS";
        try {
            singleRealTimeData = bigDataHandlerService.getRealTime(paramSingle, "0");
            if (CollectionUtil.isNotEmpty(pRealTimeData)) {
                if (singleRealTimeData == null) {
                    singleRealTimeData = new ArrayList<>();
                }
                singleRealTimeData.addAll(pRealTimeData);
            }
            if (CollectionUtils.isNotEmpty(singleRealTimeData)) {
                redisUtil.set(keyEhMeas, singleRealTimeData, bigdataRealtimeInterval);
            }
        } catch (Exception e) {
            log.warn("电采暖-单体量测-实时数据查询异常:参数{}", JSONObject.toJSONString(paramSingle));
            log.warn("电采暖-单体量测-实时数据查询异常:异常信息{}", ExceptionUtils.getStackTrace(e));
            Object singleResult = redisUtil.get(keyEhMeas);
            if(Objects.isNull(singleResult)){
                singleRealTimeData = new ArrayList<>();
                log.warn("电采暖-从缓存获取最新量测值为空");
            }else{
                singleRealTimeData = (List<BigDataRealTimeResp>) redisUtil.get(keyEhMeas);
                log.warn("电采暖-从缓存获取最新量测值:{}", JSONObject.toJSONString(singleRealTimeData));
            }
        }

        if (singleRealTimeData == null) {
            singleRealTimeData = new ArrayList<>();
            log.warn("电采暖-实时数据为空，初始化为空列表");
        }

        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, channelNo, noUpModelEnergyStationCodes);
        Map<String, AggregatorSingleModelData> energyStationMap = modelInfoList.stream().collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));

        Map<String, List<BigDataRealTimeResp>> mapGroupingByStationId = singleRealTimeData.stream().collect(Collectors.groupingBy(BigDataRealTimeResp::getStaId));
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        Map<String, List<BigDataRealTimeResp>> mapGroupingByEnergyStationId = new HashMap<>();
        for (Map.Entry<String, List<BigDataRealTimeResp>> systemCodeResult : mapGroupingByStationId.entrySet()) {
            String systemCodea = systemCodeResult.getKey();
            List<BigDataRealTimeResp> value = systemCodeResult.getValue();
            List<String> energyStationCodeLists = ehConfigs.stream().filter(e -> StrUtil.equals(e.getStationId(), systemCodea)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = ehConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }
                List<BigDataRealTimeResp> energyStationResult = value.stream().filter(e -> energyStationDeviceIds.contains(e.getEquipMK() + "_" + e.getEquipID())).collect(Collectors.toList());
                mapGroupingByEnergyStationId.put(energyStationCode, energyStationResult);
            }
        }
        mapGroupingByEnergyStationId.forEach((k, v) -> {
            Map<String, String> map = new HashMap<>(16);
            List<BigDataRealTimeResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            String totalActivePower = processMeasureDataFromRealTimeResp(bigDataHistoryRespListP);
            List<BigDataRealTimeResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            String totalReactivePower = processMeasureDataFromRealTimeResp(bigDataHistoryRespListQ);
            List<BigDataRealTimeResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            String userElecCurrent = processMeasureDataFromRealTimeResp(bigDataHistoryRespListIa);
            List<BigDataRealTimeResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            String todayZeroElecQuantity = processMeasureDataFromRealTimeResp(bigDataHistoryRespListEPTP);

            if (energyStationMap.get(k) != null) {
                map.put("username", energyStationMap.get(k).getEnergyStation());
                map.put("userActivePower", totalActivePower);
                map.put("userReactivePower", totalReactivePower);
                map.put("userElecCurrent", userElecCurrent);
                map.put("todayZeroElecQuantity", todayZeroElecQuantity);
                map.put("innerStationId", energyStationMap.get(k).getEnergyStationCode());
                singleMeasData.add(map);
                singleMeasDataMap.put(k, map);
            }
        });

        energyStationMap.forEach((energyStationCode, energyStationInfo) -> {
            if (!singleMeasDataMap.containsKey(energyStationCode)) {
                Map<String, String> map = new HashMap<>(8);
                map.put("username", energyStationInfo.getEnergyStation());
                map.put("userActivePower", "0.0000");
                map.put("userReactivePower", "0.0000");
                map.put("userElecCurrent", "0.0000");
                map.put("todayZeroElecQuantity", "0.0000");
                map.put("innerStationId", energyStationInfo.getEnergyStationCode());
                singleMeasData.add(map);
            }
        });
        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);

        String filename = getFileName(resourceCode, "MEAS", aggregatorAliasName);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MEAS", resourceCode).getName();
        Map<String, Object> map = new HashMap<>(16);
        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
        map.put("company", aggregatorAliasName);

        // 上送电网（带重试）- 重试时使用第一次准备的数据，不重新获取
        String response = null;
        String encodeString = null; // 保存第一次生成的编码数据，重试时直接使用
        // 记录开始时间，确保重试也在当前分钟内完成
        long startTime = System.currentTimeMillis();
        long timeoutMillis = deliveryTimeoutMillis; // 从配置中心获取超时时间

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 检查是否超时，如果超时则不再重试
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= timeoutMillis) {
                    log.error("电采暖-单体量测上送超时，已用时: {}ms，超过55秒限制，不再重试", elapsedTime);
                    response = "失败:超过1分钟时间限制";
                    break;
                }

                if (attempt == 1) {
                    log.info("电采暖-单体量测-开始上送电网（初始尝试），聚合商ID: {}, 资源ID: {}, 文件名: {}", aggregatorId, channelNo, filename);
                } else {
                    long remainingTime = timeoutMillis - elapsedTime;
                    log.info("电采暖-单体量测-开始上送电网（第{}次重试），聚合商ID: {}, 资源ID: {}, 文件名: {}, 剩余时间: {}ms, 使用第一次准备的数据",
                            attempt - 1, aggregatorId, channelNo, filename, remainingTime);
                }

                // 第一次尝试时生成模板数据，重试时直接使用第一次的数据
                if (encodeString == null) {
                    ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
                    if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                        response = StatusCode.F_A.getMsg();
                        throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
                    }
                    String templateData = templateResult.getData();
                    log.info("电采暖-单体量测数据-templateData:{}", templateData);
                    byte[] bytes = templateData.getBytes(Charsets.UTF_8);
                    encodeString = Base64.getEncoder().encodeToString(bytes);
                    log.info("电采暖-单体量测数据-编码后数据长度: {}", encodeString.length());
                } else {
                    log.info("电采暖-单体量测-重试使用第一次准备的数据，编码数据长度: {}", encodeString.length());
                }

                Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
                response = greeter.commitFile(filename, encodeString);
                long totalTime = System.currentTimeMillis() - startTime;
                if (attempt == 1) {
                    log.info("电采暖-单体量测数据上送成功（初始尝试），耗时: {}ms, 聚合商: {}, 资源Id: {}, 响应: {}", totalTime, aggregatorId, channelNo, response);
                } else {
                    log.info("电采暖-单体量测数据上送成功（第{}次重试），耗时: {}ms, 聚合商: {}, 资源Id: {}, 响应: {}", attempt - 1, totalTime, aggregatorId, channelNo, response);
                }
                // 成功则跳出循环
                break;
            } catch (Exception e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (attempt == 1) {
                    log.warn("电采暖-单体量测数据上送失败（初始尝试），已用时: {}ms, 异常信息: {}", elapsedTime, ExceptionUtils.getStackTrace(e));
                    // 检查剩余时间是否足够重试
                    if (elapsedTime < timeoutMillis && attempt < maxRetries) {
                        log.info("电采暖-单体量测-准备进行第1次重试，剩余时间: {}ms, 将使用第一次准备的数据，文件名: {}",
                                timeoutMillis - elapsedTime, filename);
                    } else {
                        // 剩余时间不足或已达到最大重试次数，设置失败响应
                        if (elapsedTime >= timeoutMillis) {
                            log.error("电采暖-单体量测-剩余时间不足，不再重试");
                            response = "失败:超过1分钟时间限制";
                        } else {
                            log.error("电采暖-单体量测-已达到最大重试次数，不再重试");
                            response = "失败:" + e.getMessage();
                        }
                        break;
                    }
                } else {
                    log.error("电采暖-单体量测数据上送失败（第{}次重试），已用时: {}ms, 已达到最大重试次数，异常信息: {}",
                            attempt - 1, elapsedTime, ExceptionUtils.getStackTrace(e));
                    response = "失败:" + e.getMessage();
                }
            }
        }

        // 确保response有值（防止所有尝试都失败但response仍为null的情况）
        if (response == null) {
            response = "失败:未知错误";
            log.error("电采暖-单体量测上送失败，response为null，设置为默认失败状态");
        }

        // 记录日志（无论成功或失败都保存）
        try {
            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
            singleMeasDeliveryLog.setFileName(filename);
            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
            log.info("电采暖-单体量测上送日志已记录到数据库，文件名: {}, status: {}", filename, response);
        } catch (Exception e) {
            log.error("电采暖-记录单体量测上送日志到数据库失败: {}", ExceptionUtils.getStackTrace(e));
        }

        return response;
    }


    // 处理量测元素数据
    private String processMeasureDataFromHistoryResp(List<BigDataHistoryResp> bigDataHistoryRespList) {
        String value = "0.0000";
        if (CollectionUtils.isEmpty(bigDataHistoryRespList)) {
            return value;
        }

        Double valueCalc = Double.valueOf(0.0D);
        for (BigDataHistoryResp bigDataHistoryResp : bigDataHistoryRespList) {
            // 取每个设备的最后一个值
            List<DataResp> dataRespList = CollectionUtils.isEmpty(bigDataHistoryResp.getDataResp()) ? Lists.newArrayList() : bigDataHistoryResp.getDataResp();
            dataRespList = dataRespList.stream().filter(x -> null != x.getValue()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(dataRespList)) {
                List<DataResp> dataRespListSort = dataRespList.stream().filter(e -> StrUtil.isNotEmpty(e.getTime()))
                        .sorted(Comparator.comparing(e -> DateUtil.parse(e.getTime())))
                        .collect(Collectors.toList());
                Double lastValue = dataRespListSort.get(dataRespList.size() - 1).getValue();
                valueCalc = valueCalc + lastValue;
            }
        }
        // 原逻辑是求15分钟内平均值
        // 和华北沟通后应为每15分钟时的总加实时值
        value = new BigDecimal(String.valueOf(valueCalc)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        return value;
    }

    private String processMeasureDataFromRealTimeResp(List<BigDataRealTimeResp> bigDataRealTimeRespList) {
        // 秒级触发事件
        LocalDateTime triggerTime = LocalDateTime.now();

        String valueStr = "0.0000";
        if (CollectionUtils.isEmpty(bigDataRealTimeRespList)) {
            return valueStr;
        }

        BigDecimal valueCalc = BigDecimal.ZERO;
        for (BigDataRealTimeResp bigDataHistoryResp : bigDataRealTimeRespList) {
            BigDecimal value = BigDecimal.ZERO;

            if (!Objects.isNull(bigDataHistoryResp) && !CollectionUtils.isEmpty(bigDataHistoryResp.getDataResp())) {
                List<DataResp> dataRespList = bigDataHistoryResp.getDataResp();
                DataResp powerData = dataRespList.get(dataRespList.size() - 1); // 取最后一个数据点
                if (Objects.isNull(powerData) || Objects.isNull(powerData.getValue()) || StringUtils.isBlank(powerData.getTime())) {
                    value = BigDecimal.ZERO;
                } else {
                    //格式为yyyy-MM-dd HH:mm:ss
                    String lastTimeStr = powerData.getTime();
                    LocalDateTime lastTime = LocalDateTime.parse(lastTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    // 大于有效性时间-置零
                    if (LocalDateTimeUtil.between( lastTime,triggerTime, ChronoUnit.SECONDS) > bigdataRealtimeInterval) {
                        value = BigDecimal.ZERO;
                    } else {
                        // 符合有效性直接返回
                        Double lastValue = powerData.getValue();
                        value = new BigDecimal(String.valueOf(lastValue));
                    }
                }
            }
            valueCalc = valueCalc.add(value);
        }
        // 原逻辑是求15分钟内平均值
        // 和华北沟通后应为每15分钟时的总加实时值
        valueStr = new BigDecimal(String.valueOf(valueCalc)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        return valueStr;
    }


    public String getFileName(String resCode, String type, String aggregatorAliasName) {
        StringBuilder stringBuilder = new StringBuilder(aggregatorAliasName);
        stringBuilder.append(resCode).append(fileSeparate).append(type).append(fileSeparate).append(getCurrentMin()).append(filePoint).append(fileSuffix);
        return stringBuilder.toString();
    }

    public String getCurrentMin() {
        DateTime dateTime = new DateTime(new Date());
        return dateTime.toString(DATE_TIME_MIN);
    }

    /**
     * 处理工业负荷命令数据
     * @param vppConfigs
     * @param channelNo
     * @param realTimeData
     * @return
     */
    private Map<String, String> processVPPCmdData(List<AggregatorEntDevice> vppConfigs, String channelNo, List<BigDataRealTimeResp> realTimeData) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return Maps.newHashMap();
        }

        // 从control_issue_log 获取最近的控制下发信息
        String issueValue = "0.000000";
        String issueStatus = "1";
        String issueSign = "1";
        ControlIssueLog controlIssueLog = controlIssueLogService.getLastLogByGroupNo(channelNo);

        if (null != controlIssueLog && null != controlIssueLog.getCmdData()) {
            JSONObject cmdData = JSONObject.parseObject(controlIssueLog.getCmdData());
            issueValue = StringUtils.isBlank(cmdData.getString(channelNo + "-1")) ? issueValue : cmdData.getString(channelNo + "-1");
            issueStatus = StringUtils.isBlank(cmdData.getString(channelNo + "-3")) ? issueStatus : cmdData.getString(channelNo + "-3");
            issueSign = StringUtils.isBlank(cmdData.getString(channelNo + "-4")) ? issueSign : cmdData.getString(channelNo + "-4");
        }

        //  根据子企业获取所有对应资源的数据
        Map<String, String> cmdData = new LinkedHashMap<>();
        // 工业负荷AGC投退状态 原逻辑 默认1
        cmdData.put(channelNo + "-1", issueStatus);
        // 工业负荷有功实发命令（冀北返回值）
        cmdData.put(channelNo + "-2", issueValue);
        // 华北系统中工业负荷厂AGC正控信号（返回值）
        cmdData.put(channelNo + "-3", issueSign);
        // 可参与调节的工业负荷终端数量
        String vppSize = String.valueOf(vppConfigs.size());
        cmdData.put(channelNo + "-4", vppSize);
        // 可参与调节的工业负荷实时有功（单位MW，以充电为﹢）
        String activePowerForVPP = getActivePowerForVPP(vppConfigs, realTimeData);
        // 聚合参与模型总加功率
        String activePowerForVPPForParticipation = getActivePowerForVPPForParticipation(vppConfigs, realTimeData);
        cmdData.put(channelNo + "-5", activePowerForVPPForParticipation);
        // 可参与调节的工业负荷按当前功率最大可持续时间
        cmdData.put(channelNo + "-6", "0");
        // 可参与调节的工业负荷终端功率上限（最大可充）
        cmdData.put(channelNo + "-7", "0");
        // 可参与调节的工业负荷终端功率下限（最大可放）
        cmdData.put(channelNo + "-8", "0");
        // 工业负荷整体运行模式（0仅可充，1仅可放，2可充可放） 华北回复使用1
        cmdData.put(channelNo + "-9", "1");
        // 工业负荷最大允许命令步长
        cmdData.put(channelNo + "-10", "0");
        // 工业负荷类型数量
        cmdData.put(channelNo + "-11", vppSize);
        // 工业负荷类型实时有功
        cmdData.put(channelNo + "-12", activePowerForVPP);

        return cmdData;
    }

    /**
     * 获取工业负荷聚合参与模型总加功率
     * @param vppConfigs
     * @param realTimeData
     * @return
     */
    private String getActivePowerForVPPForParticipation(List<AggregatorEntDevice> vppConfigs, List<BigDataRealTimeResp> realTimeData) {
        if (CollectionUtils.isEmpty(realTimeData)) {
            return "0.0000";
        }

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }
        // 不上送模型企业
        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(vppConfigs.get(0).getAggregatorId(), vppConfigs.get(0).getResourceTypeId(), noUpModelEnergyStationCodes);
        // 参与模型energy_station_code 集合
        if (CollectionUtil.isEmpty(modelInfoList)) {
            log.warn("工业负荷-查询参与模型为空, 能源类型：{}", vppConfigs.get(0).getResourceTypeId());
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }
        List<String> energyStationCodes = modelInfoList.stream().filter(o -> "1".equals(o.getControll())).map(AggregatorSingleModelData::getEnergyStationCode).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(energyStationCodes)) {
            log.warn("工业负荷-参与模型能源站为空，返回0");
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }
        // modify by sl 修复工业负荷参与功率计算：只计算属于参与模型能源站的设备，而不是整个站点的所有设备
        // 构建参与模型能源站的Map，用于快速查找（只包含controll="1"的能源站）
        Map<String, AggregatorSingleModelData> participationEnergyStationMap = modelInfoList.stream()
                .filter(o -> "1".equals(o.getControll()))
                .collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));
        // 获取参与模型能源站下的所有设备ID列表（确保设备所属的能源站在参与模型Map中存在，且energyStationCode不为空）
        // modify by sl 修复：设备ID需要拼接stationId，避免不同企业的相同设备ID重复
        List<String> participationDeviceIds = vppConfigs.stream()
                .filter(aggregatorEntDevice -> {
                    String energyStationCode = aggregatorEntDevice.getEnergyStationCode();
                    // 确保energyStationCode不为空，且在参与模型Map中存在（controll="1"）
                    return StringUtils.isNotBlank(energyStationCode) && participationEnergyStationMap.containsKey(energyStationCode);
                })
                .map(aggregatorEntDevice -> aggregatorEntDevice.getStationId() + "_" + aggregatorEntDevice.getDeviceId())
                .distinct()
                .collect(Collectors.toList());
        log.info("工业负荷-参与模型能源站数量: {}, 总设备数量: {}, 参与设备数量: {}", energyStationCodes.size(), vppConfigs.size(), participationDeviceIds.size());
        // realTimeData 过滤：只保留属于参与模型能源站的设备数据（使用stationId_equipMK_equipID格式匹配）
        realTimeData = realTimeData.stream()
                .filter(resp -> participationDeviceIds.contains(resp.getStaId() + "_" + resp.getEquipMK() + "_" + resp.getEquipID()))
                .collect(Collectors.toList());
        // 按站点和设备排序
        Map<String, BigDataRealTimeResp> map = realTimeData.stream()
                .collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));
        int mapSize = CollectionUtil.size(map);
        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> dataRespList = v.getDataResp();
            if (!CollectionUtils.isEmpty(dataRespList)) {
                DataResp powerData = dataRespList.get(dataRespList.size() - 1); // 取最后一个数据点
                BigDecimal value = processTotalPowerData(powerData);
                activePower[0] = MathUtils.add(activePower[0], value);
            }
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 获取工业负荷总有功功率（可调节的）
     * @param vppConfigs
     * @param realTimeData
     * @return
     */
    private String getActivePowerForVPP(List<AggregatorEntDevice> vppConfigs, List<BigDataRealTimeResp> realTimeData) {

        if (CollectionUtils.isEmpty(realTimeData)) {
            return "0.0000";
        }

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }
        // 使用和processMeasureDataFromRealTimeResp相同的时间基准和处理逻辑，保证同一时间数据一致
        // 过滤出P数据
        List<BigDataRealTimeResp> pDataList = realTimeData.stream()
                .filter(Objects::nonNull)
                .filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp()))
                .collect(Collectors.toList());
        // 使用processMeasureDataFromRealTimeResp方法处理，保证时间基准一致
        String totalPowerStr = processMeasureDataFromRealTimeResp(pDataList);
        // processMeasureDataFromRealTimeResp返回的是KW，需要转换为MW
        BigDecimal totalPower = new BigDecimal(totalPowerStr);
        return totalPower.divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 工业负荷量测数据推送
     * @param vppConfigs
     * @param channelNo
     * @param resourceCode
     * @param aggregatorId
     * @param pRealTimeData
     * @return
     */
    private String deliveryMeasDataVPP(List<AggregatorEntDevice> vppConfigs, String channelNo, String resourceCode, String aggregatorId, List<BigDataRealTimeResp> pRealTimeData) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return "VPP has no config data";
        }
        // 获取数据库配置企业信息
        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntListByResourTypeId(channelNo);
        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();

        List<Object> singleMeasData = Lists.newArrayList();
        RealTimeReq paramSingle = getParamSingle(vppConfigs);

        List<BigDataRealTimeResp> singleRealTimeData = null;
        String keyVppMeas = "loadAggregatorDelivery"+aggregatorId + ":VPP:MEAS";
        try {
            singleRealTimeData = bigDataHandlerService.getRealTime(paramSingle, "0");
            log.info("工业负荷-量测查询Q/Ia/Eptp数据条数: {}", CollectionUtils.isEmpty(singleRealTimeData) ? 0 : singleRealTimeData.size());

            if (CollectionUtil.isNotEmpty(pRealTimeData)) {
                if (singleRealTimeData == null) {
                    singleRealTimeData = new ArrayList<>();
                }
                log.info("工业负荷-量测合并P数据前，当前数据条数: {}", singleRealTimeData.size());
                log.info("工业负荷-量测P数据条数: {}", pRealTimeData.size());
                singleRealTimeData.addAll(pRealTimeData);
                log.info("工业负荷-量测合并P数据后，总数据条数: {}", singleRealTimeData.size());
            }
            if (CollectionUtils.isNotEmpty(singleRealTimeData)) {
                redisUtil.set(keyVppMeas, singleRealTimeData, bigdataRealtimeInterval);
            }
        } catch (Exception e) {
            log.warn("工业负荷-单体量测-实时数据查询异常:参数{}", JSONObject.toJSONString(paramSingle));
            log.warn("工业负荷-单体量测-实时数据查询异常:异常信息{}", ExceptionUtils.getStackTrace(e));
            Object singleResult = redisUtil.get(keyVppMeas);
            if(Objects.isNull(singleResult)){
                singleRealTimeData = new ArrayList<>();
                log.warn("工业负荷-从缓存获取最新量测值为空");
            }else{
                singleRealTimeData = (List<BigDataRealTimeResp>) redisUtil.get(keyVppMeas);
                log.warn("工业负荷-从缓存获取最新量测值:{}", JSONObject.toJSONString(singleRealTimeData));
            }
        }

        // 确保 singleRealTimeData 不为 null
        if (singleRealTimeData == null) {
            singleRealTimeData = new ArrayList<>();
            log.warn("工业负荷-实时数据为空，初始化为空列表");
        }

        log.info("工业负荷-最终量测数据条数: {}", singleRealTimeData.size());

        // 增加不上送模型
        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, channelNo, noUpModelEnergyStationCodes);
        log.info("工业负荷-查询到参与模型数量: {}", modelInfoList.size());
        Map<String, AggregatorSingleModelData> energyStationMap = modelInfoList.stream().collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));
        // 按systemCode 归并
        Map<String, List<BigDataRealTimeResp>> mapGroupingByStationId = singleRealTimeData.stream().collect(Collectors.groupingBy(BigDataRealTimeResp::getStaId));
        log.info("工业负荷-按站点分组后，站点数量: {}", mapGroupingByStationId.size());
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        //按能源站合并
        Map<String, List<BigDataRealTimeResp>> mapGroupingByEnergyStationId = new HashMap<>();
        for (Map.Entry<String, List<BigDataRealTimeResp>> systemCodeResult : mapGroupingByStationId.entrySet()) {
            String systemCodea = systemCodeResult.getKey();
            List<BigDataRealTimeResp> value = systemCodeResult.getValue();
            List<String> energyStationCodeLists = vppConfigs.stream().filter(e -> StrUtil.equals(e.getStationId(), systemCodea)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = vppConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }

                List<BigDataRealTimeResp> energyStationResult = value.stream().filter(e -> energyStationDeviceIds.contains(e.getEquipMK() + "_" + e.getEquipID())).collect(Collectors.toList());
                mapGroupingByEnergyStationId.put(energyStationCode, energyStationResult);
            }
        }
        log.info("工业负荷-按能源站合并后，能源站数量: {}", mapGroupingByEnergyStationId.size());

        mapGroupingByEnergyStationId.forEach((k, v) -> {
            log.info("工业负荷-处理能源站: {}, 数据条数: {}", k, v.size());
            Map<String, String> map = new HashMap<>(16);

            // P 总有功功率
            List<BigDataRealTimeResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            log.info("工业负荷-能源站{}, P数据条数: {}", k, bigDataHistoryRespListP.size());
            String totalActivePower = processMeasureDataFromRealTimeResp(bigDataHistoryRespListP);
            log.info("工业负荷-能源站{}, P总有功功率: {}", k, totalActivePower);

            // Q 总无功功率
            List<BigDataRealTimeResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            log.info("工业负荷-能源站{}, Q数据条数: {}", k, bigDataHistoryRespListQ.size());
            String totalReactivePower = processMeasureDataFromRealTimeResp(bigDataHistoryRespListQ);

            // Ia A相电流
            List<BigDataRealTimeResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            log.info("工业负荷-能源站{}, Ia数据条数: {}", k, bigDataHistoryRespListIa.size());
            String userElecCurrent = processMeasureDataFromRealTimeResp(bigDataHistoryRespListIa);

            // Eptp 有功电度正向量（）
            List<BigDataRealTimeResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            log.info("工业负荷-能源站{}, Eptp数据条数: {}", k, bigDataHistoryRespListEPTP.size());
            String todayZeroElecQuantity = processMeasureDataFromRealTimeResp(bigDataHistoryRespListEPTP);

            if (energyStationMap.get(k) != null) {
                map.put("username", energyStationMap.get(k).getEnergyStation());
                map.put("userActivePower", totalActivePower);
                map.put("userReactivePower", totalReactivePower);
                map.put("userElecCurrent", userElecCurrent);
                map.put("todayZeroElecQuantity", todayZeroElecQuantity);
                map.put("innerStationId", energyStationMap.get(k).getEnergyStationCode());

                log.info("工业负荷-能源站{}, 量测数据: P={}, Q={}, Ia={}, Eptp={}", k, totalActivePower, totalReactivePower, userElecCurrent, todayZeroElecQuantity);
                singleMeasData.add(map);
                singleMeasDataMap.put(k, map);
            } else {
                log.warn("工业负荷-能源站{} 在energyStationMap中不存在", k);
            }
        });

        energyStationMap.forEach((energyStationCode, energyStationInfo) -> {
            // 大数据平台数据缺失，无返回时
            if (!singleMeasDataMap.containsKey(energyStationCode)) {
                log.warn("工业负荷-能源站{} 大数据无返回，填充0值", energyStationCode);
                Map<String, String> map = new HashMap<>(8);
                map.put("username", energyStationInfo.getEnergyStation());
                map.put("userActivePower", "0.0000");
                map.put("userReactivePower", "0.0000");
                map.put("userElecCurrent", "0.0000");
                map.put("todayZeroElecQuantity", "0.0000");
                map.put("innerStationId", energyStationInfo.getEnergyStationCode());

                singleMeasData.add(map);
            }
        });
        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);

        log.info("工业负荷-最终量测数据条数: {}", singleMeasData.size());
        log.info("工业负荷-最终量测数据详情: {}", JSONObject.toJSONString(singleMeasData));

        String filename = getFileName(resourceCode, "MEAS", aggregatorAliasName);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MEAS", resourceCode).getName();
        Map<String, Object> map = new HashMap<>(16);
        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
        map.put("company", aggregatorAliasName);
        String response = null;
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();
            log.info("工业负荷-单体量测数据-templateData:{}", templateData);
            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
            String encodeString = Base64.getEncoder().encodeToString(bytes);
            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
            response = greeter.commitFile(filename, encodeString);
            log.info("工业负荷-聚合商" + aggregatorId + "资源Id" + channelNo + "单体量测数据上送成功");
        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        } finally {
            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
            singleMeasDeliveryLog.setFileName(filename);
            // 日志入库为原始请求报文
            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
        }

        return response;
    }

    /**
     * 工业负荷量测数据推送（带重试）
     */
    private String deliveryMeasDataVPPWithRetry(List<AggregatorEntDevice> vppConfigs, String channelNo, String resourceCode, String aggregatorId, List<BigDataRealTimeResp> pRealTimeData) {
        if (CollectionUtils.isEmpty(vppConfigs)) {
            return "VPP has no config data";
        }

        // 准备数据（这部分不需要重试）
        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntListByResourTypeId(channelNo);
        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
        List<Object> singleMeasData = Lists.newArrayList();
        RealTimeReq paramSingle = getParamSingle(vppConfigs);

        List<BigDataRealTimeResp> singleRealTimeData = null;
        String keyVppMeas = "loadAggregatorDelivery"+aggregatorId + ":VPP:MEAS";
        try {
            singleRealTimeData = bigDataHandlerService.getRealTime(paramSingle, "0");
            log.info("工业负荷-量测查询Q/Ia/Eptp数据条数: {}", CollectionUtils.isEmpty(singleRealTimeData) ? 0 : singleRealTimeData.size());

            if (CollectionUtil.isNotEmpty(pRealTimeData)) {
                if (singleRealTimeData == null) {
                    singleRealTimeData = new ArrayList<>();
                }
                log.info("工业负荷-量测合并P数据前，当前数据条数: {}", singleRealTimeData.size());
                log.info("工业负荷-量测P数据条数: {}", pRealTimeData.size());
                singleRealTimeData.addAll(pRealTimeData);
                log.info("工业负荷-量测合并P数据后，总数据条数: {}", singleRealTimeData.size());
            }
            if (CollectionUtils.isNotEmpty(singleRealTimeData)) {
                redisUtil.set(keyVppMeas, singleRealTimeData, bigdataRealtimeInterval);
            }
        } catch (Exception e) {
            log.warn("工业负荷-单体量测-实时数据查询异常:参数{}", JSONObject.toJSONString(paramSingle));
            log.warn("工业负荷-单体量测-实时数据查询异常:异常信息{}", ExceptionUtils.getStackTrace(e));
            Object singleResult = redisUtil.get(keyVppMeas);
            if(Objects.isNull(singleResult)){
                singleRealTimeData = new ArrayList<>();
                log.warn("工业负荷-从缓存获取最新量测值为空");
            }else{
                singleRealTimeData = (List<BigDataRealTimeResp>) redisUtil.get(keyVppMeas);
                log.warn("工业负荷-从缓存获取最新量测值:{}", JSONObject.toJSONString(singleRealTimeData));
            }
        }

        if (singleRealTimeData == null) {
            singleRealTimeData = new ArrayList<>();
            log.warn("工业负荷-实时数据为空，初始化为空列表");
        }

        log.info("工业负荷-最终量测数据条数: {}", singleRealTimeData.size());

        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, channelNo, noUpModelEnergyStationCodes);
        log.info("工业负荷-查询到参与模型数量: {}", modelInfoList.size());
        Map<String, AggregatorSingleModelData> energyStationMap = modelInfoList.stream().collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));

        Map<String, List<BigDataRealTimeResp>> mapGroupingByStationId = singleRealTimeData.stream().collect(Collectors.groupingBy(BigDataRealTimeResp::getStaId));
        log.info("工业负荷-按站点分组后，站点数量: {}", mapGroupingByStationId.size());
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        Map<String, List<BigDataRealTimeResp>> mapGroupingByEnergyStationId = new HashMap<>();
        for (Map.Entry<String, List<BigDataRealTimeResp>> systemCodeResult : mapGroupingByStationId.entrySet()) {
            String systemCodea = systemCodeResult.getKey();
            List<BigDataRealTimeResp> value = systemCodeResult.getValue();
            List<String> energyStationCodeLists = vppConfigs.stream().filter(e -> StrUtil.equals(e.getStationId(), systemCodea)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = vppConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }
                List<BigDataRealTimeResp> energyStationResult = value.stream().filter(e -> energyStationDeviceIds.contains(e.getEquipMK() + "_" + e.getEquipID())).collect(Collectors.toList());
                mapGroupingByEnergyStationId.put(energyStationCode, energyStationResult);
            }
        }
        log.info("工业负荷-按能源站合并后，能源站数量: {}", mapGroupingByEnergyStationId.size());

        mapGroupingByEnergyStationId.forEach((k, v) -> {
            log.info("工业负荷-处理能源站: {}, 数据条数: {}", k, v.size());
            Map<String, String> map = new HashMap<>(16);
            List<BigDataRealTimeResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            log.info("工业负荷-能源站{}, P数据条数: {}", k, bigDataHistoryRespListP.size());
            String totalActivePower = processMeasureDataFromRealTimeResp(bigDataHistoryRespListP);
            log.info("工业负荷-能源站{}, P总有功功率: {}", k, totalActivePower);
            List<BigDataRealTimeResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            log.info("工业负荷-能源站{}, Q数据条数: {}", k, bigDataHistoryRespListQ.size());
            String totalReactivePower = processMeasureDataFromRealTimeResp(bigDataHistoryRespListQ);
            List<BigDataRealTimeResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            log.info("工业负荷-能源站{}, Ia数据条数: {}", k, bigDataHistoryRespListIa.size());
            String userElecCurrent = processMeasureDataFromRealTimeResp(bigDataHistoryRespListIa);
            List<BigDataRealTimeResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && !Objects.isNull(x.getDataResp())).collect(Collectors.toList());
            log.info("工业负荷-能源站{}, Eptp数据条数: {}", k, bigDataHistoryRespListEPTP.size());
            String todayZeroElecQuantity = processMeasureDataFromRealTimeResp(bigDataHistoryRespListEPTP);

            if (energyStationMap.get(k) != null) {
                map.put("username", energyStationMap.get(k).getEnergyStation());
                map.put("userActivePower", totalActivePower);
                map.put("userReactivePower", totalReactivePower);
                map.put("userElecCurrent", userElecCurrent);
                map.put("todayZeroElecQuantity", todayZeroElecQuantity);
                map.put("innerStationId", energyStationMap.get(k).getEnergyStationCode());
                log.info("工业负荷-能源站{}, 量测数据: P={}, Q={}, Ia={}, Eptp={}", k, totalActivePower, totalReactivePower, userElecCurrent, todayZeroElecQuantity);
                singleMeasData.add(map);
                singleMeasDataMap.put(k, map);
            } else {
                log.warn("工业负荷-能源站{} 在energyStationMap中不存在", k);
            }
        });

        energyStationMap.forEach((energyStationCode, energyStationInfo) -> {
            if (!singleMeasDataMap.containsKey(energyStationCode)) {
                log.warn("工业负荷-能源站{} 大数据无返回，填充0值", energyStationCode);
                Map<String, String> map = new HashMap<>(8);
                map.put("username", energyStationInfo.getEnergyStation());
                map.put("userActivePower", "0.0000");
                map.put("userReactivePower", "0.0000");
                map.put("userElecCurrent", "0.0000");
                map.put("todayZeroElecQuantity", "0.0000");
                map.put("innerStationId", energyStationInfo.getEnergyStationCode());
                singleMeasData.add(map);
            }
        });
        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);

        log.info("工业负荷-最终量测数据条数: {}", singleMeasData.size());
        log.info("工业负荷-最终量测数据详情: {}", JSONObject.toJSONString(singleMeasData));

        String filename = getFileName(resourceCode, "MEAS", aggregatorAliasName);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MEAS", resourceCode).getName();
        Map<String, Object> map = new HashMap<>(16);
        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
        map.put("company", aggregatorAliasName);

        // 上送电网（带重试）- 重试时使用第一次准备的数据，不重新获取
        String response = null;
        String encodeString = null; // 保存第一次生成的编码数据，重试时直接使用
        // 记录开始时间，确保重试也在当前分钟内完成
        long startTime = System.currentTimeMillis();
        long timeoutMillis = deliveryTimeoutMillis; // 从配置中心获取超时时间

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 检查是否超时，如果超时则不再重试
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= timeoutMillis) {
                    log.error("工业负荷-单体量测上送超时，已用时: {}ms，超过55秒限制，不再重试", elapsedTime);
                    response = "失败:超过1分钟时间限制";
                    break;
                }

                if (attempt == 1) {
                    log.info("工业负荷-单体量测-开始上送电网（初始尝试），聚合商ID: {}, 资源ID: {}, 文件名: {}", aggregatorId, channelNo, filename);
                } else {
                    long remainingTime = timeoutMillis - elapsedTime;
                    log.info("工业负荷-单体量测-开始上送电网（第{}次重试），聚合商ID: {}, 资源ID: {}, 文件名: {}, 剩余时间: {}ms, 使用第一次准备的数据",
                            attempt - 1, aggregatorId, channelNo, filename, remainingTime);
                }

                // 第一次尝试时生成模板数据，重试时直接使用第一次的数据
                if (encodeString == null) {
                    ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
                    if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                        response = StatusCode.F_A.getMsg();
                        throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
                    }
                    String templateData = templateResult.getData();
                    log.info("工业负荷-单体量测数据-templateData:{}", templateData);
                    byte[] bytes = templateData.getBytes(Charsets.UTF_8);
                    encodeString = Base64.getEncoder().encodeToString(bytes);
                    log.info("工业负荷-单体量测数据-编码后数据长度: {}", encodeString.length());
                } else {
                    log.info("工业负荷-单体量测-重试使用第一次准备的数据，编码数据长度: {}", encodeString.length());
                }

                Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
                response = greeter.commitFile(filename, encodeString);
                long totalTime = System.currentTimeMillis() - startTime;
                if (attempt == 1) {
                    log.info("工业负荷-单体量测数据上送成功（初始尝试），耗时: {}ms, 聚合商: {}, 资源Id: {}, 响应: {}", totalTime, aggregatorId, channelNo, response);
                } else {
                    log.info("工业负荷-单体量测数据上送成功（第{}次重试），耗时: {}ms, 聚合商: {}, 资源Id: {}, 响应: {}", attempt - 1, totalTime, aggregatorId, channelNo, response);
                }
                // 成功则跳出循环
                break;
            } catch (Exception e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (attempt == 1) {
                    log.warn("工业负荷-单体量测数据上送失败（初始尝试），已用时: {}ms, 异常信息: {}", elapsedTime, ExceptionUtils.getStackTrace(e));
                    // 检查剩余时间是否足够重试
                    if (elapsedTime < timeoutMillis && attempt < maxRetries) {
                        log.info("工业负荷-单体量测-准备进行第1次重试，剩余时间: {}ms, 将使用第一次准备的数据，文件名: {}",
                                timeoutMillis - elapsedTime, filename);
                    } else {
                        // 剩余时间不足或已达到最大重试次数，设置失败响应
                        if (elapsedTime >= timeoutMillis) {
                            log.error("工业负荷-单体量测-剩余时间不足，不再重试");
                            response = "失败:超过1分钟时间限制";
                        } else {
                            log.error("工业负荷-单体量测-已达到最大重试次数，不再重试");
                            response = "失败:" + e.getMessage();
                        }
                        break;
                    }
                } else {
                    log.error("工业负荷-单体量测数据上送失败（第{}次重试），已用时: {}ms, 已达到最大重试次数，异常信息: {}",
                            attempt - 1, elapsedTime, ExceptionUtils.getStackTrace(e));
                    response = "失败:" + e.getMessage();
                }
            }
        }

        // 确保response有值（防止所有尝试都失败但response仍为null的情况）
        if (response == null) {
            response = "失败:未知错误";
            log.error("工业负荷-单体量测上送失败，response为null，设置为默认失败状态");
        }

        // 记录日志（无论成功或失败都保存）
        try {
            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
            singleMeasDeliveryLog.setFileName(filename);
            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
            log.info("工业负荷-单体量测上送日志已记录到数据库，文件名: {}, status: {}", filename, response);
        } catch (Exception e) {
            log.error("工业负荷-记录单体量测上送日志到数据库失败: {}", ExceptionUtils.getStackTrace(e));
        }

        return response;
    }
}
