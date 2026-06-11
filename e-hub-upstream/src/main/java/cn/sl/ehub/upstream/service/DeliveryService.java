package cn.sl.ehub.upstream.service;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.sl.ehub.upstream.dto.HistoryReq;
import cn.sl.ehub.upstream.dto.OpentsdbReq;
import cn.sl.ehub.upstream.dto.BigDataHistoryResp;
import cn.sl.ehub.upstream.dto.TagVO;
import cn.sl.ehub.upstream.service.BigDataHandlerService;
import cn.sl.ehub.upstream.config.ClientConfig;
import cn.sl.ehub.common.dto.RetryIssueDTO;
import cn.sl.ehub.common.enums.EnergyModelEnum;
import cn.sl.ehub.common.enums.EnergyModelEnumNew;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.enums.TemplateNameNewEnum;
import cn.sl.ehub.common.enums.UserTypeEnums;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.req.SingleMeasDeliveryReq;
import cn.sl.ehub.common.req.SingleModelDeliveryReq;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorInfo;
import cn.sl.ehub.service.vo.AggregatorResourceType;
import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import cn.sl.ehub.service.vo.ControlIssueLog;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.service.vo.EnergyStationInfo;
import cn.sl.ehub.service.vo.PlanDeliveryLog;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.SingleMeasDeliveryLog;
import cn.sl.ehub.service.vo.SingleModelDeliveryLog;
import cn.sl.ehub.service.vo.TotalDeliveryLog;
import cn.sl.ehub.upstream.ws.Greeter;
import cn.sl.ehub.service.service.TotalDeliveryLogService;
import cn.sl.ehub.service.service.SingleModelDeliveryLogService;
import cn.sl.ehub.service.service.SingleMeasDeliveryLogService;
import cn.sl.ehub.service.service.PlanDeliveryLogService;
import cn.sl.ehub.service.service.PlanDeliveryOnOffService;
import cn.sl.ehub.service.service.ControlIssueLogService;
import cn.sl.ehub.service.service.AggregatorEntDeviceService;
import cn.sl.ehub.service.service.AggregatorEntService;
import cn.sl.ehub.service.service.AggregatorSingleModelDataService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
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
public class DeliveryService {

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

    @Value("${model.no.up.data}")
    private String noUpModelEnergyStationCode;

    @Value("${device.no.up.data}")
    private String noUpDeviceStationIds;

    private static final String LOCAL_FILE_PATH_MEASE = System.getProperty("user.dir") + File.separator + "huabei" + File.separator + "MEASE" + File.separator;
    private static final String LOCAL_FILE_PATH_MODEL = System.getProperty("user.dir") + File.separator + "huabei" + File.separator + "MODEL" + File.separator;
    private static final String LOCAL_FILE_PATH_TOTAL = System.getProperty("user.dir") + File.separator + "huabei" + File.separator + "TOTAL" + File.separator;


    @Resource
    private TotalDeliveryLogService totalDeliveryLogService;

    @Resource
    private SingleModelDeliveryLogService singleModelDeliveryLogService;

    @Resource
    private SingleMeasDeliveryLogService singleMeasDeliveryLogService;

    @Resource
    private PlanDeliveryLogService planDeliveryLogService;

    @Resource
    private FreemarkerService freemarkerService;

    @Resource
    private BigDataHandlerService bigDataHandlerService;

    @Resource
    private ClientConfig clientConfig;

    @Resource
    private PlanDeliveryOnOffService planDeliveryOnOffService;

    @Resource
    private ControlIssueLogService controlIssueLogService;

    @Resource
    private AggregatorEntDeviceService aggregatorEntDeviceService;

    @Resource
    private AggregatorEntService aggregatorEntService;
    @Resource
    private AggregatorSingleModelDataService aggregatorSingleModelDataService;


    @Autowired
    private QueryService queryService;

    @Value("${nari.url.total}")
    private List<String> totalAndDeliveryUrl;

    @Value("${nari.url.single}")
    private List<String> singleModelAndMeasUrl;

    /**
     * 大数据实时查询有效性校验间隔秒数，默认5分钟-300秒
     */
    @Value("${bigdata.realtime.interval:300}")
    private int bigdataRealtimeInterval;


    /**
     * 总加数据接入上送接口
     * <p>
     * 其中组号为华北电网提供的聚合商系统号，点号为数据交互清单中数据内容对应的序位。
     * <p>
     * 总加数据的上送周期为1分钟
     * <p>
     * 端口39090
     * </p>
     * <p>
     * 在线设备数量以所有子企业的PCS设备数量为准
     * 分布式储能在线总容量以总电表,且数值与大数据平台相反
     */
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
        Map<String, String> cmdData = new LinkedHashMap<>();
        Map<String, String> vppCmdData = new HashMap<>();
        Map<String, String> ehCmdData = new HashMap<>();
        Map<String, String> desCmdData = new HashMap<>();
        Map<String, String> cpCmdData = new HashMap<>();
        String groupNo = "";
        Iterator<String> iterator = configMapByResourceType.keySet().iterator();
        while (iterator.hasNext()) {
            String resourceId = iterator.next();
            String resourType = resourTypeAndNameMap.get(resourceId);
            if (StrUtil.equals(resourType, EnergyModelEnumNew.INDUSTRIAL_LOAD.getName())) {
                List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                vppCmdData = processVPPCmdData(vppConfigs, resourceId);
                cmdData.putAll(vppCmdData);
                if (CollectionUtils.isNotEmpty(vppConfigs)) {
                    groupNo = groupNo + resourceId + "_";
                }
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.ELECTRIC_HEATING.getName())) {
                List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                ehCmdData = processEHCmdData(ehConfigs, resourceId);
                cmdData.putAll(ehCmdData);
                if (CollectionUtils.isNotEmpty(ehConfigs)) {
                    groupNo = groupNo + resourceId + "_";
                }
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.DISTRIBUTED_ENERGY.getName())) {
                List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                desCmdData = processDESCmdData(desConfigs, resourceId);
                cmdData.putAll(desCmdData);
                if (CollectionUtils.isNotEmpty(desConfigs)) {
                    groupNo = groupNo + resourceId + "_";
                }
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.CHARGING_PILE.getName())) {
                List<AggregatorEntDevice> cpConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                cpCmdData = processCpCmdData(aggregatorId, cpConfigs, resourceId);
                cmdData.putAll(cpCmdData);
                if (CollectionUtils.isNotEmpty(cpConfigs)) {
                    groupNo = groupNo + resourceId + "_";
                }
            }
        }
//        String groupNo = getGroupNo(vppCmdData, ehCmdData, desCmdData);
        log.info("totalDataDelivery-入参:{}", JSONObject.toJSONString(cmdData));

        //数据解析成功!
        String response = null;
        try {
            Greeter greeter = clientConfig.greeter(totalAndDeliveryUrl);
            log.info("华北调用url:{}" + "55");
            response = greeter.cmd(JSONObject.toJSONString(cmdData));
            log.info(aggregatorId + "总加数据上送成功");
            resultVO = ResultVO.success(response);
        } catch (Exception e) {
            log.info("clientConfig.greeter:{}", ExceptionUtils.getMessage(e.getCause()));
            e.printStackTrace();
            response = e.getMessage();
            resultVO = ResultVO.fail(StatusCode.F_URL_UNAVAILABLE.getCode(), response);
        } finally {
            TotalDeliveryLog totalDeliveryLog = new TotalDeliveryLog();
            totalDeliveryLog.setCreateTime(new Date());
            totalDeliveryLog.setValue(JSONObject.toJSONString(cmdData));
            totalDeliveryLog.setDeliveryStatus(response);
            totalDeliveryLog.setGroupNo(groupNo);
            totalDeliveryLogService.addLog(totalDeliveryLog);
            log.info("totalDeliveryLog-日志:{}", JSONObject.toJSONString(totalDeliveryLog));
        }

        return resultVO;
    }

    private Map<String, String> processVPPCmdData(List<AggregatorEntDevice> vppConfigs, String channelNo) {

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
        String activePowerForVPP = getActivePowerForVPP(vppConfigs);
        cmdData.put(channelNo + "-5", activePowerForVPP);
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

    private Map<String, String> processEHCmdData(List<AggregatorEntDevice> ehConfigs, String channelNo) {

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
        String activePowerForEH = getActivePowerForEH(ehConfigs);
        cmdData.put(channelNo + "-5", activePowerForEH);
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

    private Map<String, String> processDESCmdData(List<AggregatorEntDevice> desConfigs, String channelNo) {

        if (CollectionUtils.isEmpty(desConfigs)) {
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

        List<AggregatorEntDevice> deviceConfigsBeijing = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_BEIJING)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsTianjin = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_TIANJIN)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsHebei = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_HEBEI)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsShanxi = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_SHANXI)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsShandong = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_SHANDONG)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsJibei = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_JIBEI)).collect(Collectors.toList());

        // 区域设备数量
        int deviceNoBeijing = deviceConfigsBeijing.size();
        int deviceNoTianjin = deviceConfigsTianjin.size();
        int deviceNoHebei = deviceConfigsHebei.size();
        int deviceNoShanxi = deviceConfigsShanxi.size();
        int deviceNoShandong = deviceConfigsShandong.size();
        int deviceNoJibei = deviceConfigsJibei.size();

        // 区域设备容量
        double capacityBeijing = CollectionUtils.isEmpty(deviceConfigsBeijing) ? 0.0 : deviceConfigsBeijing.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityBeijing = capacityBeijing / 1000;
        double capacityTianjin = CollectionUtils.isEmpty(deviceConfigsTianjin) ? 0.0 : deviceConfigsTianjin.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityTianjin = capacityTianjin / 1000;
        double capacityHebei = CollectionUtils.isEmpty(deviceConfigsHebei) ? 0.0 : deviceConfigsHebei.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityHebei = capacityHebei / 1000;
        double capacityShanxi = CollectionUtils.isEmpty(deviceConfigsShanxi) ? 0.0 : deviceConfigsShanxi.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityShanxi = capacityShanxi / 1000;
        double capacityShandong = CollectionUtils.isEmpty(deviceConfigsShandong) ? 0.0 : deviceConfigsShandong.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityShandong = capacityShandong / 1000;
        double capacityJibei = CollectionUtils.isEmpty(deviceConfigsJibei) ? 0.0 : deviceConfigsJibei.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityJibei = capacityJibei / 1000;

        // 区域设备实时功率
        BigDecimal activePowerBeijing = getActivePowerForDES(deviceConfigsBeijing);
        BigDecimal activePowerTianjin = getActivePowerForDES(deviceConfigsTianjin);
        BigDecimal activePowerHebei = getActivePowerForDES(deviceConfigsHebei);
        BigDecimal activePowerShanxi = getActivePowerForDES(deviceConfigsShanxi);
        BigDecimal activePowerShandong = getActivePowerForDES(deviceConfigsShandong);
        BigDecimal activePowerJibei = getActivePowerForDES(deviceConfigsJibei);

        //  根据子企业获取所有对应资源的数据
        Map<String, String> cmdData = new LinkedHashMap<>();

        int deviceNoJjt = deviceNoBeijing + deviceNoTianjin;
        int deviceNoHuabei = deviceNoJjt + deviceNoHebei + deviceNoShanxi + deviceNoShandong + deviceNoJibei;

        double capacityJjt = capacityBeijing + capacityTianjin;
        double capacityHuabei = capacityJjt + capacityHebei + capacityShanxi + capacityShandong + capacityJibei;

        BigDecimal activePowerJjt = activePowerBeijing.add(activePowerTianjin).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal activePowerHuabei = activePowerJjt.add(activePowerHebei).add(activePowerShanxi).add(activePowerShandong).add(activePowerJibei).setScale(4, BigDecimal.ROUND_HALF_UP);

        // 分布式储能在线数量-华北
        cmdData.put(channelNo + "-1", String.valueOf(deviceNoHuabei));
        // 分布式储能在线总容量-华北
        cmdData.put(channelNo + "-2", String.valueOf(capacityHuabei));
        // 分布式储能在线实时有功-华北
        cmdData.put(channelNo + "-3", activePowerHuabei.toString());

        // 分布式储能在线数量-京津唐(北京+天津)
        cmdData.put(channelNo + "-4", String.valueOf(deviceNoJjt));
        // 分布式储能在线总容量-京津唐(北京+天津)
        cmdData.put(channelNo + "-5", String.valueOf(capacityJjt));
        // 分布式储能在线实时有功-京津唐
        cmdData.put(channelNo + "-6", activePowerJjt.toString());

        // 分布式储能在线数量-北京(属于京津唐电网)
        cmdData.put(channelNo + "-7", String.valueOf(deviceNoBeijing));
        // 分布式储能在线总容量-北京
        cmdData.put(channelNo + "-8", String.valueOf(capacityBeijing));
        // 分布式储能在线实时有功-北京
        cmdData.put(channelNo + "-9", activePowerBeijing.toString());

        // 分布式储能在线数量-天津
        cmdData.put(channelNo + "-10", String.valueOf(deviceNoTianjin));
        // 分布式储能在线总容量-天津
        cmdData.put(channelNo + "-11", String.valueOf(capacityTianjin));
        // 分布式储能在线实时有功-天津
        cmdData.put(channelNo + "-12", activePowerTianjin.toString());

        // 分布式储能在线数量-冀北
        cmdData.put(channelNo + "-13", String.valueOf(deviceNoJibei));
        // 分布式储能在线总容量-冀北
        cmdData.put(channelNo + "-14", String.valueOf(capacityJibei));
        // 分布式储能在线实时有功-冀北
        cmdData.put(channelNo + "-15", activePowerJibei.toString());

        // 分布式储能在线数量-河北
        cmdData.put(channelNo + "-16", String.valueOf(deviceNoHebei));
        // 分布式储能在线总容量-河北
        cmdData.put(channelNo + "-17", String.valueOf(capacityHebei));
        // 分布式储能在线实时有功-河北
        cmdData.put(channelNo + "-18", activePowerHebei.toString());

        // 分布式储能在线数量-山西
        cmdData.put(channelNo + "-19", String.valueOf(deviceNoShanxi));
        // 分布式储能在线总容量-山西
        cmdData.put(channelNo + "-20", String.valueOf(capacityShanxi));
        // 分布式储能在线实时有功-山西
        cmdData.put(channelNo + "-21", activePowerShanxi.toString());

        // 分布式储能在线数量-山东
        cmdData.put(channelNo + "-22", String.valueOf(deviceNoShandong));
        // 分布式储能在线总容量-山东
        cmdData.put(channelNo + "-23", String.valueOf(capacityShandong));
        // 分布式储能在线实时有功-山东
        cmdData.put(channelNo + "-24", activePowerShandong.toString());

        // 京津唐分布式储能AGC投退状态
        cmdData.put(channelNo + "-25", issueStatus);
        // 京津唐分布式储能有功实发命令（返回值）
        cmdData.put(channelNo + "-26", issueValue);
        // 华北系统中京津唐分布式储能AGC正控信号（返回值）
        cmdData.put(channelNo + "-27", issueSign);
        // 京津唐可参与调节的分布式储能实时数量
        cmdData.put(channelNo + "-28", String.valueOf(deviceNoHuabei));
        // 京津唐可参与调节的分布式储能实时有功（单位MW，以放电为﹢）
        cmdData.put(channelNo + "-29", activePowerHuabei.toString());
        // 京津唐可参与调节的分布式储能等效SOC
        cmdData.put(channelNo + "-30", "0");
        // 京津唐可参与调节的分布式储能有功上限（最大可充）
        cmdData.put(channelNo + "-31", "0");
        // 京津唐可参与调节的分布式储能有功下限（最大可放）
        cmdData.put(channelNo + "-32", "0");
        // 京津唐可参与调节分布式储能功率最大允许命令步长
        cmdData.put(channelNo + "-33", "0");

        return cmdData;
    }

    private Map<String, String> processCpCmdData(String aggregatorId, List<AggregatorEntDevice> cpConfigs, String channelNo) {

        if (CollectionUtils.isEmpty(cpConfigs)) {
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
        List<String> areaCodes = new ArrayList<>();
        areaCodes.add("HUABEI");
        areaCodes.add("JINGJINTANG");
        areaCodes.add("BEIJING");
        areaCodes.add("TIANJIN");
        areaCodes.add("JIBEI");
        areaCodes.add("HEBEI");
        areaCodes.add("SHANXI");
        areaCodes.add("SHANDONG");
        int label = 0;
        //  根据子企业获取所有对应资源的数据
        Map<String, String> cmdData = new LinkedHashMap<>();
        for (int i = 0; i < areaCodes.size(); i++) {
            String areaCode = areaCodes.get(i);
//            List<AggregatorEntDevice> aggregatorEntDeviceByArea = queryService.getAggregatorEntDeviceByArea(aggregatorId, areaCode);
            List<AggregatorEntDevice> aggregatorEntDeviceByArea = cpConfigs.stream().filter(e -> StrUtil.equals(e.getStateGridCode(), areaCode)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(aggregatorEntDeviceByArea)) {
                label = label + 9;
                continue;
            }
            //国网运营
            List<AggregatorEntDevice> gwDeviceLists = aggregatorEntDeviceByArea.stream().filter(e -> StrUtil.equals(e.getUserType(), UserTypeEnums.GWZY.getDesc())).collect(Collectors.toList());
            label = label + 1;
            cmdData.put(channelNo + "-" + label, String.valueOf(CollectionUtils.size(gwDeviceLists)));
            //社会运营
            List<AggregatorEntDevice> socialDeviceLists = aggregatorEntDeviceByArea.stream().filter(e -> StrUtil.equals(e.getUserType(), UserTypeEnums.SHYY.getDesc())).collect(Collectors.toList());
            label = label + 1;
            cmdData.put(channelNo + "-" + label, String.valueOf(CollectionUtils.size(socialDeviceLists)));
            //公用桩
            List<AggregatorEntDevice> gyzDeviceLists = aggregatorEntDeviceByArea.stream().filter(e -> Objects.isNull(e.getIsPublic())).filter(e -> e.getIsPublic()).collect(Collectors.toList());
            label = label + 1;
            cmdData.put(channelNo + "-" + label, String.valueOf(CollectionUtils.size(gyzDeviceLists)));
            //专用桩
            List<AggregatorEntDevice> zyzDeviceLists = aggregatorEntDeviceByArea.stream().filter(e -> Objects.isNull(e.getIsPublic())).filter(e -> !e.getIsPublic()).collect(Collectors.toList());
            label = label + 1;
            cmdData.put(channelNo + "-" + label, String.valueOf(CollectionUtils.size(zyzDeviceLists)));
            //交流桩
            List<AggregatorEntDevice> jlDeviceLists = aggregatorEntDeviceByArea.stream().filter(e -> Objects.isNull(e.getIsDirect())).filter(e -> !e.getIsDirect()).collect(Collectors.toList());
            label = label + 1;
            cmdData.put(channelNo + "-" + label, String.valueOf(CollectionUtils.size(jlDeviceLists)));
            //直流桩
            List<AggregatorEntDevice> zlzDeviceLists = aggregatorEntDeviceByArea.stream().filter(e -> Objects.isNull(e.getIsDirect())).filter(e -> e.getIsDirect()).collect(Collectors.toList());
            label = label + 1;
            cmdData.put(channelNo + "-" + label, String.valueOf(CollectionUtils.size(zlzDeviceLists)));
            //在线工作桩容量
            List<Double> powerValue = aggregatorEntDeviceByArea.stream().map(e -> e.getPower()).filter(e -> !Objects.isNull(e)).collect(Collectors.toList());
            double powerValueSum = powerValue.stream().mapToDouble(Double::doubleValue).sum();
            label = label + 1;
            cmdData.put(channelNo + "-" + label, String.valueOf(powerValueSum));
            //在线工作桩实时数量
            int size = CollectionUtils.size(aggregatorEntDeviceByArea);
            label = label + 1;
            cmdData.put(channelNo + "-" + label, String.valueOf(size));
            //在线工作桩实时有功
            String activePowerForCp = getActivePowerForCp(aggregatorEntDeviceByArea);
            label = label + 1;
            cmdData.put(channelNo + "-" + label, activePowerForCp);
        }
        //
        // 京津唐电采暖AGC投退状态
        cmdData.put(channelNo + "-73", issueStatus);
        // 京津唐电采暖有功实发命令（返回值）
        cmdData.put(channelNo + "-74", issueValue);
        // 华北系统中京津唐电采暖AGC正控信号（返回值）
        cmdData.put(channelNo + "-75", issueSign);
        // 可参与调节的京津唐电采暖实时数量
        String ehSize = String.valueOf(cpConfigs.size());
        cmdData.put(channelNo + "-76", ehSize);
        // 参与调节的京津唐电采暖实时有功（单位MW，以用电为﹢）
        String activePowerForEH = getActivePowerForCp(cpConfigs);
        cmdData.put(channelNo + "-77", activePowerForEH);
        // 京津唐可参与调节的电动车动力电池等效SOC
        cmdData.put(channelNo + "-78", "50%");
        // 京津唐可参与调节的电动汽车功率上限（最大可充）,总的装机容量和
        cmdData.put(channelNo + "-79", "0");
        // 京津唐可参与调节的电汽车功率下限（最大可放）”0“
        cmdData.put(channelNo + "-80", "0");
        // 京津唐可参与调节电动汽车运行模式（仅可充，可充可放）0仅可充
        cmdData.put(channelNo + "-81", "0");
        // 京津唐可参与调节电动汽车功率最大允许命令步长
        cmdData.put(channelNo + "-82", "0");

        return cmdData;
    }

    private String getActivePowerForVPP(List<AggregatorEntDevice> vppConfigs) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }

        String startTime = DateTime.now().minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.now().toString(DATE_FORMATTER_MIN_LAST);
        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();
        vppConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");
        // 按站点和设备排序
        Map<String, BigDataHistoryResp> map = bigDataHistoryRespList.stream().collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));

        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> powerData = v.getDataResp();
            BigDecimal value = processTotalPowerData(powerData);
            activePower[0] = MathUtils.add(activePower[0], value);
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String getActivePowerForEH(List<AggregatorEntDevice> ehConfigs) {

        if (CollectionUtils.isEmpty(ehConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }

        String startTime = DateTime.now().minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.now().toString(DATE_FORMATTER_MIN_LAST);
        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();
        ehConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });
        //入参大小
        int size = CollectionUtils.size(listQueries);

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");
        // 按站点和设备排序
        Map<String, BigDataHistoryResp> map = bigDataHistoryRespList.stream().collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));
        int mapSize = CollectionUtil.size(map);
        if (size - mapSize >= 30) {
            log.info("大数据查询缺少线程，重新调用");
            bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");
            map = bigDataHistoryRespList.stream().collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));
        }
        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> powerData = v.getDataResp();
            BigDecimal value = processTotalPowerData(powerData);
            activePower[0] = MathUtils.add(activePower[0], value);
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String getActivePowerForCp(List<AggregatorEntDevice> cpConfigs) {

        if (CollectionUtils.isEmpty(cpConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }

        String startTime = DateTime.now().minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.now().toString(DATE_FORMATTER_MIN_LAST);
        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();
        cpConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");
        // 按站点和设备排序
        Map<String, BigDataHistoryResp> map = bigDataHistoryRespList.stream().collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));

        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> powerData = v.getDataResp();
            BigDecimal value = processTotalPowerData(powerData);
            activePower[0] = MathUtils.add(activePower[0], value);
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
        List<DataResp> powerDataSort = powerData.stream().filter(e -> StrUtil.isNotEmpty(e.getTime())).sorted(Comparator.comparing(e -> DateUtil.parse(e.getTime()))).collect(Collectors.toList());
        Double lastValue = powerDataSort.get(powerData.size() - 1).getValue();
        value = new BigDecimal(String.valueOf(lastValue));

        return value;
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
        LocalDateTime lastTime = LocalDateTime.parse(lastTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 大于有效性时间-置零
        if (LocalDateTimeUtil.between(triggerTime,lastTime, ChronoUnit.SECONDS) > bigdataRealtimeInterval) {
            return value;
        }else {
            // 符合有效性直接返回
            Double lastValue = powerData.getValue();
            value = new BigDecimal(String.valueOf(lastValue));
        }

        return value;
    }

    private BigDecimal getActivePowerForDES(List<AggregatorEntDevice> desConfigs) {

        if (CollectionUtils.isEmpty(desConfigs)) {
            return BigDecimal.ZERO;
        }

        String startTime = DateTime.now().minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.now().toString(DATE_FORMATTER_MIN_LAST);
        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();
        desConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        // 按站点和设备排序
        Map<String, BigDataHistoryResp> map = bigDataHistoryRespList.stream().collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));
        // 电流为0 P为0；电流不为0，当前值为空，取前一个值
        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> powerData = v.getDataResp();
            BigDecimal value = processTotalPowerData(powerData);
            // 改变符号 分布式储能数值与大数据平台相反
            value = BigDecimal.ZERO.subtract(value);
            activePower[0] = MathUtils.add(activePower[0], value);
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP);
    }


    /**
     * 单体模型数据上送接口
     * <p>
     * 模型数据在聚合商模型发生变更时上送，上送范围为变更后的全量模型。
     * <p>
     * 端口39090
     * </p>
     * @param aggregatorId 聚合商ID
     * @param energyType 能源类型（可选），中文名称，如：电采暖、工业负荷、储能、充电桩。不传则上传全部能源类型
     */
    public String singleModelDataDelivery(String aggregatorId, String energyType) {
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getModelAggregatorEntDeviceListByAggregatorId(aggregatorId);
//        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));

        List<AggregatorResourceType> aggregatorResourceTypeListByAggregatorId = queryService.getAggregatorResourceTypeListByAggregatorId(aggregatorId);
        //       资源id-资源类型map
        Map<String, String> resourTypeAndNameMap = aggregatorResourceTypeListByAggregatorId.stream().collect(Collectors.toMap(AggregatorResourceType::getId, AggregatorResourceType::getName));
        //       资源类型名称-资源id的map（用于根据中文名称查找资源ID），如果有重复key则保留第一个
        Map<String, String> resourNameAndIdMap = aggregatorResourceTypeListByAggregatorId.stream().collect(Collectors.toMap(AggregatorResourceType::getName, AggregatorResourceType::getId, (oldValue, newValue) -> oldValue));

        Map<String, String> resourTypeAndCodeMap = EnergyModelEnumNew.getEnergyMap();
        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));
        String responseVPP = "";
        String responseEH = "";
        String responseDES = "";

        // 根据能源类型参数决定要处理的资源ID列表
        List<String> resourceIdsToProcess;
        if (StringUtils.isNotBlank(energyType)) {
            // 如果传了能源类型参数，只处理对应的资源类型
            String resourceId = resourNameAndIdMap.get(energyType);
            if (StringUtils.isBlank(resourceId)) {
                log.warn("未找到能源类型[{}]对应的资源ID，聚合商ID: {}", energyType, aggregatorId);
                return aggregatorId + " energyType [" + energyType + "] not found";
            }
            resourceIdsToProcess = Arrays.asList(resourceId);
        } else {
            // 不传参数，默认处理全部（获取所有可用的资源ID）
            resourceIdsToProcess = new ArrayList<>(resourTypeAndNameMap.keySet());
        }

        Iterator<String> iterator = resourceIdsToProcess.iterator();
        while (iterator.hasNext()) {
            String resourceId = iterator.next();
            String resourType = resourTypeAndNameMap.get(resourceId);
            if (StringUtils.isBlank(resourType)) {
                continue;
            }
            String resourceCode = resourTypeAndCodeMap.get(resourType);
            if (StrUtil.equals(resourType, EnergyModelEnumNew.INDUSTRIAL_LOAD.getName())) {
                List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                responseVPP = deliveryModelDataVPP(vppConfigs, resourceId, resourceCode, aggregatorId);
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.ELECTRIC_HEATING.getName())) {
                List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                responseEH = deliveryModelDataEH(ehConfigs, resourceId, resourceCode, aggregatorId);
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.DISTRIBUTED_ENERGY.getName())) {
                List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                responseDES = deliveryModelDataDES(desConfigs, resourceId, resourceCode, aggregatorId);
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.CHARGING_PILE.getName())) {
                List<AggregatorEntDevice> cpConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                responseDES = deliveryModelDataCp(cpConfigs, resourceId, resourceCode, aggregatorId);
            }
        }


