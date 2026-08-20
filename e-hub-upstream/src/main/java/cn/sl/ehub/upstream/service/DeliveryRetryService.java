package cn.sl.ehub.upstream.service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import cn.sl.ehub.upstream.dto.HistoryReq;
import cn.sl.ehub.upstream.dto.OpentsdbReq;
import cn.sl.ehub.upstream.dto.BigDataHistoryResp;
import cn.sl.ehub.upstream.dto.TagVO;
import cn.sl.ehub.upstream.service.BigDataHandlerService;
import cn.sl.ehub.upstream.config.ClientConfig;
import cn.sl.ehub.common.enums.EnergyModelEnumNew;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.enums.TemplateNameNewEnum;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.req.SingleMeasDeliveryReq;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorInfo;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.SingleMeasDeliveryLog;
import cn.sl.ehub.upstream.ws.Greeter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sl
 * @description 上送服务
 * @email ouyushan@hotmail.com
 * @date 2026-05-28
 */
@Service
@Slf4j
public class DeliveryRetryService {

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

    @Value("${aggregator.name}")
    private String aggregatorName;

    @Value("${file.separate}")
    private String fileSeparate;

    @Value("${file.point}")
    private String filePoint;

    @Value("${file.suffix}")
    private String fileSuffix;

    @Value("${model.no.up.data:}")
    private String noUpModelEnergyStationCode;

    @Value("${device.no.up.data:}")
    private String noUpDeviceStationIds;

    @Value("${nari.url.single}")
    private List<String> singleModelAndMeasUrl;

    private static final String LOCAL_FILE_PATH_MEASE = System.getProperty("user.dir") + File.separator + "huabei" + File.separator + "MEASE" + File.separator;
    private static final String LOCAL_FILE_PATH_MODEL = System.getProperty("user.dir") + File.separator + "huabei" + File.separator + "MODEL" + File.separator;
    private static final String LOCAL_FILE_PATH_TOTAL = System.getProperty("user.dir") + File.separator + "huabei" + File.separator + "TOTAL" + File.separator;


    @Resource
    private BigDataHandlerService bigDataHandlerService;
    @Resource
    private QueryService queryService;
    @Resource
    private FreemarkerService freemarkerService;
    @Resource
    private ClientConfig clientConfig;
    @Resource
    private cn.sl.ehub.service.service.AggregatorSingleModelDataService aggregatorSingleModelDataService;
    @Resource
    private cn.sl.ehub.service.service.SingleMeasDeliveryLogService singleMeasDeliveryLogService;
    @Resource
    private cn.sl.ehub.service.service.AggregatorEntDeviceService aggregatorEntDeviceService;
    @Resource
    private GridDeliveryAuditService gridDeliveryAuditService;