//        List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), Lists.newArrayList());
//
//        String responseVPP = deliveryModelDataVPP(vppConfigs);
//        String responseEH = deliveryModelDataEH(ehConfigs);
//        String responseDES = deliveryModelDataDES(desConfigs);

        return responseVPP + responseEH + responseDES;
    }

    private String deliveryModelDataVPP(List<AggregatorEntDevice> vppConfigs, String channelNo, String resourceCode, String aggregatorId) {

        // if (CollectionUtils.isEmpty(vppConfigs)) {
        //     return "VPP has no config data";
        // }
        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        // 工业负荷
        SingleModelDeliveryReq singleModelDeliveryReq = new SingleModelDeliveryReq();
//        singleModelDeliveryReq.setEnergyModelEnum(EnergyModelEnum.INDUSTRIAL_LOAD);
        List<Object> singleModelData = Lists.newArrayList();

        // 按企业归并
        // Map<String, List<AggregatorEntDevice>> configMapByEntId = vppConfigs.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getEntId));
        // modify by sl 2025-10-22 增加不上送模型
        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, channelNo, noUpModelEnergyStationCodes);
        if (CollectionUtils.isEmpty(modelInfoList)) {
            return aggregatorId + "EH has no config data";
        }
        log.info("工业负荷-modelInfoList:{}", JSON.toJSONString(modelInfoList));

        for (AggregatorSingleModelData data : modelInfoList) {
            Map<String, String> entMap = new HashMap<>(16);
            entMap.put("username", data.getEnergyStation());
            entMap.put("capacity", data.getPowerCap());
            entMap.put("area", data.getArea());
            entMap.put("userType", data.getUserType());
            entMap.put("owner", data.getOwner());
            // 模型id
            entMap.put("innerStationId", data.getEnergyStationCode());
            // 工业负荷参与标识：1 表示参与
            entMap.put("participation", data.getControll());

            singleModelData.add(entMap);
        }

        singleModelDeliveryReq.setSingleModelData(singleModelData);
//        String filename = getFileName(singleModelDeliveryReq.getEnergyModelEnum(), "MODEL");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MODEL", singleModelDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();

        String filename = getFileName(resourceCode, "MODEL", aggregatorAliasName);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MODEL", resourceCode).getName();

        Map<String, Object> map = new HashMap<>(8);
        map.put("detailList", singleModelDeliveryReq.getSingleModelData());
        map.put("company", aggregatorAliasName);

        String response = null;
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();

            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
            String encodeString = Base64.getEncoder().encodeToString(bytes);
            log.info("encodeString：{}", encodeString);

            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
            response = greeter.commitFile(filename, encodeString);
        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        } finally {
            SingleModelDeliveryLog singleModelDeliveryLog = new SingleModelDeliveryLog();
            singleModelDeliveryLog.setFileName(filename);
            // 入库存原始请求报文
            singleModelDeliveryLog.setFileByte(JSONObject.toJSONString(singleModelDeliveryReq));
            singleModelDeliveryLog.setDeliveryStatus(response);
            singleModelDeliveryLog.setCreateTime(new Date());

            singleModelDeliveryLogService.addLog(singleModelDeliveryLog);
        }

        return response;
    }

    private String deliveryModelDataEH(List<AggregatorEntDevice> ehConfigs, String channelNo, String resourceCode, String aggregatorId) {

        // if (CollectionUtils.isEmpty(ehConfigs)) {
        //     return aggregatorId + "EH has no config data";
        // }

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        // 电加热
        SingleModelDeliveryReq singleModelDeliveryReq = new SingleModelDeliveryReq();
//        singleModelDeliveryReq.setEnergyModelEnum(EnergyModelEnum.ELECTRIC_HEATING);
        List<Object> singleModelData = Lists.newArrayList();
        // modify by sl 024-10-24 增加不上送模型
        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, channelNo, noUpModelEnergyStationCodes);
        if (CollectionUtils.isEmpty(modelInfoList)) {
            return aggregatorId + "EH has no config data";
        }
        log.info("电采暖-modelInfoList:{}", JSON.toJSONString(modelInfoList));
        for (AggregatorSingleModelData aggregatorSingleModelData : modelInfoList) {
            Map<String, String> entMap = new HashMap<>(16);
            entMap.put("username", aggregatorSingleModelData.getEnergyStation());
            entMap.put("capacity", aggregatorSingleModelData.getPowerCap());
            entMap.put("area", aggregatorSingleModelData.getArea());
            entMap.put("userType", aggregatorSingleModelData.getUserType());
            entMap.put("equipManufactor", aggregatorSingleModelData.getDeviceManufacture());
            entMap.put("storageType", aggregatorSingleModelData.getSaveHeat());
            entMap.put("owner", aggregatorSingleModelData.getOwner());
            entMap.put("controllable", aggregatorSingleModelData.getControll());
            // 模型id
            entMap.put("innerStationId", aggregatorSingleModelData.getEnergyStationCode());
            singleModelData.add(entMap);
        }
        // 按企业归并
//        Map<String, List<AggregatorEntDevice>> configMapByEntId = ehConfigs.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getEntId));
//
//        configMapByEntId.forEach((entId, configs) -> {
//            Map<String, String> entMap = new HashMap<>(16);
//
//            entMap.put("username", configs.get(0).getUsername());
//            entMap.put("capacity", String.valueOf(configs.stream().filter(e->!Objects.isNull(e.getPower())).mapToDouble(AggregatorEntDevice::getPower).sum()));
//            entMap.put("area", configs.get(0).getAreaCode());
//            entMap.put("userType", configs.get(0).getUserType());
//            entMap.put("equipManufactor", configs.get(0).getEquipManufactor());
//            entMap.put("storageType", configs.get(0).getUserType());
//            entMap.put("owner", configs.get(0).getUsername());
//            entMap.put("controllable", String.valueOf(configs.get(0).getControllable()));
//            // 按企业id
//            entMap.put("innerStationId", entId);
//            singleModelData.add(entMap);
//        });

        singleModelDeliveryReq.setSingleModelData(singleModelData);
//        String filename = getFileName(singleModelDeliveryReq.getEnergyModelEnum(), "MODEL");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MODEL", singleModelDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();

        String filename = getFileName(resourceCode, "MODEL", aggregatorAliasName);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MODEL", resourceCode).getName();
        Map<String, Object> map = new HashMap<>(8);
        map.put("detailList", singleModelDeliveryReq.getSingleModelData());
        map.put("company", aggregatorAliasName);
        String response = null;
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();

            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
            String encodeString = Base64.getEncoder().encodeToString(bytes);
            log.info("encodeString：{}", encodeString);

            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
            response = greeter.commitFile(filename, encodeString);
        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        } finally {
            SingleModelDeliveryLog singleModelDeliveryLog = new SingleModelDeliveryLog();
            singleModelDeliveryLog.setFileName(filename);
            // 入库存原始请求报文
            singleModelDeliveryLog.setFileByte(JSONObject.toJSONString(singleModelDeliveryReq));
            singleModelDeliveryLog.setDeliveryStatus(response);
            singleModelDeliveryLog.setCreateTime(new Date());

            singleModelDeliveryLogService.addLog(singleModelDeliveryLog);
        }

        return response;
    }

    private String deliveryModelDataDES(List<AggregatorEntDevice> desConfigs, String channelNo, String resourceCode, String aggregatorId) {

        if (CollectionUtils.isEmpty(desConfigs)) {
            return "DES has no config data";
        }

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        // 分布式储能
        SingleModelDeliveryReq singleModelDeliveryReq = new SingleModelDeliveryReq();
//        singleModelDeliveryReq.setEnergyModelEnum(EnergyModelEnum.DISTRIBUTED_ENERGY);
        List<Object> singleModelData = Lists.newArrayList();

        // 按企业归并
        Map<String, List<AggregatorEntDevice>> configMapByEntId = desConfigs.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getEntId));

        configMapByEntId.forEach((entId, configs) -> {
            Map<String, String> entMap = new HashMap<>(16);
            entMap.put("stationName", configs.get(0).getUsername());
            entMap.put("area", configs.get(0).getAreaCode());
            entMap.put("totalCapacity", String.valueOf(configs.stream().filter(e -> !Objects.isNull(e.getPower())).mapToDouble(AggregatorEntDevice::getPower).sum()));
            entMap.put("innerStationId", entId);
            singleModelData.add(entMap);
        });

        singleModelDeliveryReq.setSingleModelData(singleModelData);

//        String filename = getFileName(singleModelDeliveryReq.getEnergyModelEnum(), "MODEL");
//
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MODEL", singleModelDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();

        String filename = getFileName(resourceCode, "MODEL", aggregatorAliasName);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MODEL", resourceCode).getName();

        Map<String, Object> map = new HashMap<>(16);
        map.put("detailList", singleModelDeliveryReq.getSingleModelData());
        map.put("company", aggregatorAliasName);
        String response = null;
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();

            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
            String encodeString = Base64.getEncoder().encodeToString(bytes);
            log.info("encodeString：{}", encodeString);

            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
            response = greeter.commitFile(filename, encodeString);

        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        } finally {
            SingleModelDeliveryLog singleModelDeliveryLog = new SingleModelDeliveryLog();
            singleModelDeliveryLog.setFileName(filename);
            // 入库存原始请求报文
            singleModelDeliveryLog.setFileByte(JSONObject.toJSONString(singleModelDeliveryReq));
            singleModelDeliveryLog.setDeliveryStatus(response);
            singleModelDeliveryLog.setCreateTime(new Date());
            singleModelDeliveryLogService.addLog(singleModelDeliveryLog);
        }

        return response;
    }

    private String deliveryModelDataCp(List<AggregatorEntDevice> cpConfigs, String channelNo, String resourceCode, String aggregatorId) {

        if (CollectionUtils.isEmpty(cpConfigs)) {
            return "CP has no config data";
        }

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        // 充电桩
        SingleModelDeliveryReq singleModelDeliveryReq = new SingleModelDeliveryReq();
//        singleModelDeliveryReq.setEnergyModelEnum(EnergyModelEnum.DISTRIBUTED_ENERGY);
        List<Object> singleModelData = Lists.newArrayList();

        //按企业归并
        Map<String, List<AggregatorEntDevice>> configMapByEntId = cpConfigs.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getEntId));
        //按能源站合并
        Map<String, List<AggregatorEntDevice>> configMapEnergyStation = new HashMap<>();
        for (Map.Entry<String, List<AggregatorEntDevice>> entIdConfig : configMapByEntId.entrySet()) {
            String entId = entIdConfig.getKey();
            List<AggregatorEntDevice> value = entIdConfig.getValue();
            List<String> energyStationCodeLists = cpConfigs.stream().filter(e -> StrUtil.equals(e.getEntId(), entId)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = cpConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }
                List<AggregatorEntDevice> energyStationDevices = value.stream().filter(e -> energyStationDeviceIds.contains(e.getDeviceId())).collect(Collectors.toList());
                configMapEnergyStation.put(energyStationCode, energyStationDevices);
            }
        }

        configMapEnergyStation.forEach((energyStationCode, configs) -> {
            //能源站信息
            List<EnergyStationInfo> energyStationInfos = queryService.getEnergyStationInfoByEnergyStationCode(energyStationCode);
            if (CollectionUtils.isNotEmpty(energyStationInfos)) {
                EnergyStationInfo energyStationInfo = energyStationInfos.get(0);
                Map<String, String> entMap = new HashMap<>(16);
                entMap.put("stationName", energyStationInfo.getEnergyStation());
                entMap.put("area", energyStationInfo.getAreaCode());
                entMap.put("totalCapacity", String.valueOf(configs.stream().filter(e -> !Objects.isNull(e.getPower())).mapToDouble(AggregatorEntDevice::getPower).sum()));
                entMap.put("chargingEquipNo", String.valueOf(CollectionUtils.size(configs)));
                entMap.put("innerStationId", energyStationInfo.getEnergyStationCode());
                entMap.put("stationType", energyStationInfo.getEnergyStationType());
                entMap.put("controllable", energyStationInfo.getEnergyStationController());
                singleModelData.add(entMap);
            }
        });

        singleModelDeliveryReq.setSingleModelData(singleModelData);

        List<Object> singleModelDataDevice = Lists.newArrayList();
        for (AggregatorEntDevice cpConfig : cpConfigs) {
            Map<String, String> deviceMap = new HashMap<>(16);
            deviceMap.put("equipName", cpConfig.getDeviceName());
            deviceMap.put("stationName", cpConfig.getEnergyStation());
            deviceMap.put("equipCapacity", String.valueOf(cpConfig.getPower()));
            deviceMap.put("equipType", cpConfig.getUserType());
            deviceMap.put("equipManufactor", cpConfig.getEquipManufactor());
            deviceMap.put("investor", cpConfig.getUsername());
            deviceMap.put("innerEquipId", cpConfig.getDeviceBaseId());
            singleModelDataDevice.add(deviceMap);
        }
//        String filename = getFileName(singleModelDeliveryReq.getEnergyModelEnum(), "MODEL");
//
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MODEL", singleModelDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();

        String filename = getFileName(resourceCode, "MODEL", aggregatorAliasName);
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MODEL", resourceCode).getName();

        Map<String, Object> map = new HashMap<>(16);
        map.put("electricVehicleStationList", singleModelDeliveryReq.getSingleModelData());
        map.put("company", aggregatorAliasName);
        map.put("electricVehicleEquipList", singleModelDataDevice);
        String response = null;
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();

            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
            String encodeString = Base64.getEncoder().encodeToString(bytes);
            log.info("encodeString：{}", encodeString);

            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
            response = greeter.commitFile(filename, encodeString);

        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        } finally {
            SingleModelDeliveryLog singleModelDeliveryLog = new SingleModelDeliveryLog();
            singleModelDeliveryLog.setFileName(filename);
            // 入库存原始请求报文
            singleModelDeliveryLog.setFileByte(JSONObject.toJSONString(singleModelDeliveryReq));
            singleModelDeliveryLog.setDeliveryStatus(response);
            singleModelDeliveryLog.setCreateTime(new Date());
            singleModelDeliveryLogService.addLog(singleModelDeliveryLog);
        }

        return response;
    }

//    private String deliveryModelDataVPPCustom(List<AggregatorEntDevice> vppConfigs) {
//
//        if (CollectionUtils.isEmpty(vppConfigs)) {
//            return "VPP has no config data";
//        }
//
//        // 工业负荷
//        SingleModelDeliveryReq singleModelDeliveryReq = new SingleModelDeliveryReq();
//        singleModelDeliveryReq.setEnergyModelEnum(EnergyModelEnum.INDUSTRIAL_LOAD);
//        List<Object> singleModelData = Lists.newArrayList();
//
//        // 按企业归并
//        Map<String, List<AggregatorEntDevice>> configMapByEntId = vppConfigs.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getEntId));
//
//        configMapByEntId.forEach((entId, configs) -> {
//            Map<String, String> entMap = new HashMap<>(16);
//
//            entMap.put("username", configs.get(0).getUsername());
//            entMap.put("capacity", String.valueOf(configs.stream().mapToDouble(AggregatorEntDevice::getPower).sum()));
//            entMap.put("area", configs.get(0).getAreaCode());
//            entMap.put("userType", configs.get(0).getUserType());
//            entMap.put("owner", configs.get(0).getUsername());
//            // 按企业id
//            entMap.put("innerStationId", entId);
//            entMap.put("participation", "1");
//            singleModelData.add(entMap);
//        });
//
//        singleModelDeliveryReq.setSingleModelData(singleModelData);
//        String filename = getFileName(singleModelDeliveryReq.getEnergyModelEnum(), "MODEL");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MODEL", singleModelDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//        Map<String, List<Object>> map = new HashMap<>(8);
//        map.put("detailList", singleModelDeliveryReq.getSingleModelData());
//
//        String response = null;
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                response = StatusCode.F_A.getMsg();
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//
//            response = templateResult.getMsg();
//            FileUtil.writeString(templateData, LOCAL_FILE_PATH_MODEL + File.separator + filename, Charsets.UTF_8);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//        } finally {
//            SingleModelDeliveryLog singleModelDeliveryLog = new SingleModelDeliveryLog();
//            singleModelDeliveryLog.setFileName(filename);
//            // 入库存原始请求报文
//            singleModelDeliveryLog.setFileByte(JSONObject.toJSONString(singleModelDeliveryReq));
//            singleModelDeliveryLog.setDeliveryStatus(response);
//            singleModelDeliveryLog.setCreateTime(new Date());
//
//            singleModelDeliveryLogService.addLog(singleModelDeliveryLog);
//        }
//
//        return response;
//    }

//    private String deliveryModelDataEHCustom(List<AggregatorEntDevice> ehConfigs) {
//
//        if (CollectionUtils.isEmpty(ehConfigs)) {
//            return "EH has no config data";
//        }
//
//        // 电加热
//        SingleModelDeliveryReq singleModelDeliveryReq = new SingleModelDeliveryReq();
//        singleModelDeliveryReq.setEnergyModelEnum(EnergyModelEnum.ELECTRIC_HEATING);
//        List<Object> singleModelData = Lists.newArrayList();
//
//        // 按企业归并
//        Map<String, List<AggregatorEntDevice>> configMapByEntId = ehConfigs.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getEntId));
//
//        configMapByEntId.forEach((entId, configs) -> {
//            Map<String, String> entMap = new HashMap<>(16);
//
//            entMap.put("username", configs.get(0).getUsername());
//            entMap.put("capacity", String.valueOf(configs.stream().mapToDouble(AggregatorEntDevice::getPower).sum()));
//            entMap.put("area", configs.get(0).getAreaCode());
//            entMap.put("userType", configs.get(0).getUserType());
//            entMap.put("equipManufactor", configs.get(0).getEquipManufactor());
//            entMap.put("storageType", configs.get(0).getUserType());
//            entMap.put("owner", configs.get(0).getUsername());
//            entMap.put("controllable", String.valueOf(configs.get(0).getControllable()));
//            // 按企业id
//            entMap.put("innerStationId", entId);
//            singleModelData.add(entMap);
//        });
//
//        singleModelDeliveryReq.setSingleModelData(singleModelData);
//        String filename = getFileName(singleModelDeliveryReq.getEnergyModelEnum(), "MODEL");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MODEL", singleModelDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//        Map<String, List<Object>> map = new HashMap<>(8);
//        map.put("detailList", singleModelDeliveryReq.getSingleModelData());
//
//        String response = null;
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                response = StatusCode.F_A.getMsg();
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//
//            response = templateResult.getMsg();
//            FileUtil.writeString(templateData, LOCAL_FILE_PATH_MODEL + File.separator + filename, Charsets.UTF_8);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//        } finally {
//            SingleModelDeliveryLog singleModelDeliveryLog = new SingleModelDeliveryLog();
//            singleModelDeliveryLog.setFileName(filename);
//            // 入库存原始请求报文
//            singleModelDeliveryLog.setFileByte(JSONObject.toJSONString(singleModelDeliveryReq));
//            singleModelDeliveryLog.setDeliveryStatus(response);
//            singleModelDeliveryLog.setCreateTime(new Date());
//
//            singleModelDeliveryLogService.addLog(singleModelDeliveryLog);
//        }
//
//        return response;
//    }

//    private String deliveryModelDataDESCustom(List<AggregatorEntDevice> desConfigs) {
//
//        if (CollectionUtils.isEmpty(desConfigs)) {
//            return "DES has no config data";
//        }
//
//        // 分布式储能
//        SingleModelDeliveryReq singleModelDeliveryReq = new SingleModelDeliveryReq();
//        singleModelDeliveryReq.setEnergyModelEnum(EnergyModelEnum.DISTRIBUTED_ENERGY);
//        List<Object> singleModelData = Lists.newArrayList();
//
//        // 按企业归并
//        Map<String, List<AggregatorEntDevice>> configMapByEntId = desConfigs.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getEntId));
//
//        configMapByEntId.forEach((entId, configs) -> {
//            Map<String, String> entMap = new HashMap<>(16);
//            entMap.put("stationName", configs.get(0).getUsername());
//            entMap.put("area", configs.get(0).getAreaCode());
//            entMap.put("totalCapacity", String.valueOf(configs.stream().mapToDouble(AggregatorEntDevice::getPower).sum()));
//            entMap.put("innerStationId", entId);
//            singleModelData.add(entMap);
//        });
//
//        singleModelDeliveryReq.setSingleModelData(singleModelData);
//
//        String filename = getFileName(singleModelDeliveryReq.getEnergyModelEnum(), "MODEL");
//
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MODEL", singleModelDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//        Map<String, List<Object>> map = new HashMap<>(16);
//        map.put("detailList", singleModelDeliveryReq.getSingleModelData());
//
//        String response = null;
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                response = StatusCode.F_A.getMsg();
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//            response = templateResult.getMsg();
//            FileUtil.writeString(templateData, LOCAL_FILE_PATH_MODEL + File.separator + filename, Charsets.UTF_8);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//        } finally {
//            SingleModelDeliveryLog singleModelDeliveryLog = new SingleModelDeliveryLog();
//            singleModelDeliveryLog.setFileName(filename);
//            // 入库存原始请求报文
//            singleModelDeliveryLog.setFileByte(JSONObject.toJSONString(singleModelDeliveryReq));
//            singleModelDeliveryLog.setDeliveryStatus(response);
//            singleModelDeliveryLog.setCreateTime(new Date());
//            singleModelDeliveryLogService.addLog(singleModelDeliveryLog);
//        }
//
//        return response;
//    }

    /**
     * 单体量测数据接入上送接口
     * <p>
     * 单体量测的上送周期为15分钟（每小时00,15,30,45分钟上送）。
     * <p>
     * 端口39090
     * </p>
     */
    public String singleMeasDataDelivery(String aggregatorId) {

        List<AggregatorResourceType> aggregatorResourceTypeListByAggregatorId = queryService.getAggregatorResourceTypeListByAggregatorId(aggregatorId);

//       资源id-资源类型map
        Map<String, String> resourTypeAndNameMap = aggregatorResourceTypeListByAggregatorId.stream().collect(Collectors.toMap(AggregatorResourceType::getId, AggregatorResourceType::getName));

        Map<String, String> resourTypeAndCodeMap = EnergyModelEnumNew.getEnergyMap();
//        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineAggregatorEntDeviceList();
        // modify by sl 2024-10-24 不上送设备站
        List<String> stationIds = Arrays.asList(noUpDeviceStationIds.split(","));
        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineEntDeviceListByAggregatorId(aggregatorId, stationIds);

        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));

        String responseVPP = "";
        String responseEH = "";
        String responseDES = "";
        String responseCP = "";
        Iterator<String> iterator = configMapByResourceType.keySet().iterator();
        while (iterator.hasNext()) {
            String resourceId = iterator.next();
            String resourType = resourTypeAndNameMap.get(resourceId);
            String resourceCode = resourTypeAndCodeMap.get(resourType);
            if (StrUtil.equals(resourType, EnergyModelEnumNew.INDUSTRIAL_LOAD.getName())) {

                List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                responseVPP = deliveryMeasDataVPP(vppConfigs, resourceId, resourceCode, aggregatorId);
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.ELECTRIC_HEATING.getName())) {
                List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                responseEH = deliveryMeasDataEH(ehConfigs, resourceId, resourceCode, aggregatorId);
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.DISTRIBUTED_ENERGY.getName())) {
                List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                responseDES = deliveryMeasDataDES(desConfigs, resourceId, resourceCode, aggregatorId);
            }
            if (StrUtil.equals(resourType, EnergyModelEnumNew.CHARGING_PILE.getName())) {
                List<AggregatorEntDevice> cpConfigs = configMapByResourceType.getOrDefault(resourceId, Lists.newArrayList());
                responseCP = deliveryMeasDataCp(cpConfigs, resourceId, resourceCode, aggregatorId);
            }
        }