    /**
     *
     * <单体量测补招><功能具体实现>
     *
     * @create：2024/10/28 19:24
     * @author sl
     * @param aggregatorId
     * @param resourceTypeId
     * @param time
     * @return void
     */
    public ResultVO<String> singleMeasRetry(String aggregatorId, String resourceTypeId, Long time) {
        log.info("单体量测补招-singleMeasRetry aggregatorId: {}, resourceTypeId: {}, time: {}", aggregatorId, resourceTypeId, time);
        if (StringUtils.isBlank(aggregatorId) || StringUtils.isBlank(resourceTypeId) || time == null) {
            return ResultVO.fail(StatusCode.C.getCode(), "聚合商、资源类型和补送时刻不能为空");
        }
        // 资源id-资源类型map
        List<String> stationIds = Arrays.asList(noUpDeviceStationIds.split(","));
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineEntDeviceListByAggregatorId(aggregatorId, stationIds);
        //  key为资源类型id
        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));
        if (MapUtil.isEmpty(configMapByResourceType)) {
            return ResultVO.fail(StatusCode.E_B.getCode(), "没有可补送的在线设备");
        }

        // 获取资源类型名称
        List<AggregatorResourceType> aggregatorResourceTypeList = queryService.getAggregatorResourceTypeListByAggregatorId(aggregatorId);
        Map<String, String> resourTypeAndNameMap = aggregatorResourceTypeList.stream().collect(Collectors.toMap(AggregatorResourceType::getId, AggregatorResourceType::getName));
        String resourType = resourTypeAndNameMap.get(resourceTypeId);

        List<AggregatorEntDevice> configs = configMapByResourceType.getOrDefault(resourceTypeId, Lists.newArrayList());
        if (CollectionUtils.isEmpty(configs)) {
            return ResultVO.fail(StatusCode.E_B.getCode(), "所选资源类型没有可补送设备");
        }

        // 根据资源类型名称获取资源类型编码
        Map<String, String> resourTypeAndCodeMap = EnergyModelEnumNew.getEnergyMap();
        String actualResourceCode = resourTypeAndCodeMap.get(resourType);
        if (StrUtil.isBlank(actualResourceCode)) {
            log.warn("单体量测补招-未找到资源类型编码，resourceTypeId: {}, resourType: {}", resourceTypeId, resourType);
            return ResultVO.fail(StatusCode.F_NO_GROUP.getCode(), "暂不支持该资源类型补送");
        }

        // 判断资源类型，分别处理电采暖和工业负荷
        if (StrUtil.equals(resourType, EnergyModelEnumNew.ELECTRIC_HEATING.getName())) {
            // 电采暖补招
            return this.deliveryMeasDataEHRetry(configs, resourceTypeId, actualResourceCode, aggregatorId, time);
        } else if (StrUtil.equals(resourType, EnergyModelEnumNew.INDUSTRIAL_LOAD.getName())) {
            // 工业负荷补招
            return this.deliveryMeasDataVPPRetry(configs, resourceTypeId, actualResourceCode, aggregatorId, time);
        }
        return ResultVO.fail(StatusCode.F_NO_GROUP.getCode(), "当前仅支持电采暖和工业负荷单体量测补送");
    }

    /**
     *
     * <电采暖补招数据><功能具体实现>
     *
     * @create：2024/10/28 19:25
     * @author sl
     * @param ehConfigs
     * @param resourceTypeId
     * @param resourceCode
     * @param aggregatorId
     * @param time
     * @return cn.sl.ehub.upstream.vo.ResultVO<java.lang.String>
     */
    private ResultVO<String> deliveryMeasDataEHRetry(List<AggregatorEntDevice> ehConfigs, String resourceTypeId, String resourceCode, String aggregatorId, Long time) {

        if (CollectionUtils.isEmpty(ehConfigs)) {
            return ResultVO.success("EH has no config data");
        }

        // 根据聚合商id获取聚合商信息
        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        // 聚合商名称
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        List<String> statioCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        // 根据聚合商id、资源类型id获取单体模型信息
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, resourceTypeId, statioCodes);
        log.info("单体模型信息:{}", JSONObject.toJSONString(modelInfoList));
        // 根据能源站编码进行分组
        Map<String, AggregatorSingleModelData> energyStationMap = modelInfoList.stream().collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();

        List<Object> singleMeasData = Lists.newArrayList();
        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForEHRetry(time, ehConfigs));
        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForEHRetry(time, ehConfigs));

        // 按systemCode 归并