//        List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), Lists.newArrayList());
//
//        responseVPP = deliveryMeasDataVPP(vppConfigs);
//        responseEH = deliveryMeasDataEH(ehConfigs);
//        responseDES = deliveryMeasDataDES(desConfigs);

        return responseVPP + responseDES + responseEH + responseCP;

    }

    private String deliveryMeasDataVPP(List<AggregatorEntDevice> vppConfigs, String channelNo, String resourceCode, String aggregatorId) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return "VPP has no config data";
        }

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();
        // 获取数据库配置企业信息
        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntListByResourTypeId(channelNo);
        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();


        List<Object> singleMeasData = Lists.newArrayList();
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        // 获取大数据
        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForEHAndVPP(vppConfigs));

        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForEHAndVPP(vppConfigs));

        // 按systemCode 归并
        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));

        // 基于大数据返回结果汇总信息
        mapGroupingByStationId.forEach((k, v) -> {
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
            String todayZeroElecQuantity = processMeasureDataFromHistoryResp(bigDataHistoryRespListEPTP);
            //            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();

            map.put("username", stationIdToEntMap.get(k).getEntName());
            map.put("userActivePower", totalActivePower);
            map.put("userReactivePower", totalReactivePower);
            map.put("userElecCurrent", userElecCurrent);
            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
            map.put("innerStationId", stationIdToEntMap.get(k).getEntId());

            singleMeasData.add(map);
            singleMeasDataMap.put(k, map);
        });

        stationIdToEntMap.forEach((stationId, ent) -> {
            // 大数据平台数据缺失，无返回时
            if (!singleMeasDataMap.containsKey(stationId)) {
                Map<String, String> map = new HashMap<>(8);
                map.put("username", ent.getEntName());
                map.put("userActivePower", "0.0000");
                map.put("userReactivePower", "0.0000");
                map.put("userElecCurrent", "0.0000");
                map.put("todayZeroElecQuantity", "0.0000");
                map.put("innerStationId", ent.getEntId());

                singleMeasData.add(map);
            }
        });

        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);


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
            log.info("templateData:{}", templateData);

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
//            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setFileByte("");
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
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

    private String deliveryMeasDataEH(List<AggregatorEntDevice> ehConfigs, String channelNo, String resourceCode, String aggregatorId) {

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
        HistoryReq reqParam = getHistoryReqForEHAndVPP(ehConfigs);
        List<BigDataHistoryResp> measData = getMeasDataByMetric(reqParam);

        List<OpentsdbReq> listQueries = reqParam.getListQueries();
        int reqSize = CollectionUtil.size(listQueries);
        int resultSize = CollectionUtil.size(measData);
        if (reqSize - resultSize >= 30) {
            log.info("单体量测带哦用大数据返回数据缺失>30");
            measData = getMeasDataByMetric(reqParam);
        }
        //Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForEHAndVPP(ehConfigs));
        // modify by sl 024-10-24 增加不上送模型
        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, channelNo, noUpModelEnergyStationCodes);
        Map<String, AggregatorSingleModelData> energyStationMap = modelInfoList.stream().collect(Collectors.toMap(AggregatorSingleModelData::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));
        // 按systemCode 归并
        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        //按充电站合并
        Map<String, List<BigDataHistoryResp>> mapGroupingByEnergyStationId = new HashMap<>();
        for (Map.Entry<String, List<BigDataHistoryResp>> systemCodeResult : mapGroupingByStationId.entrySet()) {
            String systemCodea = systemCodeResult.getKey();
            List<BigDataHistoryResp> value = systemCodeResult.getValue();
            List<String> energyStationCodeLists = ehConfigs.stream().filter(e -> StrUtil.equals(e.getStationId(), systemCodea)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = ehConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }

                List<BigDataHistoryResp> energyStationResult = value.stream().filter(e -> energyStationDeviceIds.contains(e.getEquipMK() + "_" + e.getEquipID())).collect(Collectors.toList());
                mapGroupingByEnergyStationId.put(energyStationCode, energyStationResult);
            }
        }
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
            String todayZeroElecQuantity = processMeasureDataFromHistoryResp(bigDataHistoryRespListEPTP);
            //            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();

            map.put("username", energyStationMap.get(k).getEnergyStation());
            map.put("userActivePower", totalActivePower);
            map.put("userReactivePower", totalReactivePower);
            map.put("userElecCurrent", userElecCurrent);
            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
            map.put("innerStationId", energyStationMap.get(k).getEnergyStationCode());

            singleMeasData.add(map);
            singleMeasDataMap.put(k, map);
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
            log.info("templateData:{}", templateData);
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
//            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setFileByte("");
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
        }

        return response;
    }

    private String deliveryMeasDataDES(List<AggregatorEntDevice> desConfigs, String channelNo, String resourceCode, String aggregatorId) {

        if (CollectionUtils.isEmpty(desConfigs)) {
            return "DES has no config data";
        }
        // 获取数据库配置企业信息
        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntListByResourTypeId(channelNo);
        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
//        singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.DISTRIBUTED_ENERGY);
//聚合商别名
        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        List<Object> singleMeasData = Lists.newArrayList();
        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForDES(desConfigs));
        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForDES(desConfigs));

        // 按systemCode 归并
        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);

        mapGroupingByStationId.forEach((k, v) -> {
            Map<String, String> map = new HashMap<>(16);

            // P 总有功功率
            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            // 分布式储能P值与大数据值相反
            String totalActivePower = BigDecimal.ZERO.subtract(new BigDecimal(processMeasureDataFromHistoryResp(bigDataHistoryRespListP))).setScale(4, BigDecimal.ROUND_HALF_UP).toString();

            // Q 总无功功率
            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            // 分布式储能Q值与大数据值相反
            String totalReactivePower = BigDecimal.ZERO.subtract(new BigDecimal(processMeasureDataFromHistoryResp(bigDataHistoryRespListQ))).setScale(4, BigDecimal.ROUND_HALF_UP).toString();

            // Eptp 有功电度正向量（）
//            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String todayZeroElecQuantity = processMeasureDataFromHistoryResp(bigDataHistoryRespListEPTP);
            //            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();

            map.put("stationName", stationIdToEntMap.get(k).getEntName());
            map.put("totalActivePower", totalActivePower);
            map.put("totalReactivePower", totalReactivePower);
            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
            map.put("innerStationId", stationIdToEntMap.get(k).getEntId());

            singleMeasData.add(map);
            singleMeasDataMap.put(k, map);
        });

        stationIdToEntMap.forEach((stationId, ent) -> {
            // 大数据平台数据缺失，无返回时
            if (!singleMeasDataMap.containsKey(stationId)) {
                Map<String, String> map = new HashMap<>(8);
                map.put("stationName", ent.getEntName());
                map.put("totalActivePower", "0.0000");
                map.put("totalReactivePower", "0.0000");
                map.put("todayZeroElecQuantity", "0.0000");
                map.put("innerStationId", ent.getEntId());

                singleMeasData.add(map);
            }
        });
        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);

//        String filename = getFileName(singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();

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
            log.info("templateData:{}", templateData);
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
//            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setFileByte("");
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());

            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
        }

        return response;
    }

    private String deliveryMeasDataCp(List<AggregatorEntDevice> cpConfigs, String channelNo, String resourceCode, String aggregatorId) {

        if (CollectionUtils.isEmpty(cpConfigs)) {
            return "Cp has no config data";
        }
        // 获取数据库配置企业信息

        Map<String, AggregatorEntDevice> energyStationIdToEntMap = cpConfigs.stream().collect(Collectors.toMap(AggregatorEntDevice::getEnergyStationCode, Function.identity(), (k1, k2) -> k2));

        List<AggregatorInfo> aggregatorInfoByAggregatorId = queryService.getAggregatorInfoByAggregatorId(aggregatorId);
        AggregatorInfo aggregatorInfo = aggregatorInfoByAggregatorId.get(0);
        String aggregatorAliasName = aggregatorInfo.getAggregatorAliasName();

        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
//        singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.ELECTRIC_HEATING);

        List<Object> singleMeasData = Lists.newArrayList();
        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForCp(cpConfigs));

        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForEHAndVPP(cpConfigs));
        // 按systemCode 归并
        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
        Map<String, Map<String, String>> singleMeasDataMap = new HashMap<>(16);
        //按充电站合并
        Map<String, List<BigDataHistoryResp>> mapGroupingByEnergyStationId = new HashMap<>();
        for (Map.Entry<String, List<BigDataHistoryResp>> systemCodeResult : mapGroupingByStationId.entrySet()) {
            String systemCodea = systemCodeResult.getKey();
            List<BigDataHistoryResp> value = systemCodeResult.getValue();
            List<String> energyStationCodeLists = cpConfigs.stream().filter(e -> StrUtil.equals(e.getStationId(), systemCodea)).map(e -> e.getEnergyStationCode()).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationCodeLists)) {
                continue;
            }
            for (String energyStationCode : energyStationCodeLists) {
                List<String> energyStationDeviceIds = cpConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).map(e -> e.getDeviceId()).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(energyStationDeviceIds)) {
                    continue;
                }

                List<BigDataHistoryResp> energyStationResult = value.stream().filter(e -> energyStationDeviceIds.contains(e.getEquipMK() + "_" + e.getEquipID())).collect(Collectors.toList());
                mapGroupingByEnergyStationId.put(energyStationCode, energyStationResult);
            }
        }

        mapGroupingByEnergyStationId.forEach((k, v) -> {
            Map<String, String> map = new HashMap<>(16);

            // P 总有功功率
            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String totalActivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListP);


            // Eptp 有功电度正向量（）
            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
            String todayZeroElecQuantity = processMeasureDataFromHistoryResp(bigDataHistoryRespListEPTP);
//            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();

            map.put("stationName", energyStationIdToEntMap.get(k).getEnergyStation());
            map.put("totalPower", totalActivePower);
            map.put("regularTotalPower", totalActivePower);
            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
            map.put("innerStationId", energyStationIdToEntMap.get(k).getEnergyStationCode());

            singleMeasData.add(map);
            singleMeasDataMap.put(k, map);
        });

        energyStationIdToEntMap.forEach((energyStationId, ent) -> {
            // 大数据平台数据缺失，无返回时
            if (!singleMeasDataMap.containsKey(energyStationId)) {
                Map<String, String> map = new HashMap<>(8);
                map.put("stationName", ent.getEnergyStation());
                map.put("totalPower", "0.0000");
                map.put("regularTotalPower", "0.0000");
                map.put("todayZeroElecQuantity", "0.0000");
                map.put("innerStationId", ent.getEnergyStationCode());
                singleMeasData.add(map);
            }
        });
        List<Object> singleMeasDataDvcice = Lists.newArrayList();
        //充电桩数据List
        for (Map.Entry<String, List<BigDataHistoryResp>> stringListEntry : mapGroupingByEnergyStationId.entrySet()) {
            String energyStationCode = stringListEntry.getKey();
            List<BigDataHistoryResp> value = stringListEntry.getValue();
            List<AggregatorEntDevice> energyStationDeviceInfos = cpConfigs.stream().filter(e -> StrUtil.equals(e.getEnergyStationCode(), energyStationCode)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(energyStationDeviceInfos)) {
                continue;
            }
            for (AggregatorEntDevice energyStationDeviceInfo : energyStationDeviceInfos) {
                String deviceId = energyStationDeviceInfo.getDeviceId();
                List<BigDataHistoryResp> deviceValue = value.stream().filter(e -> StrUtil.equals(e.getEquipMK() + "_" + e.getEquipID(), deviceId)).collect(Collectors.toList());

                String devicepValue = "";
                String deviceIaValue = "";
                String deviceEptpValue = "";
                if (CollectionUtils.isNotEmpty(deviceValue)) {
                    //p
                    List<BigDataHistoryResp> devicepValues = deviceValue.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
                    devicepValue = processMeasureDataFromHistoryResp(devicepValues);
                    //Ia
                    List<BigDataHistoryResp> devicepIaValues = deviceValue.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
                    deviceIaValue = processMeasureDataFromHistoryResp(devicepIaValues);
                    //eptp
                    List<BigDataHistoryResp> devicepEptpValues = deviceValue.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
                    deviceEptpValue = processMeasureDataFromHistoryResp(devicepEptpValues);
                } else {
                    devicepValue = "0.0000";
                    deviceIaValue = "0.0000";
                    deviceEptpValue = "0.0000";
                }
                Map<String, String> mapDevice = new HashMap<>(16);
                //桩名
                mapDevice.put("equipName", energyStationDeviceInfo.getDeviceName());
                //所属站
                mapDevice.put("stationName", energyStationDeviceInfo.getEnergyStation());
                //桩有功
                mapDevice.put("equipPower", devicepValue);
                //桩电流
                mapDevice.put("equipElecCurrent", deviceIaValue);
                //桩当日零点电量
                mapDevice.put("equipzeroElecQuanlity", deviceEptpValue);
                //运营系统内部设备ID
                mapDevice.put("innerEquipId", energyStationDeviceInfo.getDeviceBaseId());
                singleMeasDataDvcice.add(mapDevice);
            }
        }

        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);
        singleMeasDeliveryReq.setSingleMeasDataDevice(singleMeasDataDvcice);
//        String filename = getFileName(singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
        String filename = getFileName(resourceCode, "MEAS", aggregatorAliasName);
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
        String tempalteName = TemplateNameNewEnum.getByTypeAndNo("MEAS", resourceCode).getName();
        Map<String, Object> map = new HashMap<>(16);
        map.put("electricVehicleStationList", singleMeasDeliveryReq.getSingleMeasData());
        map.put("electricVehicleEquipList", singleMeasDataDvcice);
        map.put("company", aggregatorAliasName);
        String response = null;
        try {
            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);

            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
                response = StatusCode.F_A.getMsg();
                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
            }

            String templateData = templateResult.getData();
            log.info("templateData:{}", templateData);
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
//            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
            singleMeasDeliveryLog.setFileByte("");
            singleMeasDeliveryLog.setDeliveryStatus(response);
            singleMeasDeliveryLog.setCreateTime(new Date());
            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
        }

        return response;
    }

//    private String deliveryMeasDataVPPFromBigData(List<AggregatorEntDevice> vppConfigs, String dateTime) {
//
//        if (CollectionUtils.isEmpty(vppConfigs)) {
//            return "VPP has no config data";
//        }
//
//        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntList();
//        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));
//
//        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
//        singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.INDUSTRIAL_LOAD);
//
//        List<Object> singleMeasData = Lists.newArrayList();
//        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForEHAndVPPCustom(vppConfigs, dateTime));
//
//        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForEHAndVPPCustom(vppConfigs, dateTime));
//
//        // 按systemCode 归并
//        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
//
//        mapGroupingByStationId.forEach((k, v) -> {
//            Map<String, String> map = new HashMap<>(16);
//            // P 总有功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String totalActivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListP);
//            // Q 总无功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String totalReactivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListQ);
//            // Ia A相电流
//            List<BigDataHistoryResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String userElecCurrent = processMeasureDataFromHistoryResp(bigDataHistoryRespListIa);
//            // Eptp 有功电度正向量（）
//            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();
//
//            map.put("username", stationIdToEntMap.get(k).getEntName());
//            map.put("userActivePower", totalActivePower);
//            map.put("userReactivePower", totalReactivePower);
//            map.put("userElecCurrent", userElecCurrent);
//            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
//            map.put("innerStationId", stationIdToEntMap.get(k).getEntId());
//
//            singleMeasData.add(map);
//        });
//
//        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);
//
//        String filename = getFileNameCustom(dateTime, singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//
//        Map<String, List<Object>> map = new HashMap<>(8);
//        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
//
//        String response = null;
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//            response = templateResult.getMsg();
//
//            FileUtil.writeString(templateData, LOCAL_FILE_PATH_MEASE + File.separator + filename, Charsets.UTF_8);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//        }
//
//        return response;
//    }


    private String getFileNameCustom(String dateTime, EnergyModelEnum energyModelEnum, String type) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMATTER_SEC);

        StringBuilder stringBuilder = new StringBuilder(aggregatorName);
        DateTime dateTimeCustom = DateTime.parse(dateTime, dateTimeFormatter);
        dateTime = dateTimeCustom.toString(DATE_FORMATTER_FILE_NAME);
        stringBuilder.append(energyModelEnum.getCode()).append(fileSeparate).append(type).append(fileSeparate).append(dateTime).append(filePoint).append(fileSuffix);
        return stringBuilder.toString();
    }


    private HistoryReq getHistoryReqForEHAndVPPCustom(List<AggregatorEntDevice> vppConfigs, String dateTime) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMATTER_SEC);
        String startTime = DateTime.parse(dateTime, dateTimeFormatter).minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.parse(dateTime, dateTimeFormatter).toString(DATE_FORMATTER_MIN_LAST);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        vppConfigs.stream().forEach(config -> {
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

//    private String deliveryMeasDataEHFromBigData(List<AggregatorEntDevice> ehConfigs, String dateTime) {
//
//        if (CollectionUtils.isEmpty(ehConfigs)) {
//            return "EH has no config data";
//        }
//
//        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntList();
//        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));
//
//        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
//        singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.ELECTRIC_HEATING);
//
//        List<Object> singleMeasData = Lists.newArrayList();
//        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForEHAndVPPCustom(ehConfigs, dateTime));
//
//        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForEHAndVPPCustom(ehConfigs, dateTime));
//
//        // 按systemCode 归并
//        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
//
//        mapGroupingByStationId.forEach((k, v) -> {
//            Map<String, String> map = new HashMap<>(16);
//
//            // P 总有功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String totalActivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListP);
//            // Q 总无功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String totalReactivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListQ);
//            // Ia A相电流
//            List<BigDataHistoryResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String userElecCurrent = processMeasureDataFromHistoryResp(bigDataHistoryRespListIa);
//            // Eptp 有功电度正向量（）
//            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();
//
//            map.put("username", stationIdToEntMap.get(k).getEntName());
//            map.put("userActivePower", totalActivePower);
//            map.put("userReactivePower", totalReactivePower);
//            map.put("userElecCurrent", userElecCurrent);
//            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
//            map.put("innerStationId", stationIdToEntMap.get(k).getEntId());
//
//            singleMeasData.add(map);
//        });
//
//        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);
//
//        String filename = getFileNameCustom(dateTime, singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//
//        Map<String, List<Object>> map = new HashMap<>(16);
//        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
//
//        String response = null;
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//            response = templateResult.getMsg();
//            FileUtil.writeString(templateData, LOCAL_FILE_PATH_MEASE + File.separator + filename, Charsets.UTF_8);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//        }
//
//        return response;
//    }

//    private String deliveryMeasDataDESFromBigData(List<AggregatorEntDevice> desConfigs, String dateTime) {
//
//        if (CollectionUtils.isEmpty(desConfigs)) {
//            return "DES has no config data";
//        }
//
//        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntList();
//        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));
//
//        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
//        singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.DISTRIBUTED_ENERGY);
//
//        List<Object> singleMeasData = Lists.newArrayList();
//        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForDESCustom(desConfigs, dateTime));
//        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForDESCustom(desConfigs, dateTime));
//
//        // 按systemCode 归并
//        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
//
//        mapGroupingByStationId.forEach((k, v) -> {
//            Map<String, String> map = new HashMap<>(16);
//            // P 总有功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            // 分布式储能P值与大数据值相反
//            String totalActivePower = BigDecimal.ZERO.subtract(new BigDecimal(processMeasureDataFromHistoryResp(bigDataHistoryRespListP))).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
//            // Q 总无功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            // 分布式储能P值与大数据值相反
//            String totalReactivePower = BigDecimal.ZERO.subtract(new BigDecimal(processMeasureDataFromHistoryResp(bigDataHistoryRespListQ))).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
//            // Eptp 有功电度正向量（）
//            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();
//
//            map.put("stationName", stationIdToEntMap.get(k).getEntName());
//            map.put("totalActivePower", totalActivePower);
//            map.put("totalReactivePower", totalReactivePower);
//            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
//            map.put("innerStationId", stationIdToEntMap.get(k).getEntId());
//
//            singleMeasData.add(map);
//        });
//
//        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);
//
//        String filename = getFileNameCustom(dateTime, singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//
//        Map<String, List<Object>> map = new HashMap<>(16);
//        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
//
//        String response = null;
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//            response = templateResult.getMsg();
//            FileUtil.writeString(templateData, LOCAL_FILE_PATH_MEASE + File.separator + filename, Charsets.UTF_8);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//        }
//
//        return response;
//    }


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

    private List<BigDataHistoryResp> getMeasDataByMetric(HistoryReq historyReq) {

        return bigDataHandlerService.getHistory(historyReq, "0");

    }

    private HistoryReq getHistoryReqForEHAndVPP(List<AggregatorEntDevice> ehConfigs) {
        String startTime = DateTime.now().minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.now().toString(DATE_FORMATTER_MIN_LAST);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        ehConfigs.stream().forEach(config -> {
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

    private HistoryReq getHistoryReqForDES(List<AggregatorEntDevice> desConfigs) {
        String startTime = DateTime.now().minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.now().toString(DATE_FORMATTER_MIN_LAST);
        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");
        List<OpentsdbReq> listQueries = Lists.newArrayList();

        desConfigs.stream().forEach(config -> {

            OpentsdbReq opentsdbReq01 = new OpentsdbReq();
            opentsdbReq01.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq01.setMetric("EMS.P");
            opentsdbReq01.setAggregator("last");
            TagVO tag01 = new TagVO();
            tag01.setStaId(config.getStationId());
            tag01.setEquipMK(config.getDeviceType());
            tag01.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq01.setTags(tag01);
            listQueries.add(opentsdbReq01);

            OpentsdbReq opentsdbReq02 = new OpentsdbReq();
            opentsdbReq02.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq02.setMetric("EMS.Q");
            opentsdbReq02.setAggregator("last");
            TagVO tag02 = new TagVO();
            tag02.setStaId(config.getStationId());
            tag02.setEquipMK(config.getDeviceType());
            tag02.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq02.setTags(tag02);
            listQueries.add(opentsdbReq02);

            OpentsdbReq opentsdbReq03 = new OpentsdbReq();
            opentsdbReq03.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq03.setMetric("EMS.Eptp");
            opentsdbReq03.setAggregator("last");
            TagVO tag03 = new TagVO();
            tag03.setStaId(config.getStationId());
            tag03.setEquipMK(config.getDeviceType());
            tag03.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq03.setTags(tag03);
            listQueries.add(opentsdbReq03);
        });

        historyReq.setListQueries(listQueries);

        return historyReq;
    }

    private HistoryReq getHistoryReqForCp(List<AggregatorEntDevice> cpConfigs) {
        String startTime = DateTime.now().minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.now().toString(DATE_FORMATTER_MIN_LAST);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        cpConfigs.stream().forEach(config -> {
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

//            OpentsdbReq opentsdbReq2 = new OpentsdbReq();
//            opentsdbReq2.setDownsample(ONE_MIN_LAST_NULL);
//            opentsdbReq2.setMetric("EMS.Q");
//            opentsdbReq2.setAggregator("last");
//            TagVO tag2 = new TagVO();
//            tag2.setStaId(config.getStationId());
//            tag2.setEquipMK(config.getDeviceType());
//            tag2.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
//            opentsdbReq2.setTags(tag2);
//            listQueries.add(opentsdbReq2);
//
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

    private HistoryReq getHistoryReqForDESCustom(List<AggregatorEntDevice> desConfigs, String dateTime) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMATTER_SEC);
        String startTime = DateTime.parse(dateTime, dateTimeFormatter).minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.parse(dateTime, dateTimeFormatter).toString(DATE_FORMATTER_MIN_LAST);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");
        List<OpentsdbReq> listQueries = Lists.newArrayList();

        desConfigs.forEach(config -> {

            OpentsdbReq opentsdbReq01 = new OpentsdbReq();
            opentsdbReq01.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq01.setMetric("EMS.P");
            opentsdbReq01.setAggregator("last");
            TagVO tag01 = new TagVO();
            tag01.setStaId(config.getStationId());
            tag01.setEquipMK(config.getDeviceType());
            tag01.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq01.setTags(tag01);
            listQueries.add(opentsdbReq01);

            OpentsdbReq opentsdbReq02 = new OpentsdbReq();
            opentsdbReq02.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq02.setMetric("EMS.Q");
            opentsdbReq02.setAggregator("last");
            TagVO tag02 = new TagVO();
            tag02.setStaId(config.getStationId());
            tag02.setEquipMK(config.getDeviceType());
            tag02.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq02.setTags(tag02);
            listQueries.add(opentsdbReq02);

            OpentsdbReq opentsdbReq03 = new OpentsdbReq();
            opentsdbReq03.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq03.setMetric("EMS.Eptp");
            opentsdbReq03.setAggregator("last");
            TagVO tag03 = new TagVO();
            tag03.setStaId(config.getStationId());
            tag03.setEquipMK(config.getDeviceType());
            tag03.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq03.setTags(tag03);
            listQueries.add(opentsdbReq03);
        });

        historyReq.setListQueries(listQueries);

        return historyReq;
    }

    /**
     * 计划申报接入上送接口
     * <p>
     * 市场正式运行阶段，每日9点之前上报次日基础负荷，基准功率，调峰范围。
     * <p>
     * 端口39090
     * </p>
     */
    public String declare(List<LinkedHashMap<String, String>> cmdDataList) {

        String cmdData = JSONObject.toJSONString(cmdDataList);
        String response = null;
        try {
            if (planDeliveryOnOffService.getMark()) {
                log.info("计划申报接入上送,totalAndDeliveryUrl:{},  cmdData:{}", totalAndDeliveryUrl, cmdData);
                Greeter greeter = clientConfig.greeter(totalAndDeliveryUrl);
                response = greeter.declare(cmdData);
                log.info("计划申报接入上送,response:{}", response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        } finally {
            PlanDeliveryLog planDeliveryLog = new PlanDeliveryLog();
            planDeliveryLog.setContent(cmdData);
            planDeliveryLog.setDeliveryStatus(response);
            planDeliveryLog.setCreateTime(new Date());
            log.info("计划申报接入上送log入库, planDeliveryLog:{}", planDeliveryLog);
            planDeliveryLogService.addLog(planDeliveryLog);
        }

        return response;
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


//    /**
//     * 总加数据补招处理逻辑
//     *
//     * @param retryIssueDTO
//     * @return
//     */
//    public ResultVO<String> totalDataDeliveryRetry(RetryIssueDTO retryIssueDTO) {
//
//        ResultVO<String> result = new ResultVO<>();
//        // step 1 获取所有子企业entId
//        String group = retryIssueDTO.getGroup();
//        // step 2 根据子企业获取所有对应资源的数据
//        Map<String, String> cmdData = new LinkedHashMap<>();
//        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineAggregatorEntDeviceList();
//        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));
//        List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), Lists.newArrayList());
//
//        // 从control_issue_log 获取最近的控制下发信息
//        String issueValue = "0.000000";
//        String issueStatus = "1";
//        String issueSign = "1";
//
//        String startTimeStamp = retryIssueDTO.getTimestamp();
//        Date startTimeDate = new Date(Long.parseLong(startTimeStamp) * 1000);
//        DateTime dateTime = new DateTime(startTimeDate);
//        String startTime = dateTime.toString(DATE_FORMATTER_MIN);
//
//        switch (group) {
//            case "25":
//                cmdData = deliveryTotalDataVPPRetry(retryIssueDTO, vppConfigs, startTime, issueValue, issueStatus, issueSign);
//                break;
//            case "26":
//                cmdData = deliveryTotalDataEHRetry(retryIssueDTO, ehConfigs, startTime, issueValue, issueStatus, issueSign);
//                break;
//            case "27":
//                cmdData = deliveryTotalDataDESRetry(retryIssueDTO, desConfigs, startTime, issueValue, issueStatus, issueSign);
//                break;
//            default:
//                break;
//        }
//
//        if (cmdData.isEmpty()) {
//            return ResultVO.success("Has no config data");
//        }
//
//        log.info("totalDataDelivery:{}", JSONObject.toJSONString(cmdData));
//        //数据解析成功!
//        String response = "成功";
//        try {
//            Greeter greeter = clientConfig.greeter(totalAndDeliveryUrl);
//            response = greeter.cmd(JSONObject.toJSONString(cmdData));
//            result = ResultVO.success(response, response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//            result = ResultVO.fail(StatusCode.F_URL_UNAVAILABLE.getCode(), response);
//        } finally {
//            TotalDeliveryLog totalDeliveryLog = new TotalDeliveryLog();
//            totalDeliveryLog.setCreateTime(new Date());
//            totalDeliveryLog.setValue(JSONObject.toJSONString(cmdData));
//            totalDeliveryLog.setDeliveryStatus(response);
//            totalDeliveryLog.setGroupNo(group);
//            totalDeliveryLog.setIssueTime(Long.parseLong(startTimeStamp));
//            totalDeliveryLogService.addLog(totalDeliveryLog);
//            log.info("totalDeliveryLog:{}", JSONObject.toJSONString(totalDeliveryLog));
//        }
//        return result;
//
//    }

    private Map<String, String> deliveryTotalDataVPPRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> vppConfigs, String startTime, String issueValue, String issueStatus, String issueSign) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return Maps.newHashMap();
        }

        Map<String, String> cmdData = new LinkedHashMap<>();

        ControlIssueLog controlIssueLog25 = controlIssueLogService.getLastLogByGroupNoCustom(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo(), startTime);
        if (null != controlIssueLog25 && null != controlIssueLog25.getCmdData()) {
            JSONObject cmdDataJson = JSONObject.parseObject(controlIssueLog25.getCmdData());
            issueValue = StringUtils.isBlank(cmdDataJson.getString("25-1")) ? issueValue : cmdDataJson.getString("25-1");
            issueStatus = StringUtils.isBlank(cmdDataJson.getString("25-3")) ? issueStatus : cmdDataJson.getString("25-3");
            issueSign = StringUtils.isBlank(cmdDataJson.getString("25-4")) ? issueSign : cmdDataJson.getString("25-4");
        }
        // 工业负荷AGC投退状态
        cmdData.put("25-1", issueStatus);
        // 工业负荷有功实发命令（冀北返回值）
        cmdData.put("25-2", issueValue);
        // 华北系统中工业负荷厂AGC正控信号（返回值）
        cmdData.put("25-3", issueSign);
        // 可参与调节的工业负荷终端数量
        String vppSize = String.valueOf(vppConfigs.size());
        cmdData.put("25-4", vppSize);
        // 可参与调节的工业负荷实时有功（单位MW，以充电为﹢）
        String activePowerForVPP = getActivePowerForVPPRetry(retryIssueDTO, vppConfigs);
        cmdData.put("25-5", activePowerForVPP);
        // 可参与调节的工业负荷按当前功率最大可持续时间
        cmdData.put("25-6", "0");
        // 可参与调节的工业负荷终端功率上限（最大可充）
        cmdData.put("25-7", "0");
        // 可参与调节的工业负荷终端功率下限（最大可放）
        cmdData.put("25-8", "0");
        // 工业负荷整体运行模式（0仅可充，1仅可放，2可充可放）
        cmdData.put("25-9", "1");
        // 工业负荷最大允许命令步长
        cmdData.put("25-10", "0");
        // 工业负荷类型数量
        cmdData.put("25-11", vppSize);
        // 工业负荷类型实时有功
        cmdData.put("25-12", activePowerForVPP);

        return cmdData;

    }

    private Map<String, String> deliveryTotalDataEHRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> ehConfigs, String startTime, String issueValue, String issueStatus, String issueSign) {

        if (CollectionUtils.isEmpty(ehConfigs)) {
            return Maps.newHashMap();
        }

        Map<String, String> cmdData = new LinkedHashMap<>();

        ControlIssueLog controlIssueLog26 = controlIssueLogService.getLastLogByGroupNoCustom(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), startTime);
        if (null != controlIssueLog26 && null != controlIssueLog26.getCmdData()) {
            JSONObject cmdDataJson = JSONObject.parseObject(controlIssueLog26.getCmdData());
            issueValue = StringUtils.isBlank(cmdDataJson.getString("26-1")) ? issueValue : cmdDataJson.getString("26-1");
            issueStatus = StringUtils.isBlank(cmdDataJson.getString("26-3")) ? issueStatus : cmdDataJson.getString("26-3");
            issueSign = StringUtils.isBlank(cmdDataJson.getString("26-4")) ? issueSign : cmdDataJson.getString("26-4");
        }
        // 京津唐电采暖AGC投退状态
        cmdData.put("26-1", issueStatus);
        // 京津唐电采暖有功实发命令（返回值）
        cmdData.put("26-2", issueValue);
        // 华北系统中京津唐电采暖AGC正控信号（返回值）
        cmdData.put("26-3", issueSign);
        // 可参与调节的京津唐电采暖实时数量
        String ehSize = String.valueOf(ehConfigs.size());
        cmdData.put("26-4", ehSize);
        String activePowerForEH = getActivePowerForEHRetry(retryIssueDTO, ehConfigs);
        cmdData.put("26-5", activePowerForEH);
        // 参与调节的京津唐电采暖功率可维持最大时间
        cmdData.put("26-6", "0");
        // 参与调节的京津唐电采暖有功上限（最大用电）
        cmdData.put("26-7", "0");
        // 参与调节的京津唐电采暖下限（最小用电）
        cmdData.put("26-8", "0");
        // 参与调节京津唐电采暖功率最大允许命令步长
        cmdData.put("26-9", "0");
        cmdData.put("26-10", activePowerForEH);

        return cmdData;

    }

    private Map<String, String> deliveryTotalDataDESRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> desConfigs, String startTime, String issueValue, String issueStatus, String issueSign) {

        if (CollectionUtils.isEmpty(desConfigs)) {
            return Maps.newHashMap();
        }

        Map<String, String> cmdData = new LinkedHashMap<>();

        ControlIssueLog controlIssueLog27 = controlIssueLogService.getLastLogByGroupNoCustom(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), startTime);
        if (null != controlIssueLog27 && null != controlIssueLog27.getCmdData()) {
            JSONObject cmdDataJson = JSONObject.parseObject(controlIssueLog27.getCmdData());
            issueValue = StringUtils.isBlank(cmdDataJson.getString("27-1")) ? issueValue : cmdDataJson.getString("27-1");
            issueStatus = StringUtils.isBlank(cmdDataJson.getString("27-3")) ? issueStatus : cmdDataJson.getString("27-3");
            issueSign = StringUtils.isBlank(cmdDataJson.getString("27-4")) ? issueSign : cmdDataJson.getString("27-4");
        }

        List<AggregatorEntDevice> deviceConfigsBeijing = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_BEIJING)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsTianjin = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_TIANJIN)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsHebei = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_HEBEI)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsShanxi = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_SHANXI)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsShandong = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_SHANDONG)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsJibei = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_JIBEI)).collect(Collectors.toList());

        // 区域设备数量
        int deviceNoBeijing = deviceConfigsBeijing.size();
        int deviceNoTianjin = deviceConfigsTianjin.size();
        int deviceNoHebei = deviceConfigsHebei.size();
        int deviceNoShanxi = deviceConfigsShanxi.size();
        int deviceNoShandong = deviceConfigsShandong.size();
        int deviceNoJibei = deviceConfigsJibei.size();

        // 区域设备容量
        double capacityBeijing = CollectionUtils.isEmpty(deviceConfigsBeijing) ? 0.0 : deviceConfigsBeijing.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityBeijing = capacityBeijing / 1000;
        double capacityTianjin = CollectionUtils.isEmpty(deviceConfigsTianjin) ? 0.0 : deviceConfigsTianjin.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityTianjin = capacityTianjin / 1000;
        double capacityHebei = CollectionUtils.isEmpty(deviceConfigsHebei) ? 0.0 : deviceConfigsHebei.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityHebei = capacityHebei / 1000;
        double capacityShanxi = CollectionUtils.isEmpty(deviceConfigsShanxi) ? 0.0 : deviceConfigsShanxi.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityShanxi = capacityShanxi / 1000;
        double capacityShandong = CollectionUtils.isEmpty(deviceConfigsShandong) ? 0.0 : deviceConfigsShandong.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityShandong = capacityShandong / 1000;
        double capacityJibei = CollectionUtils.isEmpty(deviceConfigsJibei) ? 0.0 : deviceConfigsJibei.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityJibei = capacityJibei / 1000;

        // 区域设备实时功率
        BigDecimal activePowerBeijing = getActivePowerForDESRetry(retryIssueDTO, deviceConfigsBeijing);
        BigDecimal activePowerTianjin = getActivePowerForDESRetry(retryIssueDTO, deviceConfigsTianjin);
        BigDecimal activePowerHebei = getActivePowerForDESRetry(retryIssueDTO, deviceConfigsHebei);
        BigDecimal activePowerShanxi = getActivePowerForDESRetry(retryIssueDTO, deviceConfigsShanxi);
        BigDecimal activePowerShandong = getActivePowerForDESRetry(retryIssueDTO, deviceConfigsShandong);
        BigDecimal activePowerJibei = getActivePowerForDESRetry(retryIssueDTO, deviceConfigsJibei);

        int deviceNoJjt = deviceNoBeijing + deviceNoTianjin;
        int deviceNoHuabei = deviceNoJjt + deviceNoHebei + deviceNoShanxi + deviceNoShandong + deviceNoJibei;

        double capacityJjt = capacityBeijing + capacityTianjin;
        double capacityHuabei = capacityJjt + capacityHebei + capacityShanxi + capacityShandong + capacityJibei;

        BigDecimal activePowerJjt = activePowerBeijing.add(activePowerTianjin).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal activePowerHuabei = activePowerJjt.add(activePowerHebei).add(activePowerShanxi).add(activePowerShandong).add(activePowerJibei).setScale(4, BigDecimal.ROUND_HALF_UP);

        // 分布式储能在线数量-华北
        cmdData.put("27-1", String.valueOf(deviceNoHuabei));
        // 分布式储能在线总容量-华北
        cmdData.put("27-2", String.valueOf(capacityHuabei));
        // 分布式储能在线实时有功-华北
        cmdData.put("27-3", activePowerHuabei.toString());

        // 分布式储能在线数量-京津唐(北京+天津)
        cmdData.put("27-4", String.valueOf(deviceNoJjt));
        // 分布式储能在线总容量-京津唐(北京+天津)
        cmdData.put("27-5", String.valueOf(capacityJjt));
        // 分布式储能在线实时有功-京津唐
        cmdData.put("27-6", activePowerJjt.toString());

        // 分布式储能在线数量-北京(属于京津唐电网)
        cmdData.put("27-7", String.valueOf(deviceNoBeijing));
        // 分布式储能在线总容量-北京
        cmdData.put("27-8", String.valueOf(capacityBeijing));
        // 分布式储能在线实时有功-北京
        cmdData.put("27-9", activePowerBeijing.toString());

        // 分布式储能在线数量-天津
        cmdData.put("27-10", String.valueOf(deviceNoTianjin));
        // 分布式储能在线总容量-天津
        cmdData.put("27-11", String.valueOf(capacityTianjin));
        // 分布式储能在线实时有功-天津
        cmdData.put("27-12", activePowerTianjin.toString());

        // 分布式储能在线数量-冀北
        cmdData.put("27-13", String.valueOf(deviceNoJibei));
        // 分布式储能在线总容量-冀北
        cmdData.put("27-14", String.valueOf(capacityJibei));
        // 分布式储能在线实时有功-冀北
        cmdData.put("27-15", activePowerJibei.toString());

        // 分布式储能在线数量-河北
        cmdData.put("27-16", String.valueOf(deviceNoHebei));
        // 分布式储能在线总容量-河北
        cmdData.put("27-17", String.valueOf(capacityHebei));
        // 分布式储能在线实时有功-河北
        cmdData.put("27-18", activePowerHebei.toString());

        // 分布式储能在线数量-山西
        cmdData.put("27-19", String.valueOf(deviceNoShanxi));
        // 分布式储能在线总容量-山西
        cmdData.put("27-20", String.valueOf(capacityShanxi));
        // 分布式储能在线实时有功-山西
        cmdData.put("27-21", activePowerShanxi.toString());

        // 分布式储能在线数量-山东
        cmdData.put("27-22", String.valueOf(deviceNoShandong));
        // 分布式储能在线总容量-山东
        cmdData.put("27-23", String.valueOf(capacityShandong));
        // 分布式储能在线实时有功-山东
        cmdData.put("27-24", activePowerShandong.toString());

        // 京津唐分布式储能AGC投退状态
        cmdData.put("27-25", issueStatus);
        // 京津唐分布式储能有功实发命令（返回值）
        cmdData.put("27-26", issueValue);
        // 华北系统中京津唐分布式储能AGC正控信号（返回值）
        cmdData.put("27-27", issueSign);
        // 京津唐可参与调节的分布式储能实时数量
        cmdData.put("27-28", String.valueOf(deviceNoHuabei));
        // 京津唐可参与调节的分布式储能实时有功（单位MW，以放电为﹢）
        cmdData.put("27-29", activePowerHuabei.toString());
        // 京津唐可参与调节的分布式储能等效SOC
        cmdData.put("27-30", "0");
        // 京津唐可参与调节的分布式储能有功上限（最大可充）
        cmdData.put("27-31", "0");
        // 京津唐可参与调节的分布式储能有功下限（最大可放）
        cmdData.put("27-32", "0");
        // 京津唐可参与调节分布式储能功率最大允许命令步长
        cmdData.put("27-33", "0");

        return cmdData;

    }


    private String getActivePowerForVPPRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> vppConfigs) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }

        String startTimeStamp = retryIssueDTO.getTimestamp();
        Date startTimeDate = new Date(Long.parseLong(startTimeStamp) * 1000);
        DateTime dateTime = new DateTime(startTimeDate);
        String startTime = dateTime.toString(DATE_FORMATTER_MIN);
        String endTime = dateTime.plusSeconds(5).toString(DATE_FORMATTER_SEC);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        vppConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        final BigDecimal[] activePower = {BigDecimal.ZERO};
        bigDataHistoryRespList.stream().filter(Objects::nonNull).forEach(resp -> activePower[0] = MathUtils.add(activePower[0], BigDecimal.valueOf(CollectionUtils.isEmpty(resp.getDataResp()) || null == resp.getDataResp().get(0).getValue() ? 0.0D : resp.getDataResp().get(0).getValue())));
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();

    }

    private String getActivePowerForEHRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> ehConfigs) {
        if (CollectionUtils.isEmpty(ehConfigs)) {
            return BigDecimal.ZERO.setScale(4).toString();
        }

        String startTimeStamp = retryIssueDTO.getTimestamp();
        Date startTimeDate = new Date(Long.parseLong(startTimeStamp) * 1000);
        DateTime dateTime = new DateTime(startTimeDate);
        String startTime = dateTime.toString(DATE_FORMATTER_MIN);
        String endTime = dateTime.plusSeconds(5).toString(DATE_FORMATTER_SEC);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        ehConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        final BigDecimal[] activePower = {BigDecimal.ZERO};
        bigDataHistoryRespList.stream().filter(Objects::nonNull).forEach(resp -> activePower[0] = MathUtils.add(activePower[0], BigDecimal.valueOf(CollectionUtils.isEmpty(resp.getDataResp()) || null == resp.getDataResp().get(0).getValue() ? 0.0D : resp.getDataResp().get(0).getValue())));
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }

    private BigDecimal getActivePowerForDESRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> desConfigs) {

        if (CollectionUtils.isEmpty(desConfigs)) {
            return BigDecimal.ZERO;
        }

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        String startTimeStamp = retryIssueDTO.getTimestamp();
        Date startTimeDate = new Date(Long.parseLong(startTimeStamp) * 1000);
        DateTime dateTime = new DateTime(startTimeDate);
        String startTime = dateTime.toString(DATE_FORMATTER_MIN);
        String endTime = dateTime.plusSeconds(5).toString(DATE_FORMATTER_SEC);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        desConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        final BigDecimal[] activePower = {BigDecimal.ZERO};
        bigDataHistoryRespList.stream().filter(Objects::nonNull).forEach(resp -> activePower[0] = MathUtils.add(activePower[0], BigDecimal.valueOf(CollectionUtils.isEmpty(resp.getDataResp()) || null == resp.getDataResp().get(0).getValue() ? 0.0D : resp.getDataResp().get(0).getValue())));

        // 改变符号
        activePower[0] = BigDecimal.ZERO.subtract(activePower[0]);

        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000), BigDecimal.ROUND_HALF_UP).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