//        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));

        // 按systemCode进行分组
        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
        // 单体模型量测数据
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        // 按充电站合并
        Map<String, List<BigDataHistoryResp>> mapGroupingByEnergyStationId = new HashMap<>();
        for (Map.Entry<String, List<BigDataHistoryResp>> systemCodeResult : mapGroupingByStationId.entrySet()) {
            String systemCodea = systemCodeResult.getKey();
            List<BigDataHistoryResp> value = systemCodeResult.getValue();
            // 能源站编码
            List<String> energyStationCodeLists = ehConfigs.stream().filter(e -> StrUtil.equals(e.getStationId(), systemCodea)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            // 遍历能源站编码 获取 设备
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = ehConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }
                // 获取对应设备的实时数据
                List<BigDataHistoryResp> energyStationResult = value.stream().filter(e -> energyStationDeviceIds.contains(e.getEquipMK() + "_" + e.getEquipID())).collect(Collectors.toList());
                mapGroupingByEnergyStationId.put(energyStationCode, energyStationResult);
            }
        }
        log.info("按充电站合并:{}", JSONObject.toJSONString(mapGroupingByEnergyStationId));

        mapGroupingByEnergyStationId.forEach((k, v) -> {
            Map<String, String> map = new HashMap<>(16);
            // P 总有功功率
            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String totalActivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListP);

            // Q 总无功功率
            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String totalReactivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListQ);

            // Ia A相电流
            List<BigDataHistoryResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String userElecCurrent = processMeasureDataFromHistoryResp(bigDataHistoryRespListIa);

            // Eptp 有功电度正向量（）
            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
                });
            }
            String todayZeroElecQuantity = elecQuantity[0].setScale(4, RoundingMode.HALF_UP).toString();

            if (energyStationMap.get(k) != null) {

                map.put("username", energyStationMap.get(k).getEnergyStation());
                map.put("userActivePower", totalActivePower);
                map.put("userReactivePower", totalReactivePower);
                map.put("userElecCurrent", userElecCurrent);
                map.put("todayZeroElecQuantity", todayZeroElecQuantity);
                map.put("innerStationId", k);

                singleMeasData.add(map);
            }
        });

        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);

        String filename = getRetryFileName(resourceCode, "MEAS", aggregatorAliasName, time);
        log.info("单体量测补招-filename:{}", filename);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MEAS", resourceCode).getName();
        log.info("单体量测补招-tempalteName:{}", tempalteName);
        Map<String, Object> map = new HashMap<>(16);
        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
        map.put("company", aggregatorAliasName);

        ResultVO<String> result;
        String response = "成功";
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();

            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
            String encodeString = Base64.getEncoder().encodeToString(bytes);
            log.info("encodeString:{}", encodeString);

            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("filename", filename);
            dataMap.put("filebyte", encodeString);

            log.info("电采暖-单体量测补招--templateData:{}", templateData);

            // 生成文件并保存到指定目录
            String fileName = null;
            try {
                // 将时间戳转换为LocalDateTime并格式化为文件名格式
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault());
                // 文件名时间加2分钟
                LocalDateTime fileNameDateTime = dateTime.plusMinutes(2);
                String timeStr = fileNameDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
                // 构建文件名：XTJHSEH_MEAS_2025-12-12-00-00.RB
                fileName = "XTJHSEH_MEAS_" + timeStr + ".RB";

                // 指定输出目录
                String outputDirPath = "/Users/sl/Downloads/鑫泰电采暖";
                File outputDir = new File(outputDirPath);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                // 写入文件
                String filePath = outputDirPath + File.separator + fileName;
                File targetFile = new File(filePath);
                byte[] dataBytes = templateData.getBytes(StandardCharsets.UTF_8);

                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    fos.write(dataBytes);
                    fos.flush();
                }

                // 检查文件是否写入成功
                if (targetFile.exists() && targetFile.length() == dataBytes.length) {
                    log.info("文件写入成功 - 路径: {}, 大小: {} bytes", filePath, targetFile.length());
                } else {
                    log.warn("文件写入可能不完整 - 路径: {}, 期望大小: {} bytes, 实际大小: {} bytes",
                            filePath, dataBytes.length, targetFile.exists() ? targetFile.length() : 0);
                }
            } catch (Exception e) {
                log.error("保存文件失败 - 文件名: {}, 错误信息: {}", fileName, e.getMessage(), e);
            }

            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
            response = greeter.commitFile(filename, encodeString);
            log.info("聚合商" + aggregatorId + "资源Id:" + resourceTypeId + "单体量测数据上送成功");

            result = ResultVO.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
            result = ResultVO.fail(StatusCode.F_URL_UNAVAILABLE.getCode(), response);
        } finally {
            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
            singleMeasDeliveryLog.setFileName(filename);
            // 日志入库为原始请求报文
            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLog.setIssueTime(time);
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
            auditRetry(aggregatorId, resourceTypeId, time, singleMeasDeliveryReq, response);
        }

        return result;
    }

    /**
     *
     * <工业负荷补招数据><功能具体实现>
     *
     * @create：2024/12/26
     * @author sl
     * @param vppConfigs
     * @param resourceTypeId
     * @param resourceCode
     * @param aggregatorId
     * @param time
     * @return cn.sl.ehub.upstream.vo.ResultVO<java.lang.String>
     */
    private ResultVO<String> deliveryMeasDataVPPRetry(List<AggregatorEntDevice> vppConfigs, String resourceTypeId, String resourceCode, String aggregatorId, Long time) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return ResultVO.success("VPP has no config data");
        }

        // 根据聚合商id获取聚合商信息
        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        // 聚合商名称
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        List<String> statioCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        // 根据聚合商id、资源类型id获取单体模型信息
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, resourceTypeId, statioCodes);
        log.info("工业负荷-单体模型信息:{}", JSONObject.toJSONString(modelInfoList));
        // 根据能源站编码进行分组
        Map<String, AggregatorSingleModelData> energyStationMap = modelInfoList.stream().collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();

        List<Object> singleMeasData = Lists.newArrayList();
        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForEHRetry(time, vppConfigs));
        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForEHRetry(time, vppConfigs));

        // 按systemCode进行分组
        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
        // 单体模型量测数据
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        // 按能源站合并
        Map<String, List<BigDataHistoryResp>> mapGroupingByEnergyStationId = new HashMap<>();
        for (Map.Entry<String, List<BigDataHistoryResp>> systemCodeResult : mapGroupingByStationId.entrySet()) {
            String systemCodea = systemCodeResult.getKey();
            List<BigDataHistoryResp> value = systemCodeResult.getValue();
            // 能源站编码
            List<String> energyStationCodeLists = vppConfigs.stream().filter(e -> StrUtil.equals(e.getStationId(), systemCodea)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            // 遍历能源站编码 获取 设备
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = vppConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }
                // 获取对应设备的历史数据
                List<BigDataHistoryResp> energyStationResult = value.stream().filter(e -> energyStationDeviceIds.contains(e.getEquipMK() + "_" + e.getEquipID())).collect(Collectors.toList());
                mapGroupingByEnergyStationId.put(energyStationCode, energyStationResult);
            }
        }
        log.info("工业负荷-按能源站合并:{}", JSONObject.toJSONString(mapGroupingByEnergyStationId));

        mapGroupingByEnergyStationId.forEach((k, v) -> {
            Map<String, String> map = new HashMap<>(16);
            // P 总有功功率
            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String totalActivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListP);

            // Q 总无功功率
            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String totalReactivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListQ);

            // Ia A相电流
            List<BigDataHistoryResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String userElecCurrent = processMeasureDataFromHistoryResp(bigDataHistoryRespListIa);

            // Eptp 有功电度正向量
            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
                });
            }
            String todayZeroElecQuantity = elecQuantity[0].setScale(4, RoundingMode.HALF_UP).toString();

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

        // 对于模型中存在但大数据无返回的能源站，填充0值
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

        String filename = getRetryFileName(resourceCode, "MEAS", aggregatorAliasName, time);
        log.info("工业负荷-单体量测补招-filename:{}", filename);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MEAS", resourceCode).getName();
        log.info("工业负荷-单体量测补招-tempalteName:{}", tempalteName);
        Map<String, Object> map = new HashMap<>(16);
        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
        map.put("company", aggregatorAliasName);

        ResultVO<String> result;
        String response = "成功";
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();

            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
            String encodeString = Base64.getEncoder().encodeToString(bytes);
            log.info("工业负荷-单体量测补招-encodeString:{}", encodeString);

            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("filename", filename);
            dataMap.put("filebyte", encodeString);

            log.info("工业负荷-单体量测补招--templateData:{}", templateData);
            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
            response = greeter.commitFile(filename, encodeString);
            log.info("聚合商" + aggregatorId + "资源Id:" + resourceTypeId + "工业负荷单体量测数据上送成功");

            result = ResultVO.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
            result = ResultVO.fail(StatusCode.F_URL_UNAVAILABLE.getCode(), response);
        } finally {
            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
            singleMeasDeliveryLog.setFileName(filename);
            // 日志入库为原始请求报文
            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLog.setIssueTime(time);
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
            auditRetry(aggregatorId, resourceTypeId, time, singleMeasDeliveryReq, response);
        }

        return result;
    }

    private List<BigDataHistoryResp> getMeasDataByMetric(HistoryReq historyReq) {

        return bigDataHandlerService.getHistory(historyReq, "0");

    }

    private void auditRetry(String aggregatorId, String resourceTypeId, Long time,
                            SingleMeasDeliveryReq request, String response) {
        try {
            LocalDateTime businessTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault());
            gridDeliveryAuditService.recordSingleAt(aggregatorId, resourceTypeId, resourceTypeId,
                    request, response, businessTime);
        } catch (Exception ex) {
            log.error("单体量测补送审计记录失败, aggregatorId={}, resourceTypeId={}, time={}",
                    aggregatorId, resourceTypeId, time, ex);
        }
    }

    private HistoryReq getHistoryReqForEHRetry(Long time, List<AggregatorEntDevice> ehConfigs) {

        Date startTimeDate = new Date(time * 1000);
        DateTime dateTime = new DateTime(startTimeDate);
        String endTime = dateTime.toString(DATE_FORMATTER_MIN_LAST);
        String startTime = dateTime.minusMinutes(4).toString(DATE_FORMATTER_MIN);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        ehConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq1 = new OpentsdbReq();
            opentsdbReq1.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq1.setMetric("EMS.P");
            opentsdbReq1.setAggregator("last");
            TagVO tag1 = new TagVO();
            tag1.setStaId(config.getStationId());
            tag1.setEquipMK(config.getDeviceType());
            tag1.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq1.setTags(tag1);
            listQueries.add(opentsdbReq1);

            OpentsdbReq opentsdbReq2 = new OpentsdbReq();
            opentsdbReq2.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq2.setMetric("EMS.Q");
            opentsdbReq2.setAggregator("last");
            TagVO tag2 = new TagVO();
            tag2.setStaId(config.getStationId());
            tag2.setEquipMK(config.getDeviceType());
            tag2.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq2.setTags(tag2);
            listQueries.add(opentsdbReq2);

            OpentsdbReq opentsdbReq3 = new OpentsdbReq();
            opentsdbReq3.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq3.setMetric("EMS.Ia");
            opentsdbReq3.setAggregator("last");
            TagVO tag3 = new TagVO();
            tag3.setStaId(config.getStationId());
            tag3.setEquipMK(config.getDeviceType());
            tag3.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq3.setTags(tag3);
            listQueries.add(opentsdbReq3);

            OpentsdbReq opentsdbReq4 = new OpentsdbReq();
            opentsdbReq4.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq4.setMetric("EMS.Eptp");
            opentsdbReq4.setAggregator("last");
            TagVO tag4 = new TagVO();
            tag4.setStaId(config.getStationId());
            tag4.setEquipMK(config.getDeviceType());
            tag4.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq4.setTags(tag4);
            listQueries.add(opentsdbReq4);
        });

        historyReq.setListQueries(listQueries);

        return historyReq;
    }

    /**
     * key = stationId-equipMK-equipID 查当日零点电量作为基数
     */
    private Map<String, BigDecimal> getZeroEPTPMap(HistoryReq historyReq) {

        Map<String, List<OpentsdbReq>> groupingMap = historyReq.getListQueries().stream().filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric())).collect(Collectors.groupingBy(x -> x.getTags().getStaId().concat("-").concat(x.getTags().getEquipMK().concat("-").concat(x.getTags().getEquipID()))));

        String queryTime = historyReq.getStartTime();
        org.joda.time.format.DateTimeFormatter dateTimeFormatter = org.joda.time.format.DateTimeFormat.forPattern(DATE_FORMATTER_SEC);
        DateTime dateTime = DateTime.parse(queryTime, dateTimeFormatter);
        String startTime = dateTime.toString("yyyy-MM-dd 00:00:00");
        String endTime = dateTime.toString("yyyy-MM-dd 00:01:00");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        groupingMap.forEach((k, v) -> listQueries.addAll(v));

        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");
        historyReq.setListQueries(listQueries);

        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        Map<String, BigDecimal> map = new HashMap<>(16);
        bigDataHistoryRespList.forEach(resp -> {
            List<DataResp> dataRespList = resp.getDataResp();
            double result = (CollectionUtils.isEmpty(dataRespList) || null == dataRespList.get(0) || null == dataRespList.get(0).getValue()) ? 0.0 : dataRespList.get(0).getValue();
            map.put(resp.getStaId().concat("-").concat(resp.getEquipMK().concat("-").concat(resp.getEquipID())), new BigDecimal(result));
        });
        return map;
    }

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
                List<DataResp> dataRespListSort = dataRespList.stream().filter(e -> StrUtil.isNotEmpty(e.getTime())).sorted(Comparator.comparing(e -> DateUtil.parse(e.getTime()))).collect(Collectors.toList());
                Double lastValue = dataRespListSort.get(dataRespList.size() - 1).getValue();
                valueCalc = valueCalc + lastValue;
            }
        }
        // 原逻辑是求15分钟内平均值
        // 和华北沟通后应为每15分钟时的总加实时值
        value = new BigDecimal(String.valueOf(valueCalc)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        return value;
    }

    public String getRetryFileName(String resCode, String type, String aggregatorAliasName, Long time) {
        StringBuilder stringBuilder = new StringBuilder(aggregatorAliasName);
        stringBuilder.append(resCode).append(fileSeparate).append(type).append(fileSeparate).append(RetryClosestTimePoint(time)).append(filePoint).append(fileSuffix);
        return stringBuilder.toString();
    }

    public String RetryClosestTimePoint(Long time) {
        // 假设传入的时间戳是秒级的10位整数
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault());

        // 计算最近的 00、15、30、45 分钟的时间点
        LocalDateTime roundedTime = LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(), dateTime.getHour(), (dateTime.getMinute() / 15) * 15, 0);

        // 如果计算出的时间点大于原始时间，则减去 15 分钟
        if (roundedTime.isAfter(dateTime)) {
            roundedTime = roundedTime.minusMinutes(15);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        return roundedTime.format(formatter);
    }

}