//    public ResultVO<String> singleMeasDeliveryRetry(RetryIssueDTO retryIssueDTO) {
//
//        List<AggregatorEnt> allAggregatorEnt = aggregatorEntService.getOnlineAggregatorEntList();
//        Map<String, AggregatorEnt> stationIdToEntMap = allAggregatorEnt.stream().collect(Collectors.toMap(AggregatorEnt::getStationId, Function.identity(), (k1, k2) -> k2));
//
//        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineAggregatorEntDeviceList();
//        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));
//
//        List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), Lists.newArrayList());
//
//        ResultVO<String> result = new ResultVO<>();
//
//        // step 1 获取所有子企业entId
//        String group = retryIssueDTO.getGroup();
//
//        switch (group) {
//            case "25":
//                result = deliveryMeasDataVPPRetry(retryIssueDTO, stationIdToEntMap, vppConfigs);
//                break;
//            case "26":
//                result = deliveryMeasDataEHRetry(retryIssueDTO, stationIdToEntMap, ehConfigs);
//                break;
//            case "27":
//                result = deliveryMeasDataDESRetry(retryIssueDTO, stationIdToEntMap, desConfigs);
//                break;
//            default:
//                result = ResultVO.fail(StatusCode.F_NO_GROUP.getCode(), StatusCode.F_NO_GROUP.getMsg() + group);
//        }
//
//        return result;
//    }

//    private ResultVO<String> deliveryMeasDataVPPRetry(RetryIssueDTO retryIssueDTO, Map<String, AggregatorEnt> stationIdToEntMap, List<AggregatorEntDevice> vppConfigs) {
//
//        if (CollectionUtils.isEmpty(vppConfigs)) {
//            return ResultVO.success("VPP has no config data");
//        }
//
//        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
//        singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.INDUSTRIAL_LOAD);
//
//        List<Object> singleMeasData = Lists.newArrayList();
//        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForVPPRetry(retryIssueDTO, vppConfigs));
//        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForVPPRetry(retryIssueDTO, vppConfigs));
//
//        // 按systemCode 归并
//        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
//
//        mapGroupingByStationId.forEach((k, v) -> {
//            // P 总有功功率
//            Map<String, String> map = new HashMap<>(16);
//            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String totalActivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListP);
//
//            // Q 总无功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String totalReactivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListQ);
//
//            // Ia A相电流
//            List<BigDataHistoryResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String userElecCurrent = processMeasureDataFromHistoryResp(bigDataHistoryRespListIa);
//
//            // Eptp 有功电度正向量（）
//            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, RoundingMode.HALF_UP).toString();
//
//            map.put("username", stationIdToEntMap.get(k).getEntName());
//            map.put("userActivePower", totalActivePower);
//            map.put("userReactivePower", totalReactivePower);
//            map.put("userElecCurrent", userElecCurrent);
//            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
//            map.put("innerStationId", k);
//
//            singleMeasData.add(map);
//        });
//
//        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);
//
//        String filename = getFileName(singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//
//        Map<String, List<Object>> map = new HashMap<>(8);
//        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
//
//        ResultVO<String> result;
//        String response = "成功";
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                response = StatusCode.F_A.getMsg();
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//
//            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
//            String encodeString = Base64.getEncoder().encodeToString(bytes);
//            log.info("encodeString:{}", encodeString);
//
//            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
//            response = greeter.commitFile(filename, encodeString);
//            result = ResultVO.success(response, response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//            result = ResultVO.fail(StatusCode.F_URL_UNAVAILABLE.getCode(), response);
//        } finally {
//            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
//            singleMeasDeliveryLog.setFileName(filename);
//            // 日志入库为原始请求报文
//            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
//            singleMeasDeliveryLog.setDeliveryStatus(response);
//            singleMeasDeliveryLog.setCreateTime(new Date());
//            singleMeasDeliveryLog.setIssueTime(Long.valueOf(retryIssueDTO.getTimestamp()));
//            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
//        }
//
//        return result;
//    }

//    private ResultVO<String> deliveryMeasDataEHRetry(RetryIssueDTO retryIssueDTO, Map<String, AggregatorEnt> stationIdToEntMap, List<AggregatorEntDevice> ehConfigs) {
//
//        if (CollectionUtils.isEmpty(ehConfigs)) {
//            return ResultVO.success("EH has no config data");
//        }
//
//        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
//        singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.ELECTRIC_HEATING);
//
//        List<Object> singleMeasData = Lists.newArrayList();
//        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForEHRetry(retryIssueDTO, ehConfigs));
//        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForEHRetry(retryIssueDTO, ehConfigs));
//
//        // 按systemCode 归并
//        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
//
//        mapGroupingByStationId.forEach((k, v) -> {
//            Map<String, String> map = new HashMap<>(16);
//            // P 总有功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String totalActivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListP);
//
//            // Q 总无功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String totalReactivePower = processMeasureDataFromHistoryResp(bigDataHistoryRespListQ);
//
//            // Ia A相电流
//            List<BigDataHistoryResp> bigDataHistoryRespListIa = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Ia".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            String userElecCurrent = processMeasureDataFromHistoryResp(bigDataHistoryRespListIa);
//
//            // Eptp 有功电度正向量（）
//            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, RoundingMode.HALF_UP).toString();
//
//            map.put("username", stationIdToEntMap.get(k).getEntName());
//            map.put("userActivePower", totalActivePower);
//            map.put("userReactivePower", totalReactivePower);
//            map.put("userElecCurrent", userElecCurrent);
//            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
//            map.put("innerStationId", k);
//
//            singleMeasData.add(map);
//        });
//
//        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);
//
//        String filename = getFileName(singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//
//        Map<String, List<Object>> map = new HashMap<>(16);
//        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
//
//        ResultVO<String> result;
//        String response = "成功";
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                response = StatusCode.F_A.getMsg();
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//
//            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
//            String encodeString = Base64.getEncoder().encodeToString(bytes);
//            log.info("encodeString:{}", encodeString);
//
//            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
//            response = greeter.commitFile(filename, encodeString);
//            result = ResultVO.success(response, response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//            result = ResultVO.fail(StatusCode.F_URL_UNAVAILABLE.getCode(), response);
//        } finally {
//            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
//            singleMeasDeliveryLog.setFileName(filename);
//            // 日志入库为原始请求报文
//            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
//            singleMeasDeliveryLog.setDeliveryStatus(response);
//            singleMeasDeliveryLog.setCreateTime(new Date());
//            singleMeasDeliveryLog.setIssueTime(Long.valueOf(retryIssueDTO.getTimestamp()));
//            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
//        }
//
//        return result;
//    }

//    private ResultVO<String> deliveryMeasDataDESRetry(RetryIssueDTO retryIssueDTO, Map<String, AggregatorEnt> stationIdToEntMap, List<AggregatorEntDevice> desConfigs) {
//
//        if (CollectionUtils.isEmpty(desConfigs)) {
//            return ResultVO.success("DES has no config data");
//        }
//
//        ResultVO<String> result = new ResultVO<>();
//
//        SingleMeasDeliveryReq singleMeasDeliveryReq = new SingleMeasDeliveryReq();
////        singleMeasDeliveryReq.setEnergyModelEnum(EnergyModelEnum.DISTRIBUTED_ENERGY);
//
//
//
//        List<Object> singleMeasData = Lists.newArrayList();
//        List<BigDataHistoryResp> measData = getMeasDataByMetric(getHistoryReqForDESRetry(retryIssueDTO, desConfigs));
//        Map<String, BigDecimal> zeroEPTPMap = getZeroEPTPMap(getHistoryReqForDESRetry(retryIssueDTO, desConfigs));
//
//        // 按systemCode 归并
//        Map<String, List<BigDataHistoryResp>> mapGroupingByStationId = measData.stream().collect(Collectors.groupingBy(BigDataHistoryResp::getStaId));
//
//        mapGroupingByStationId.forEach((k, v) -> {
//            Map<String, String> map = new HashMap<>(16);
//
//            // P 总有功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.P".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            // 分布式储能P值与大数据值相反
//            String totalActivePower = BigDecimal.ZERO.subtract(new BigDecimal(processMeasureDataFromHistoryResp(bigDataHistoryRespListP))).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
//
//            // Q 总无功功率
//            List<BigDataHistoryResp> bigDataHistoryRespListQ = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Q".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            // 分布式储能Q值与大数据值相反
//            String totalReactivePower = BigDecimal.ZERO.subtract(new BigDecimal(processMeasureDataFromHistoryResp(bigDataHistoryRespListQ))).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
//
//            // Eptp 有功电度正向量（）
//            final BigDecimal[] elecQuantity = {BigDecimal.ZERO};
//            List<BigDataHistoryResp> bigDataHistoryRespListEPTP = v.stream().filter(Objects::nonNull).filter(x -> "EMS.Eptp".equalsIgnoreCase(x.getMetric()) && CollectionUtils.isNotEmpty(x.getDataResp())).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(bigDataHistoryRespListEPTP)) {
//                bigDataHistoryRespListEPTP.stream().filter(Objects::nonNull).forEach(bigDataHistoryResp -> {
//                    BigDecimal temElecQuantity = zeroEPTPMap.getOrDefault(k + "-" + bigDataHistoryResp.getEquipMK() + "-" + bigDataHistoryResp.getEquipID(), BigDecimal.ZERO);
//                    elecQuantity[0] = elecQuantity[0].add(temElecQuantity);
//                });
//            }
//            String todayZeroElecQuantity = elecQuantity[0].setScale(4, BigDecimal.ROUND_HALF_UP).toString();
//
//            map.put("stationName", stationIdToEntMap.get(k).getEntName());
//            map.put("totalActivePower", totalActivePower);
//            map.put("totalReactivePower", totalReactivePower);
//            map.put("todayZeroElecQuantity", todayZeroElecQuantity);
//            map.put("innerStationId", k);
//
//            singleMeasData.add(map);
//        });
//
//        singleMeasDeliveryReq.setSingleMeasData(singleMeasData);
//
//        String filename = getFileName(singleMeasDeliveryReq.getEnergyModelEnum(), "MEAS");
//        String tempalteName = TemplateNameEnum.getByTypeAndNo("MEAS", singleMeasDeliveryReq.getEnergyModelEnum().getChannelNo()).getName();
//
//        Map<String, List<Object>> map = new HashMap<>(16);
//        map.put("detailList", singleMeasDeliveryReq.getSingleMeasData());
//
//        String response = "成功";
//        try {
//            ResultVO<String> templateResult = freemarkerService.process(tempalteName, map);
//
//            if (templateResult.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
//                response = StatusCode.F_A.getMsg();
//                throw new BaseException(StatusCode.F_A.getCode(), StatusCode.F_A.getMsg());
//            }
//
//            String templateData = templateResult.getData();
//
//            byte[] bytes = templateData.getBytes(Charsets.UTF_8);
//            String encodeString = Base64.getEncoder().encodeToString(bytes);
//            log.info("encodeString:{}", encodeString);
//
//            Greeter greeter = clientConfig.greeter(singleModelAndMeasUrl);
//            response = greeter.commitFile(filename, encodeString);
//            result = ResultVO.success(response, response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//            result = ResultVO.fail(StatusCode.F_URL_UNAVAILABLE.getCode(), response);
//        } finally {
//            SingleMeasDeliveryLog singleMeasDeliveryLog = new SingleMeasDeliveryLog();
//            singleMeasDeliveryLog.setFileName(filename);
//            // 日志入库为原始请求报文
//            singleMeasDeliveryLog.setFileByte(JSONObject.toJSONString(singleMeasDeliveryReq));
//            singleMeasDeliveryLog.setDeliveryStatus(response);
//            singleMeasDeliveryLog.setCreateTime(new Date());
//            singleMeasDeliveryLog.setIssueTime(Long.valueOf(retryIssueDTO.getTimestamp()));
//            singleMeasDeliveryLogService.addLog(singleMeasDeliveryLog);
//        }
//
//        return result;
//    }

    private HistoryReq getHistoryReqForVPPRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> vppConfigs) {

        String startTimeStamp = retryIssueDTO.getTimestamp();
        Date startTimeDate = new Date(Long.parseLong(startTimeStamp) * 1000);
        DateTime dateTime = new DateTime(startTimeDate);
        String endTime = dateTime.toString(DATE_FORMATTER_MIN_LAST);
        String startTime = dateTime.minusMinutes(4).toString(DATE_FORMATTER_MIN);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        vppConfigs.forEach(config -> {
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

    private HistoryReq getHistoryReqForEHRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> ehConfigs) {

        String startTimeStamp = retryIssueDTO.getTimestamp();
        Date startTimeDate = new Date(Long.parseLong(startTimeStamp) * 1000);
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

    private HistoryReq getHistoryReqForDESRetry(RetryIssueDTO retryIssueDTO, List<AggregatorEntDevice> desConfigs) {

        String startTimeStamp = retryIssueDTO.getTimestamp();
        Date startTimeDate = new Date(Long.parseLong(startTimeStamp) * 1000);
        DateTime dateTime = new DateTime(startTimeDate);
        String endTime = dateTime.toString(DATE_FORMATTER_MIN_LAST);
        String startTime = dateTime.minusMinutes(4).toString(DATE_FORMATTER_MIN);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");
        List<OpentsdbReq> listQueries = Lists.newArrayList();

        desConfigs.forEach(config -> {

            OpentsdbReq opentsdbReq01 = new OpentsdbReq();
            opentsdbReq01.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq01.setMetric("EMS.P");
            opentsdbReq01.setAggregator("last");
            TagVO tag01 = new TagVO();
            tag01.setStaId(config.getStationId());
            tag01.setEquipMK(config.getDeviceType());
            tag01.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq01.setTags(tag01);
            listQueries.add(opentsdbReq01);

            OpentsdbReq opentsdbReq02 = new OpentsdbReq();
            opentsdbReq02.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq02.setMetric("EMS.Q");
            opentsdbReq02.setAggregator("last");
            TagVO tag02 = new TagVO();
            tag02.setStaId(config.getStationId());
            tag02.setEquipMK(config.getDeviceType());
            tag02.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq02.setTags(tag02);
            listQueries.add(opentsdbReq02);

            OpentsdbReq opentsdbReq03 = new OpentsdbReq();
            opentsdbReq03.setDownsample(ONE_MIN_LAST_NONE);
            opentsdbReq03.setMetric("EMS.Eptp");
            opentsdbReq03.setAggregator("last");
            TagVO tag03 = new TagVO();
            tag03.setStaId(config.getStationId());
            tag03.setEquipMK(config.getDeviceType());
            tag03.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq03.setTags(tag03);
            listQueries.add(opentsdbReq03);
        });

        historyReq.setListQueries(listQueries);

        return historyReq;
    }

//    public ResultVO<String> totalDataFromBigDataToDB(String dateTime) {
//
//        if (StringUtils.isBlank(dateTime)) {
//            return ResultVO.fail(StatusCode.E_B.getCode(), StatusCode.E_B.getMsg());
//        }
//
//        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineAggregatorEntDeviceList();
//        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));
//
//        List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), Lists.newArrayList());
//
//        Map<String, String> vppCmdData = processVPPCmdDataFromBigData(vppConfigs, dateTime);
//        Map<String, String> ehCmdData = processEHCmdDataFromBigData(ehConfigs, dateTime);
//        Map<String, String> desCmdData = processDESCmdDataFromBigData(desConfigs, dateTime);
//
//        Map<String, String> cmdData = new LinkedHashMap<>();
//        cmdData.putAll(vppCmdData);
//        cmdData.putAll(ehCmdData);
//        cmdData.putAll(desCmdData);
//
//        String groupNo = getGroupNo(vppCmdData, ehCmdData, desCmdData);
//
//        log.info("totalDataDelivery-入参:{}", JSONObject.toJSONString(cmdData));
//
//        ResultVO<String> resultVO;
//        //数据解析成功!
//        String response = "数据解析成功!";
//        try {
//            resultVO = ResultVO.success(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = e.getMessage();
//            resultVO = ResultVO.fail(StatusCode.F_URL_UNAVAILABLE.getCode(), response);
//        } finally {
//
//            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
//            Date deliveryTime = DateTime.parse(dateTime, dateTimeFormatter).toDate();
//            TotalDeliveryLog totalDeliveryLog = new TotalDeliveryLog();
//            totalDeliveryLog.setCreateTime(new Date());
//            totalDeliveryLog.setValue(JSONObject.toJSONString(cmdData));
//            totalDeliveryLog.setDeliveryStatus(response);
//            totalDeliveryLog.setGroupNo(groupNo);
//            totalDeliveryLog.setDeliveryTime(deliveryTime);
//            totalDeliveryLogService.addLog(totalDeliveryLog);
//            log.info("totalDeliveryLog-日志:{}", JSONObject.toJSONString(totalDeliveryLog));
//        }
//
//        return resultVO;
//    }

    private String getGroupNo(Map<String, String> vppCmdData, Map<String, String> ehCmdData, Map<String, String> desCmdData) {
        List<String> groupNoList = Lists.newArrayList();
        if (MapUtils.isNotEmpty(vppCmdData)) {
            groupNoList.add(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo());
        }
        if (MapUtils.isNotEmpty(ehCmdData)) {
            groupNoList.add(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo());
        }
        if (MapUtils.isNotEmpty(desCmdData)) {
            groupNoList.add(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo());
        }
        String groupNo = groupNoList.stream().collect(Collectors.joining("_"));
        return groupNo;
    }

    private Map<String, String> processDESCmdDataFromBigData(List<AggregatorEntDevice> desConfigs, String dateTime) {

        if (CollectionUtils.isEmpty(desConfigs)) {
            return Maps.newHashMap();
        }

        // 从control_issue_log 获取最近的控制下发信息
        String issueValue = "0.000000";
        String issueStatus = "1";
        String issueSign = "1";
        ControlIssueLog controlIssueLog = controlIssueLogService.getLastLogByGroupNoCustom(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), dateTime);

        if (null != controlIssueLog && null != controlIssueLog.getCmdData()) {
            JSONObject cmdData = JSONObject.parseObject(controlIssueLog.getCmdData());
            issueValue = StringUtils.isBlank(cmdData.getString("27-1")) ? issueValue : cmdData.getString("27-1");
            issueStatus = StringUtils.isBlank(cmdData.getString("27-3")) ? issueStatus : cmdData.getString("27-3");
            issueSign = StringUtils.isBlank(cmdData.getString("27-4")) ? issueSign : cmdData.getString("27-4");
        }

        List<AggregatorEntDevice> deviceConfigsBeijing = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_BEIJING)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsTianjin = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_TIANJIN)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsHebei = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_HEBEI)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsShanxi = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_SHANXI)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsShandong = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_SHANDONG)).collect(Collectors.toList());
        List<AggregatorEntDevice> deviceConfigsJibei = desConfigs.stream().filter(device -> device.getStateGridCode().startsWith(STATE_GRID_JIBEI)).collect(Collectors.toList());

        // 区域设备数量
        int deviceNoBeijing = deviceConfigsBeijing.size();
        int deviceNoTianjin = deviceConfigsTianjin.size();
        int deviceNoHebei = deviceConfigsHebei.size();
        int deviceNoShanxi = deviceConfigsShanxi.size();
        int deviceNoShandong = deviceConfigsShandong.size();
        int deviceNoJibei = deviceConfigsJibei.size();

        // 区域设备容量
        double capacityBeijing = CollectionUtils.isEmpty(deviceConfigsBeijing) ? 0.0 : deviceConfigsBeijing.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityBeijing = capacityBeijing / 1000;
        double capacityTianjin = CollectionUtils.isEmpty(deviceConfigsTianjin) ? 0.0 : deviceConfigsTianjin.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityTianjin = capacityTianjin / 1000;
        double capacityHebei = CollectionUtils.isEmpty(deviceConfigsHebei) ? 0.0 : deviceConfigsHebei.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityHebei = capacityHebei / 1000;
        double capacityShanxi = CollectionUtils.isEmpty(deviceConfigsShanxi) ? 0.0 : deviceConfigsShanxi.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityShanxi = capacityShanxi / 1000;
        double capacityShandong = CollectionUtils.isEmpty(deviceConfigsShandong) ? 0.0 : deviceConfigsShandong.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityShandong = capacityShandong / 1000;
        double capacityJibei = CollectionUtils.isEmpty(deviceConfigsJibei) ? 0.0 : deviceConfigsJibei.stream().map(AggregatorEntDevice::getPower).collect(Collectors.toList()).stream().reduce(Double::sum).get();
        capacityJibei = capacityJibei / 1000;

        // 区域设备实时功率
        BigDecimal activePowerBeijing = getActivePowerForDESCustom(deviceConfigsBeijing, dateTime);
        BigDecimal activePowerTianjin = getActivePowerForDESCustom(deviceConfigsTianjin, dateTime);
        BigDecimal activePowerHebei = getActivePowerForDESCustom(deviceConfigsHebei, dateTime);
        BigDecimal activePowerShanxi = getActivePowerForDESCustom(deviceConfigsShanxi, dateTime);
        BigDecimal activePowerShandong = getActivePowerForDESCustom(deviceConfigsShandong, dateTime);
        BigDecimal activePowerJibei = getActivePowerForDESCustom(deviceConfigsJibei, dateTime);

        //  根据子企业获取所有对应资源的数据
        Map<String, String> cmdData = new LinkedHashMap<>();

        int deviceNoJjt = deviceNoBeijing + deviceNoTianjin;
        int deviceNoHuabei = deviceNoJjt + deviceNoHebei + deviceNoShanxi + deviceNoShandong + deviceNoJibei;

        double capacityJjt = capacityBeijing + capacityTianjin;
        double capacityHuabei = capacityJjt + capacityHebei + capacityShanxi + capacityShandong + capacityJibei;

        BigDecimal activePowerJjt = activePowerBeijing.add(activePowerTianjin).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal activePowerHuabei = activePowerJjt.add(activePowerHebei).add(activePowerShanxi).add(activePowerShandong).add(activePowerJibei).setScale(4, BigDecimal.ROUND_HALF_UP);

        // 分布式储能在线数量-华北
        cmdData.put("27-1", String.valueOf(deviceNoHuabei));
        // 分布式储能在线总容量-华北
        cmdData.put("27-2", String.valueOf(capacityHuabei));
        // 分布式储能在线实时有功-华北
        cmdData.put("27-3", activePowerHuabei.toString());

        // 分布式储能在线数量-京津唐(北京+天津)
        cmdData.put("27-4", String.valueOf(deviceNoJjt));
        // 分布式储能在线总容量-京津唐(北京+天津)
        cmdData.put("27-5", String.valueOf(capacityJjt));
        // 分布式储能在线实时有功-京津唐
        cmdData.put("27-6", activePowerJjt.toString());

        // 分布式储能在线数量-北京(属于京津唐电网)
        cmdData.put("27-7", String.valueOf(deviceNoBeijing));
        // 分布式储能在线总容量-北京
        cmdData.put("27-8", String.valueOf(capacityBeijing));
        // 分布式储能在线实时有功-北京
        cmdData.put("27-9", activePowerBeijing.toString());

        // 分布式储能在线数量-天津
        cmdData.put("27-10", String.valueOf(deviceNoTianjin));
        // 分布式储能在线总容量-天津
        cmdData.put("27-11", String.valueOf(capacityTianjin));
        // 分布式储能在线实时有功-天津
        cmdData.put("27-12", activePowerTianjin.toString());

        // 分布式储能在线数量-冀北
        cmdData.put("27-13", String.valueOf(deviceNoJibei));
        // 分布式储能在线总容量-冀北
        cmdData.put("27-14", String.valueOf(capacityJibei));
        // 分布式储能在线实时有功-冀北
        cmdData.put("27-15", activePowerJibei.toString());

        // 分布式储能在线数量-河北
        cmdData.put("27-16", String.valueOf(deviceNoHebei));
        // 分布式储能在线总容量-河北
        cmdData.put("27-17", String.valueOf(capacityHebei));
        // 分布式储能在线实时有功-河北
        cmdData.put("27-18", activePowerHebei.toString());

        // 分布式储能在线数量-山西
        cmdData.put("27-19", String.valueOf(deviceNoShanxi));
        // 分布式储能在线总容量-山西
        cmdData.put("27-20", String.valueOf(capacityShanxi));
        // 分布式储能在线实时有功-山西
        cmdData.put("27-21", activePowerShanxi.toString());

        // 分布式储能在线数量-山东
        cmdData.put("27-22", String.valueOf(deviceNoShandong));
        // 分布式储能在线总容量-山东
        cmdData.put("27-23", String.valueOf(capacityShandong));
        // 分布式储能在线实时有功-山东
        cmdData.put("27-24", activePowerShandong.toString());

        // 京津唐分布式储能AGC投退状态
        cmdData.put("27-25", issueStatus);
        // 京津唐分布式储能有功实发命令（返回值）
        cmdData.put("27-26", issueValue);
        // 华北系统中京津唐分布式储能AGC正控信号（返回值）
        cmdData.put("27-27", issueSign);
        // 京津唐可参与调节的分布式储能实时数量
        cmdData.put("27-28", String.valueOf(deviceNoHuabei));
        // 京津唐可参与调节的分布式储能实时有功（单位MW，以放电为﹢）
        cmdData.put("27-29", activePowerHuabei.toString());
        // 京津唐可参与调节的分布式储能等效SOC
        cmdData.put("27-30", "0");
        // 京津唐可参与调节的分布式储能有功上限（最大可充）
        cmdData.put("27-31", "0");
        // 京津唐可参与调节的分布式储能有功下限（最大可放）
        cmdData.put("27-32", "0");
        // 京津唐可参与调节分布式储能功率最大允许命令步长
        cmdData.put("27-33", "0");

        return cmdData;
    }

    private BigDecimal getActivePowerForDESCustom(List<AggregatorEntDevice> desConfigs, String dateTime) {

        if (CollectionUtils.isEmpty(desConfigs)) {
            return BigDecimal.ZERO;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMATTER_SEC);
        String startTime = DateTime.parse(dateTime, dateTimeFormatter).minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.parse(dateTime, dateTimeFormatter).toString(DATE_FORMATTER_MIN_LAST);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        desConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        // 按站点和设备排序
        Map<String, BigDataHistoryResp> map = bigDataHistoryRespList.stream().collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));

        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> powerData = v.getDataResp();
            BigDecimal value = processTotalPowerData(powerData);
            // 分布式储能P值与大数据值相反
            value = BigDecimal.ZERO.subtract(value);
            activePower[0] = MathUtils.add(activePower[0], value);
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    private Map<String, String> processEHCmdDataFromBigData(List<AggregatorEntDevice> ehConfigs, String dateTime) {

        if (CollectionUtils.isEmpty(ehConfigs)) {
            return Maps.newHashMap();
        }

        String issueValue = "0.000000";
        String issueStatus = "1";
        String issueSign = "1";
        ControlIssueLog controlIssueLog = controlIssueLogService.getLastLogByGroupNoCustom(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), dateTime);

        if (null != controlIssueLog && null != controlIssueLog.getCmdData()) {
            JSONObject cmdData = JSONObject.parseObject(controlIssueLog.getCmdData());
            issueValue = StringUtils.isBlank(cmdData.getString("26-1")) ? issueValue : cmdData.getString("26-1");
            issueStatus = StringUtils.isBlank(cmdData.getString("26-3")) ? issueStatus : cmdData.getString("26-3");
            issueSign = StringUtils.isBlank(cmdData.getString("26-4")) ? issueSign : cmdData.getString("26-4");
        }

        //  根据子企业获取所有对应资源的数据
        Map<String, String> cmdData = new LinkedHashMap<>();

        // 京津唐电采暖AGC投退状态
        cmdData.put("26-1", issueStatus);
        // 京津唐电采暖有功实发命令（返回值）
        cmdData.put("26-2", issueValue);
        // 华北系统中京津唐电采暖AGC正控信号（返回值）
        cmdData.put("26-3", issueSign);
        // 可参与调节的京津唐电采暖实时数量
        String ehSize = String.valueOf(ehConfigs.size());
        cmdData.put("26-4", ehSize);
        // 参与调节的京津唐电采暖实时有功（单位MW，以用电为﹢）
        String activePowerForEH = getActivePowerForEHCustom(ehConfigs, dateTime);
        cmdData.put("26-5", activePowerForEH);
        // 参与调节的京津唐电采暖功率可维持最大时间
        cmdData.put("26-6", "0");
        // 参与调节的京津唐电采暖有功上限（最大用电）
        cmdData.put("26-7", "0");
        // 参与调节的京津唐电采暖下限（最小用电）
        cmdData.put("26-8", "0");
        // 参与调节京津唐电采暖功率最大允许命令步长
        cmdData.put("26-9", "0");
        // 可调节的京津唐电采暖实时有功（单位MW，以用电为﹢）
        cmdData.put("26-10", activePowerForEH);

        return cmdData;
    }

    private String getActivePowerForEHCustom(List<AggregatorEntDevice> ehConfigs, String dateTime) {


        if (CollectionUtils.isEmpty(ehConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMATTER_SEC);
        String startTime = DateTime.parse(dateTime, dateTimeFormatter).minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.parse(dateTime, dateTimeFormatter).toString(DATE_FORMATTER_MIN_LAST);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        ehConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        // 按站点和设备排序
        Map<String, BigDataHistoryResp> map = bigDataHistoryRespList.stream().collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));

        // 电流为0 P为0；电流不为0，当前值为空，取前一个值
        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> powerData = v.getDataResp();
            BigDecimal value = processTotalPowerData(powerData);
            activePower[0] = MathUtils.add(activePower[0], value);
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }

    private Map<String, String> processVPPCmdDataFromBigData(List<AggregatorEntDevice> vppConfigs, String dateTime) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return Maps.newHashMap();
        }

        // 从control_issue_log 获取最近的控制下发信息
        String issueValue = "0.000000";
        String issueStatus = "1";
        String issueSign = "1";
        ControlIssueLog controlIssueLog = controlIssueLogService.getLastLogByGroupNo(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo());

        if (null != controlIssueLog && null != controlIssueLog.getCmdData()) {
            JSONObject cmdData = JSONObject.parseObject(controlIssueLog.getCmdData());
            issueValue = StringUtils.isBlank(cmdData.getString("25-1")) ? issueValue : cmdData.getString("25-1");
            issueStatus = StringUtils.isBlank(cmdData.getString("25-3")) ? issueStatus : cmdData.getString("25-3");
            issueSign = StringUtils.isBlank(cmdData.getString("25-4")) ? issueSign : cmdData.getString("25-4");
        }

        //  根据子企业获取所有对应资源的数据
        Map<String, String> cmdData = new LinkedHashMap<>();

        // 工业负荷AGC投退状态
        cmdData.put("25-1", issueStatus);
        // 工业负荷有功实发命令（冀北返回值）
        cmdData.put("25-2", issueValue);
        // 华北系统中工业负荷厂AGC正控信号（返回值）
        cmdData.put("25-3", issueSign);
        // 可参与调节的工业负荷终端数量
        String vppSize = String.valueOf(vppConfigs.size());
        cmdData.put("25-4", vppSize);
        // 可参与调节的工业负荷实时有功（单位MW，以充电为﹢）
        String activePowerForVPP = getActivePowerForVPPCustom(vppConfigs, dateTime);
        cmdData.put("25-5", activePowerForVPP);
        // 可参与调节的工业负荷按当前功率最大可持续时间
        cmdData.put("25-6", "0");
        // 可参与调节的工业负荷终端功率上限（最大可充）
        cmdData.put("25-7", "0");
        // 可参与调节的工业负荷终端功率下限（最大可放）
        cmdData.put("25-8", "0");
        // 工业负荷整体运行模式（0仅可充，1仅可放，2可充可放）
        cmdData.put("25-9", "1");
        // 工业负荷最大允许命令步长
        cmdData.put("25-10", "0");
        // 工业负荷类型数量
        cmdData.put("25-11", vppSize);
        // 工业负荷类型实时有功
        cmdData.put("25-12", activePowerForVPP);

        return cmdData;
    }

    private String getActivePowerForVPPCustom(List<AggregatorEntDevice> vppConfigs, String dateTime) {

        if (CollectionUtils.isEmpty(vppConfigs)) {
            return BigDecimal.ZERO.setScale(4, BigDecimal.ROUND_HALF_UP).toString();
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMATTER_SEC);
        String startTime = DateTime.parse(dateTime, dateTimeFormatter).minusMinutes(4).toString(DATE_FORMATTER_MIN);
        String endTime = DateTime.parse(dateTime, dateTimeFormatter).toString(DATE_FORMATTER_MIN_LAST);

        HistoryReq historyReq = new HistoryReq();
        historyReq.setStartTime(startTime);
        historyReq.setEndTime(endTime);
        historyReq.setDataSource("EMS");

        List<OpentsdbReq> listQueries = Lists.newArrayList();

        vppConfigs.forEach(config -> {
            OpentsdbReq opentsdbReq = new OpentsdbReq();
            opentsdbReq.setDownsample(ONE_MIN_LAST_NULL);
            opentsdbReq.setMetric("EMS.P");
            opentsdbReq.setAggregator("last");
            TagVO tag = new TagVO();
            tag.setStaId(config.getStationId());
            tag.setEquipMK(config.getDeviceType());
            tag.setEquipID(StringUtils.split(config.getDeviceId(), "_")[1]);
            opentsdbReq.setTags(tag);
            listQueries.add(opentsdbReq);
        });

        historyReq.setListQueries(listQueries);
        List<BigDataHistoryResp> bigDataHistoryRespList = bigDataHandlerService.getHistory(historyReq, "0");

        // 按站点和设备排序
        Map<String, BigDataHistoryResp> map = bigDataHistoryRespList.stream().collect(Collectors.toMap((resp -> resp.getStaId() + "#" + resp.getEquipID()), Function.identity(), (key1, key2) -> key2));

        // 电流为0 P为0；电流不为0，当前值为空，取前一个值
        final BigDecimal[] activePower = {BigDecimal.ZERO};
        map.forEach((k, v) -> {
            List<DataResp> powerData = v.getDataResp();
            BigDecimal value = processTotalPowerData(powerData);
            activePower[0] = MathUtils.add(activePower[0], value);
        });
        // 将KW转换为MW
        return activePower[0].divide(new BigDecimal(1000)).setScale(4, BigDecimal.ROUND_HALF_UP).toString();
    }

//    public ResultVO<String> singleMeasFromBigData(String dateTime) {
//
//        if (StringUtils.isBlank(dateTime)) {
//            return ResultVO.fail(StatusCode.E_B.getCode(), StatusCode.E_B.getMsg());
//        }
//
//        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getOnlineAggregatorEntDeviceList();
//        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));
//
//        List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), Lists.newArrayList());
//
//        String responseVPP = deliveryMeasDataVPPFromBigData(vppConfigs, dateTime);
//        String responseEH = deliveryMeasDataEHFromBigData(ehConfigs, dateTime);
//        String responseDES = deliveryMeasDataDESFromBigData(desConfigs, dateTime);
//
//        return ResultVO.success(dateTime + responseVPP + responseEH + responseDES);
//    }

//    public ResultVO<String> singleModelFromDBToFile() {
//
//        List<AggregatorEntDevice> aggregatorEntDeviceList = aggregatorEntDeviceService.getModelAggregatorEntDeviceList();
//        Map<String, List<AggregatorEntDevice>> configMapByResourceType = aggregatorEntDeviceList.stream().collect(Collectors.groupingBy(AggregatorEntDevice::getResourceTypeId));
//
//        List<AggregatorEntDevice> vppConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.INDUSTRIAL_LOAD.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> ehConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.ELECTRIC_HEATING.getChannelNo(), Lists.newArrayList());
//        List<AggregatorEntDevice> desConfigs = configMapByResourceType.getOrDefault(EnergyModelEnum.DISTRIBUTED_ENERGY.getChannelNo(), Lists.newArrayList());
//
//        String responseVPP = deliveryModelDataVPPCustom(vppConfigs);
//        String responseEH = deliveryModelDataEHCustom(ehConfigs);
//        String responseDES = deliveryModelDataDESCustom(desConfigs);
//
//        return ResultVO.success(responseVPP + responseEH + responseDES);
//    }

    /**
     * 按照华北指定格式生产总加数据补招excel文件
     * 电采暖全口径和参与口径26-5
     * 储能全口径和参与口径27-6
     * timeType 0:createTiem，数据库数据正常，无需从大数据计算数据，限定条件为数据创建时间create_time
     * timeType 1:deliveryTime，数据库数据缺失，需从大数据计算数据并落库，限定条件为实际数据上送时间delivery_time
     *
     * @param beginTimeStr
     * @param endTimeStr
     * @return
     */
    public ResultVO<String> totalDataFromDBToFile(String beginTimeStr, String endTimeStr, String timeType) {

        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");
        java.time.format.DateTimeFormatter dateTimeHourStr = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:00:00");
        java.time.format.DateTimeFormatter dateTimeMinuteStr = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:00");

        List<TotalDeliveryLog> logsByTime = Lists.newArrayList();
        if (StringUtils.equals(timeType, "0")) {
            logsByTime = totalDeliveryLogService.getLogsByCreateTimeAsc(beginTimeStr, endTimeStr);
        }
        if (StringUtils.equals(timeType, "1")) {
            logsByTime = totalDeliveryLogService.getLogsByDeliveryTimeAsc(beginTimeStr, endTimeStr);
        }
        Map<String, String> valuesMap = Maps.newTreeMap();
        // 处理数据库数据
        ZoneId zoneId = ZoneId.systemDefault();
        logsByTime.forEach(logByTime -> {
            Date queryTime = logByTime.getCreateTime();
            if (StringUtils.equals(timeType, "0")) {
                queryTime = logByTime.getCreateTime();
            }
            if (StringUtils.equals(timeType, "1")) {
                queryTime = logByTime.getDeliveryTime();
            }
            Instant instant = queryTime.toInstant();
            String dateTimestr = instant.atZone(zoneId).toLocalDateTime().format(dateTimeMinuteStr);
            String value = logByTime.getValue();
            JSONObject valueJson = JSONObject.parseObject(value);
            String vppValue = valueJson.getString("25-5");
            String ehValue = valueJson.getString("26-5");
            String desValue = valueJson.getString("27-6");
            value = StringUtils.joinWith("#", vppValue, ehValue, desValue);
            valuesMap.put(dateTimestr, value);
        });

        LocalDateTime beginTime = LocalDateTime.parse(beginTimeStr, dateTimeFormatter);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr, dateTimeFormatter);
        long minuteBetween = ChronoUnit.MINUTES.between(beginTime, endTime);
        int hours = BigDecimal.valueOf(minuteBetween).divide(BigDecimal.valueOf(60), BigDecimal.ROUND_UP).intValue();

        List<String> header = getHeader();

        List<List<String>> colListVPP = Lists.newArrayList();
        colListVPP.add(header);

        List<List<String>> colListEH = Lists.newArrayList();
        colListEH.add(header);

        List<List<String>> colListDES = Lists.newArrayList();
        colListDES.add(header);

        LocalDateTime beginTimeHour;

        for (int i = 0; i < hours; i++) {
            beginTimeHour = beginTime.plusHours(i);
            LocalDateTime beginTimeMinute;
            String time = beginTimeHour.format(dateTimeHourStr);

            List<String> rowListVPPAll = Lists.newArrayList();
            List<String> rowListVPPParticipation = Lists.newArrayList();
            rowListVPPAll.add(time);
            rowListVPPAll.add("全口径");
            rowListVPPParticipation.add(time);
            rowListVPPParticipation.add("参与口径");

            List<String> rowListEHAll = Lists.newArrayList();
            List<String> rowListEHParticipation = Lists.newArrayList();
            rowListEHAll.add(time);
            rowListEHAll.add("全口径");
            rowListEHParticipation.add(time);
            rowListEHParticipation.add("参与口径");

            List<String> rowListDESAll = Lists.newArrayList();
            List<String> rowListDESParticipation = Lists.newArrayList();
            rowListDESAll.add(time);
            rowListDESAll.add("全口径");
            rowListDESParticipation.add(time);
            rowListDESParticipation.add("参与口径");

            for (int j = 0; j < 60; j++) {
                beginTimeMinute = beginTimeHour.plusMinutes(j);
                String timeKey = beginTimeMinute.format(dateTimeMinuteStr);
                String valuestr = valuesMap.get(timeKey);
                if (StringUtils.isBlank(valuestr)) {
                    log.info("补招数据为空时间：{}", timeKey);
                    valuestr = "0.0000#0.0000#0.0000";
                }
                String[] values = StringUtils.split(valuestr, "#");

                rowListVPPAll.add(values[0]);
                rowListVPPParticipation.add(values[0]);

                rowListEHAll.add(values[1]);
                rowListEHParticipation.add(values[1]);

                rowListDESAll.add(values[2]);
                rowListDESParticipation.add(values[2]);
            }
            colListVPP.add(rowListVPPAll);
            colListVPP.add(rowListVPPParticipation);
            colListEH.add(rowListEHAll);
            colListEH.add(rowListEHParticipation);
            colListDES.add(rowListDESAll);
            colListDES.add(rowListDESParticipation);
        }

        noModelWrite(beginTimeStr, endTimeStr, colListVPP, colListEH, colListDES);

        return ResultVO.success();
    }

    private void noModelWrite(String beginTimeStr, String endTimeStr, List<List<String>> colListVPP, List<List<String>> colListEH, List<List<String>> colListDES) {

        String filePath = LOCAL_FILE_PATH_TOTAL;
        String fileName = "新奥总加数据补招" + beginTimeStr.substring(0, 13) + "~" + endTimeStr.substring(0, 13) + ".xlsx";
        FileUtil.mkParentDirs(filePath + fileName);
        ExcelWriter excelWriter = EasyExcel.write(filePath + fileName).build();

        WriteSheet writeSheet = EasyExcel.writerSheet(0, "工业负荷").build();
        excelWriter.write(colListVPP, writeSheet);

        writeSheet = EasyExcel.writerSheet(1, "电采暖").build();
        excelWriter.write(colListEH, writeSheet);

        writeSheet = EasyExcel.writerSheet(2, "分布式储能").build();
        excelWriter.write(colListDES, writeSheet);

        excelWriter.finish();
    }


    private List<String> getHeader() {
        List<String> list = Lists.newArrayList();
        String head0 = "时间";
        String head1 = "数据类型";
        list.add(head0);
        list.add(head1);
        for (int i = 0; i < 60; i++) {
            String data = String.valueOf(i);
            list.add(data);
        }
        return list;
    }
}
