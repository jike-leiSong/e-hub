package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.resp.BigDataHistoryResp;
import cn.enn.bigdata.service.IBigDataHandlerService;
import cn.sl.ehub.common.enums.EnergyModelEnumNew;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.enums.MetricEnum;
import cn.sl.ehub.service.mapper.ClearIssueLogMapper;
import cn.sl.ehub.service.resp.AggregatorDeviceDateProfitResp;
import cn.sl.ehub.service.resp.AggregatorEntDateAdjustResp;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.utils.GZIPUtil;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.*;
import cn.sl.ehub.service.service.AggregatorSingleModelDataService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * 数据处理ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataServiceV2Impl implements IDataV2Service {

    @Resource
    private IAggregatorInfoService aggregatorInfoService;
    @Resource
    private IAggregatorResourceTypeService aggregatorResourceTypeService;
    @Resource
    private IAggregatorEntDateApplyDetailService aggregatorEntDateApplyDetailService;
    @Resource
    private IAggregatorDateApplyDetailService aggregatorDateApplyDetailService;
    @Resource
    private IAggregatorResourceDateIssueOfferService aggregatorResourceDateIssueOfferService;
    @Resource
    private IAggregatorDateIssueChartService aggregatorDateIssueChartService;
    @Resource
    IAggregatorBaseLineLoadChartService aggregatorBaseLineLoadChartService;
    @Resource
    IAggregatorAvgRtChartService aggregatorAvgRtChartService;
    @Resource
    IAggregatorCrChartService aggregatorCrChartService;
    @Resource
    IAggregatorDapChartService aggregatorDapChartService;
    @Resource
    IAggregatorVppBasePowerChartService aggregatorVppBasePowerChartService;
    @Resource
    private IAggregatorDeviceDateDeliveryChartService aggregatorDeviceDateDeliveryChartService;
    @Resource
    private IAggregatorDeviceDateIssueChartService aggregatorDeviceDateIssueChartService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    @Resource
    private IAggregatorDeviceDateBaseLineLoadChartService aggregatorDeviceDateBaseLineLoadChartService;
    @Resource
    private IAggregatorEntBaseLineLoadChartService aggregatorEntBaseLineLoadChartService;
    @Resource
    private IAggregatorEntDapChartService aggregatorEntDapChartService;
    @Resource
    private IAggregatorDeviceDateProfitService aggregatorDeviceDateProfitService;
    @Resource
    private IAggregatorEntDateAdjustService aggregatorEntDateAdjustService;
    private final IAggregatorEntService aggregatorEntService;
    @Resource
    private IAggregatorEntProfitTimeService aggregatorEntProfitTimeService;
    @Resource
    private IAggregatorEntDateProfitService aggregatorEntDateProfitService;
    @Resource
    private IAggregatorDateProfitService aggregatorDateProfitService;
    @Resource
    private IBigDataHandlerService bigDataService;
    @Resource
    private ClearIssueLogMapper clearIssueLogMapper;
    @Resource
    private ISmsService pushService;
    @Resource(name = "threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Value("${device.no.up.data}")
    private String noUpDeviceStationIds;
    @Value("${model.no.up.data}")
    private String noUpModelEnergyStationCode;

    @Value("${filteredStationCodeList:}")
    private List<String> stationCodeList;
    /*= Lists.newArrayList(
            "6538c84cb5551b4e285dfe14","6538c84cb5551b4e285dfe15","656d888c370233a377800e3b","6538b498b555972dd0207cbd","656d888c370233a377800e3a",
            "6538c84cb5551b4e285dfe17","656d888c370233a377800e3e","6538c84cb5551b4e285dfe18","656d888c370233a377800e3f","656d888c370233a377800e40",
            "6538d416b5551b4e285dfe4f","656d8c47370233a377800e4d","6538d416b5551b4e285dfe51","6538ce00b5551b4e285dfe40","6538ce00b5551b4e285dfe42",
            "6538ce00b5551b4e285dfe44","656d8c47370233a377800e4e","656ec71db55544356a13d7a7","656ec71db55544356a13d7a8","656ec71db55544356a13d7a9");*/

//    private List<String> stationCodeList = Lists.newArrayList(
//            "6538c84cb5551b4e285dfe14","6538c84cb5551b4e285dfe15","656d888c370233a377800e3b","6538b498b555972dd0207cbd","656d888c370233a377800e3a",
//            "6538c84cb5551b4e285dfe17","656d888c370233a377800e3e","6538c84cb5551b4e285dfe18","656d888c370233a377800e3f","656d888c370233a377800e40",
//            "6538d416b5551b4e285dfe4f","656d8c47370233a377800e4d","6538d416b5551b4e285dfe51","6538ce00b5551b4e285dfe40","6538ce00b5551b4e285dfe42",
//            "6538ce00b5551b4e285dfe44","656d8c47370233a377800e4e","656ec71db55544356a13d7a7","656ec71db55544356a13d7a8","656ec71db55544356a13d7a9",
//            "6538c84cb5551b4e285dfe12","6538ce00b5551b4e285dfe3b","6538ce00b5551b4e285dfe3a","6538ce00b5551b4e285dfe3c","656d8c47370233a377800e45",
//            "656d8c47370233a377800e46","656d8c47370233a377800e47","656d8c47370233a377800e48","6538c84cb5551b4e285dfe16","656d888c370233a377800e3c",
//            "656d888c370233a377800e3c","656d888c370233a377800e3d","6538ce00b5551b4e285dfe34","656d888c370233a377800e41","6538ce00b5551b4e285dfe35",
//            "656d888c370233a377800e42","6538ce00b5551b4e285dfe36","656d888c370233a377800e43","6538ce00b5551b4e285dfe3d","656d8c47370233a377800e49",
//            "656d8c47370233a377800e4a","656d8c47370233a377800e4b","656d8c47370233a377800e4c","6538b498b555972dd0207cb6","6538b498b555972dd0207cb3",
//            "6538b498b555972dd0207cae","6538b498b555972dd0207cb2","6538b498b555972dd0207cb5","6538b498b555972dd0207caf","6538b498b555972dd0207cb9");

    @Resource
    private AggregatorSingleModelDataService aggregatorSingleModelDataService;


    @Override
    public void dealData(String issue) {
        executor.execute(() -> dealDataDetail(issue));
    }

    @Override
    public void dealDataDetail(String issue) {
        log.info("电网出清数据拆分" + issue);
        if (StringUtils.isEmpty(issue)) {
            return;
        }
        List<AggregatorResourceType> aggregatorShowResourceTypeList = aggregatorResourceTypeService.getAggregatorShowResourceTypeList();
        AggregatorInfo aggregatorInfo = new AggregatorInfo();
        String resourTypeName = "";
        for (AggregatorResourceType aggregatorResourceType : aggregatorShowResourceTypeList) {
            String key = "CP-" + aggregatorResourceType.getId();
            if (StrUtil.contains(issue, key)) {
                String aggregatorId = aggregatorResourceType.getAggregatorId();
                resourTypeName = aggregatorResourceType.getName();
                aggregatorInfo = aggregatorInfoService.getAggregatorInfo(aggregatorId);
            }
        }
        if (Objects.isNull(aggregatorInfo.getAggregatorId())) {
            return;
        }
        AtomicReference<String> date = new AtomicReference<>();
        AtomicReference<String> resourceType = new AtomicReference<>();
        JSONObject issueJson = JSONObject.parseObject(issue);
        List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorDisplayResourceTypeList(aggregatorInfo.getAggregatorId());
        List<Map<String, String>> dataList = (List<Map<String, String>>) issueJson.get("data");
        aggregatorResourceTypeList.forEach(aggregatorResourceType -> {
            //处理时间
            for (Map<String, String> data : dataList) {
                String offerAndTime = data.get("CP-" + aggregatorResourceType.getId() + "-1");
                if (StringUtils.isNotEmpty(offerAndTime)) {
                    String[] offerTimes = offerAndTime.split(":");
                    date.set(DateUtils.format(DateUtils.stampToDate(offerTimes[1]), "yyyy-MM-dd"));
                    resourceType.set(aggregatorResourceType.getId());
                }
            }
        });
        Map<String, String> energyTypeMap = EnergyModelEnumNew.getEnergyMap();
        if (StringUtils.isNotEmpty(resourceType.get())) {
            if (StrUtil.equals(energyTypeMap.get(resourTypeName), EnergyModelEnumNew.ELECTRIC_HEATING.getCode())) {
                dealEleHeatData(aggregatorInfo.getAggregatorId(), resourceType.get(), dataList, date);
            }
            if (StrUtil.equals(energyTypeMap.get(resourTypeName), EnergyModelEnumNew.CHARGING_PILE.getCode())) {
                dealCpData(aggregatorInfo.getAggregatorId(), resourceType.get(), dataList, date);
            }
            if (StrUtil.equals(energyTypeMap.get(resourTypeName), EnergyModelEnumNew.INDUSTRIAL_LOAD.getCode())) {
                dealVppData(aggregatorInfo.getAggregatorId(), resourceType.get(), dataList, date);
            }
        }
        log.info("数据处理结束");

//        if (StringUtils.isNotEmpty(resourceType.get())) {
//            //查询设备信息
//            List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceList(aggregatorInfo.getAggregatorId(), null, null, resourceType.get());
//            //处理时间
//            for (Map<String, String> data : dataList) {
//                String offerAndTime = data.get("CP-" + resourceType.get() + "-1");
//                if (StringUtils.isNotEmpty(offerAndTime)) {
//                    String[] offerTimes = offerAndTime.split(":");
//                    date.set(DateUtils.format(DateUtils.stampToDate(offerTimes[1]), "yyyy-MM-dd"));
//                }
//            }
//            if (StringUtils.isNotEmpty(date.get())) {
//                //保存中标状态
//                saveWinStatus(dataList, aggregatorInfo.getAggregatorId(), resourceType.get());
//                //保存下发价格
//                Map<String, Double> offerMap = saveAggregatorResourceDateIssueOfferList(dataList, aggregatorInfo.getAggregatorId(), resourceType.get());
//                //处理时间
//                List<String> minuteList = DateUtils.getMinuteList(date + " 00:00:00", date + " 23:59:59");
//                //保存日前计划
//                saveAggregatorDateIssueChartList(dataList, aggregatorInfo.getAggregatorId(), resourceType.get());
//                if (CollectionUtils.isNotEmpty(deviceList)) {
//                    //查询历史数据
//                    List<BigDataHistoryResp> bigDataHistoryRespList = bigDataService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), date + " 00:00:00", date + " 23:59:59", "0");
//                    //保存基线负荷
//                    Map<String, Map<String, Double>> baseLineMap = saveIssueBaseLineChartList(dataList, aggregatorInfo.getAggregatorId(), resourceType.get(), deviceList, bigDataHistoryRespList, date.get());
//                    //计算调节量收益
//                    List<AggregatorDeviceDateProfit> deviceProfitList = saveDeviceProfit(dataList, aggregatorInfo.getAggregatorId(), resourceType.get(), date.get(), deviceList, offerMap, baseLineMap, bigDataHistoryRespList);
//                }
//            }
//        }
//        if (StringUtils.isNotEmpty(date.get())) {
//            List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList = aggregatorDeviceDateProfitService.getAggregatorDeviceDateProfitList(aggregatorInfo.getAggregatorId(), date.get());
//            if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitList)) {
//                dealAggregatorAndEntProfit(aggregatorInfo.getAggregatorId(), date.get(), aggregatorDeviceDateProfitList);
//            }
//        }
//        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggregatorInfo.getAggregatorId());
//        //推送刷新
//        try {
//            //企业用户推送
//            List<String> codeListByApp = AggregatorRefreshEnum.getCodeByType("app");
//            codeListByApp.forEach(code -> {
//                aggregatorEntList.forEach(ent -> {
//                    SendMessageReq req = new SendMessageReq();
//                    req.setContent(code);
//                    req.setEntId(ent.getEntId());
//                    log.info("企业用户出清下发推送消息,{}", JSONObject.toJSONString(req));
//                    pushService.sendSocket(req);
//                });
//            });
//        } catch (Exception e) {
//            log.info("企业用户出清下发推送消息失败");
//        }
//        try {
//            //聚合商推送
//            List<String> codeListByPc = AggregatorRefreshEnum.getCodeByType("pc");
//            codeListByPc.forEach(code -> {
//                SendMessageReq req = new SendMessageReq();
//                req.setContent(code);
//                req.setEntId(aggregatorInfo.getAggregatorId());
//                log.info("聚合商出清下发推送消息,{}", JSONObject.toJSONString(req));
//                pushService.sendSocket(req);
//            });
//        } catch (Exception e) {
//            log.info("聚合商出清下发推送消息失败");
//        }
    }

    public void dealEleHeatData(String aggregatorId, String resourceTypeId, List<Map<String, String>> dataList, AtomicReference<String> date) {
        //查询设备信息
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceListModel(aggregatorId, null, null, resourceTypeId);

        //添加根据申报的单体进行过滤
        List<String> noUpModelEnergyStationCodes = Arrays.asList(noUpModelEnergyStationCode.split(","));
        List<AggregatorSingleModelData> modelInfoList = aggregatorSingleModelDataService.getByAggregatorAndResoureId(aggregatorId, resourceTypeId, noUpModelEnergyStationCodes);
        List<String> energyStationCodes = modelInfoList.stream().filter(o -> "1".equals(o.getControll()) ).map(AggregatorSingleModelData::getEnergyStationCode).collect(Collectors.toList());
        if(CollectionUtil.isNotEmpty(stationCodeList)){
            energyStationCodes = stationCodeList;
        }
        if (CollectionUtil.isEmpty(energyStationCodes)) {
            log.info(aggregatorId + resourceTypeId + "上送单体数量为空");
            return;
        }
        List<String> finalEnergyStationCodes = energyStationCodes;
        deviceList = deviceList.stream().filter(e -> CollectionUtil.contains(finalEnergyStationCodes, e.getEnergyStationCode())).collect(toList());
        if (CollectionUtil.isEmpty(deviceList)) {
            log.info(aggregatorId + resourceTypeId + "上送申报数量为空");
            return;
        }

        //处理时间
        for (Map<String, String> data : dataList) {
            String offerAndTime = data.get("CP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                String[] offerTimes = offerAndTime.split(":");
                date.set(DateUtils.format(DateUtils.stampToDate(offerTimes[1]), "yyyy-MM-dd"));
            }
        }
        if (StringUtils.isNotEmpty(date.get())) {
            //保存下发价格
            Map<String, Double> offerMap = null;
            try {
                offerMap = saveAggregatorResourceDateIssueOfferList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "下发价格未存库");
            }
            //保存聚合商基线
            try {
                saveAggregatorBaseLineChartList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "基线未存库");
            }
            //保存下发总聚合商火电平均负荷率数据
            try {
                saveAggregatorAVGRTChartList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "火电平均负荷率数据未存库");
            }
            //保存聚合商cr碳因子曲线 saveAggregatorCrChartList
            try {
                saveAggregatorCrChartList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "Cr数据未存库");
            }
            //保存调度功率dap
            try {
                saveEntDapLineChartList(aggregatorId, dataList, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "调度功率数据未存库");
            }
            // 保存聚合商dap曲线
            try {
                saveAggregatorDapLineChartList(aggregatorId, dataList, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "dap数据未存库");
            }

            if (CollectionUtils.isNotEmpty(deviceList)) {
                //查询历史数据
                List<BigDataHistoryResp> bigDataHistoryRespList = bigDataService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), date + " 00:00:00", date + " 23:59:59", "0");
                //保存用户基线负荷
                Map<String, Map<String, Double>> uesrBaseLineMap = saveBaseLineChartList(aggregatorId, dataList, bigDataHistoryRespList, resourceTypeId, EnergyModelEnumNew.ELECTRIC_HEATING.getCode(), date.get(), deviceList);
                //用户收益及调节量处理
                // todo check
                List<AggregatorEntDateAdjust> aggregatorEntDateAdjusts = saveUserProfit(dataList, aggregatorId, resourceTypeId, date.get(), offerMap, uesrBaseLineMap, bigDataHistoryRespList, deviceList);

                // 用户收益及聚合商收益处理
                dealAggregatorAndEntProfitNew(aggregatorId, date.get(), aggregatorEntDateAdjusts);
            }
        }
/*        if (StringUtils.isNotEmpty(date.get())) {
            List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList = aggregatorEntDateAdjustService.getAggregatorEntDateAdjustList(aggregatorId, date.get());
            if (CollectionUtils.isNotEmpty(aggregatorEntDateAdjustList)) {
                //用户收益及聚合商收益处理
                // todo check
                dealAggregatorAndEntProfitNew(aggregatorId,date.get(),aggregatorEntDateAdjustList);
            }
        }*/
    }

    public void dealCpData(String aggregatorId, String resourceTypeId, List<Map<String, String>> dataList, AtomicReference<String> date) {
        //查询设备信息
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceListModel(aggregatorId, null, null, resourceTypeId);
        //处理时间
        for (Map<String, String> data : dataList) {
            String offerAndTime = data.get("CP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                String[] offerTimes = offerAndTime.split(":");
                date.set(DateUtils.format(DateUtils.stampToDate(offerTimes[1]), "yyyy-MM-dd"));
            }
        }
        if (StringUtils.isNotEmpty(date.get())) {
            //保存下发价格
            Map<String, Double> offerMap = null;
            try {
                offerMap = saveAggregatorResourceDateIssueOfferList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "下发价格未存库");
            }
            //保存聚合商基线
            try {
                saveAggregatorBaseLineChartList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "基线未存库");
            }
            //保存下发总聚合商火电平均负荷率数据
            try {
                saveAggregatorAVGRTChartList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "火电平均负荷率未存库");
            }
            //保存调度功率dap
            try {
                saveEntDapLineChartList(aggregatorId, dataList, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "调度功率数据未存库");
            }
            // 保存聚合商dap曲线
            try {
                saveAggregatorDapLineChartList(aggregatorId, dataList, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "dap数据未存库");
            }

            //用户基线处理
            if (CollectionUtils.isNotEmpty(deviceList)) {
                //查询7:15-12:00历史数据
                List<BigDataHistoryResp> bigDataHistoryRespListOne = bigDataService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), date + " 07:15:00", date + " 12:00:00", "0");
                //查询16:15-24:00历史数据
                List<BigDataHistoryResp> bigDataHistoryRespListTwo = bigDataService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), date + " 16:15:00", date + " 24:00:00", "0");
                List<BigDataHistoryResp> mergedList = new ArrayList<>();
                mergedList.addAll(bigDataHistoryRespListOne);
                mergedList.addAll(bigDataHistoryRespListTwo);
                // 遍历合并后的List，根据姓名和年龄属性进行合并
                List<BigDataHistoryResp> resultList = new ArrayList<>();
                for (BigDataHistoryResp bigDataHistoryResp : mergedList) {
                    boolean found = false;
                    for (BigDataHistoryResp resultBigDataHistoryResp : resultList) {
                        if (StrUtil.equals(bigDataHistoryResp.getStaId(), resultBigDataHistoryResp.getStaId()) && StrUtil.equals(bigDataHistoryResp.getEquipMK(), resultBigDataHistoryResp.getEquipMK())
                                && StrUtil.equals(bigDataHistoryResp.getEquipID(), resultBigDataHistoryResp.getEquipID()) && StrUtil.equals(bigDataHistoryResp.getMetric(), resultBigDataHistoryResp.getMetric())) {
                            // 合并兴趣爱好List
                            List<DataResp> data = new ArrayList<>(resultBigDataHistoryResp.getDataResp());
                            data.addAll(bigDataHistoryResp.getDataResp());
                            resultBigDataHistoryResp.setDataResp(data);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        resultList.add(bigDataHistoryResp);
                    }
                }
                //保存基线负荷
                Map<String, Map<String, Double>> uesrBaseLineMap = saveBaseLineChartList(aggregatorId, dataList, resultList, resourceTypeId, EnergyModelEnumNew.CHARGING_PILE.getCode(), date.get(), deviceList);

                //查询历史数据
                List<BigDataHistoryResp> allDayBigDataHistoryRespList = bigDataService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), date + " 00:00:00", date + " 23:59:59", "0");
                // 用户收益及调节量处理
                List<AggregatorEntDateAdjust> aggregatorEntDateAdjusts = saveUserProfit(dataList, aggregatorId, resourceTypeId, date.get(), offerMap, uesrBaseLineMap, allDayBigDataHistoryRespList, deviceList);
                // 用户收益及聚合商收益处理
                dealAggregatorAndEntProfitNew(aggregatorId, date.get(), aggregatorEntDateAdjusts);
            }
        }
/*        if (StringUtils.isNotEmpty(date.get())) {
            List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList = aggregatorEntDateAdjustService.getAggregatorEntDateAdjustList(aggregatorId, date.get());
            if (CollectionUtils.isNotEmpty(aggregatorEntDateAdjustList)) {
                //用户收益及聚合商收益处理
                dealAggregatorAndEntProfitNew(aggregatorId,date.get(),aggregatorEntDateAdjustList);
            }
        }*/
    }

    /**
     * 工业负荷下发数据处理
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param dataList
     * @param date
     */
    public void dealVppData(String aggregatorId, String resourceTypeId, List<Map<String, String>> dataList, AtomicReference<String> date) {
        //查询设备信息
        List<AggregatorEntDevice> deviceList = aggregatorEntDeviceService.getAggregatorEntDeviceListModel(aggregatorId, null, null, resourceTypeId);
        //处理时间
        for (Map<String, String> data : dataList) {
            String offerAndTime = data.get("CP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                String[] offerTimes = offerAndTime.split(":");
                date.set(DateUtils.format(DateUtils.stampToDate(offerTimes[1]), "yyyy-MM-dd"));
            }
        }
        if (StringUtils.isNotEmpty(date.get())) {
            //保存下发价格
            Map<String, Double> offerMap = null;
            try {
                offerMap = saveAggregatorResourceDateIssueOfferList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "下发价格未存库");
            }
            //保存聚合商基线
            try {
                saveAggregatorBaseLineChartList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "基线未存库");
            }
            //保存下发总聚合商火电平均负荷率数据
            try {
                saveAggregatorAVGRTChartList(dataList, aggregatorId, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "电平均负荷率数据未存库");
            }
            //保存调度功率dap
            try {
                saveEntDapLineChartList(aggregatorId, dataList, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "调度功率数据未存库");
            }
            // 保存聚合商dap曲线
            try {
                saveAggregatorDapLineChartList(aggregatorId, dataList, resourceTypeId);
            } catch (Exception e) {
                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "dap数据未存库");
            }

            if (CollectionUtils.isNotEmpty(deviceList)) {
                //查询历史数据
                List<BigDataHistoryResp> bigDataHistoryRespList = bigDataService.getBigData(deviceList, Arrays.asList(MetricEnum.YES_POWER.getCode()), date + " 00:00:00", date + " 23:59:59", "0");
                //保存用户基线负荷
                Map<String, Map<String, Double>> uesrBaseLineMap = saveVppBaseLineChartList(aggregatorId, dataList, bigDataHistoryRespList, resourceTypeId, EnergyModelEnumNew.INDUSTRIAL_LOAD.getCode(), date.get());
                //用户收益及调节量处理
                List<AggregatorEntDateAdjust> aggregatorEntDateAdjusts = saveUserProfit(dataList, aggregatorId, resourceTypeId, date.get(), offerMap, uesrBaseLineMap, bigDataHistoryRespList, deviceList);
                // 用户收益及聚合商收益处理
                dealAggregatorAndEntProfitNew(aggregatorId, date.get(), aggregatorEntDateAdjusts);

            }
        }
/*        if (StringUtils.isNotEmpty(date.get())) {
            List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList = aggregatorEntDateAdjustService.getAggregatorEntDateAdjustList(aggregatorId, date.get());
            if (CollectionUtils.isNotEmpty(aggregatorEntDateAdjustList)) {
                //用户收益及聚合商收益处理
                dealAggregatorAndEntProfitNew(aggregatorId,date.get(),aggregatorEntDateAdjustList);
            }
        }*/
    }


/*    @Override
    public void dealClear(String startDate, String endDate) {
        Weekend<ClearIssueLog> weekendProfit = Weekend.of(ClearIssueLog.class);
        WeekendCriteria<ClearIssueLog, Object> criteriaProfit = weekendProfit.weekendCriteria();
        criteriaProfit.andGreaterThanOrEqualTo(ClearIssueLog::getClearDate, startDate);
        criteriaProfit.andLessThanOrEqualTo(ClearIssueLog::getClearDate, endDate);
        List<ClearIssueLog> clearIssueLogList = clearIssueLogMapper.selectByExample(weekendProfit);
        List<Integer> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(clearIssueLogList)) {
            clearIssueLogList.forEach(clearIssueLog -> {
                ids.add(clearIssueLog.getId());
                dealDataDetail(clearIssueLog.getCmdData());
            });
        }
        log.info("ids:{}",ids.toString());
    }*/

    @Override
    public void dealClear(String startDate, String endDate) {
        List<ClearIssueLog> clearIssueLogList = clearIssueLogMapper.getLastIssueDate(startDate,endDate);
        if (CollectionUtils.isNotEmpty(clearIssueLogList)) {
            clearIssueLogList.forEach(clearIssueLog -> dealDataDetail(clearIssueLog.getCmdData()));
        }
    }

    /**
     *     private Map<String, Double> profitMap(List<Map<String, String>> dataList, String resourceTypeId) {
     *         Map<String, Double> profitMap = new HashMap<>();
     *         dataList.forEach(data -> {
     *             String profitAndTime = data.get("FEE-" + resourceTypeId + "-1");
     *             if (StringUtils.isNotEmpty(profitAndTime)) {
     *                 data.values().forEach(profitTime -> {
     *                     String[] profitAndTimes = profitTime.split(":");
     *                     profitMap.put(DateUtils.stampToDate(profitAndTimes[1]), MathUtils.stringToDouble(profitAndTimes[0]));
     *                 });
     *             }
     *         });
     *         return profitMap;
     *     }
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public double gridClear(String startDate, String endDate) {
        List<ClearIssueLog> clearIssueLogList = clearIssueLogMapper.getLastIssueDate(startDate,endDate);
        List<Double> profitList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(clearIssueLogList)) {
            clearIssueLogList.forEach(clearIssueLog -> {
                JSONObject issueJson = JSONObject.parseObject(clearIssueLog.getCmdData());
                List<Map<String, String>> dataList = (List<Map<String, String>>) issueJson.get("data");
                dataList.forEach(dataMap -> {
                    dataMap.forEach((key, value) -> {
                        if (key.startsWith("FEE")) {
                            String[] profitAndTimes = value.split(":");
                            double profit = MathUtils.stringToDouble(profitAndTimes[0]);
                            profitList.add(profit);
                        }
                    });
                });
            });
        }

        double sum = profitList.stream().mapToDouble(Double::doubleValue).sum();
        return sum;
    }

    /**
     * 保存企业和聚合商申报中标状态
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    private String saveWinStatus(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        AtomicReference<String> resultDate = new AtomicReference<>();
        dataList.forEach(data -> {
            String timeValue = data.get("IFCE-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(timeValue)) {
                Boolean flag = false;
                String now = DateUtils.getTime();
                String[] timeValues = timeValue.split(":");
                String date = DateUtils.format(DateUtils.stampToDate(timeValues[1]), "yyyy-MM-dd");
                if (MathUtils.stringToDouble(timeValues[0]).compareTo(1D) == 0) {
                    flag = true;
                }
                AggregatorEntDateApplyDetail aggregatorEntDateApplyDetail = new AggregatorEntDateApplyDetail();
                aggregatorEntDateApplyDetail.setWinStatus(flag);
                aggregatorEntDateApplyDetailService.update(aggregatorEntDateApplyDetail, aggregatorId, Arrays.asList(date));
                AggregatorDateApplyDetail aggregatorDateApplyDetail = new AggregatorDateApplyDetail();
                aggregatorDateApplyDetail.setWinStatus(flag ? "1" : "0");
                aggregatorDateApplyDetail.setWinTime(now);
                aggregatorDateApplyDetail.setUpdateTime(now);
                aggregatorDateApplyDetailService.updateAggregatorDateApplyDetail(aggregatorDateApplyDetail, aggregatorId, date);
                resultDate.set(date);
            }
        });
        return resultDate.get();
    }

    /**
     * 保存下发价格
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     * @return
     */
    private Map<String, Double> saveAggregatorResourceDateIssueOfferList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        Map<String, Double> offerMap = new HashMap<>();
        String date = DateUtils.getLastDay();
        for (Map<String, String> data : dataList) {
            List<AggregatorResourceDateIssueOffer> aggregatorResourceDateIssueOfferList = Lists.newArrayList();
            String offerAndTime = data.get("CP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                List<DataResp> dataRespList = Lists.newArrayList();
                data.values().forEach(offerTime -> {
                    String[] offerTimes = offerTime.split(":");
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.stampToDate(offerTimes[1]));
                    dataResp.setValue(MathUtils.mulDoubleNull(MathUtils.stringToDouble(offerTimes[0]), 0.001, 8));
                    dataRespList.add(dataResp);
                });
                String priceChart = JSONObject.toJSONString(dataRespList);
                Double offer = 0D;
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    List<DataResp> dataRespListSort = dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                    priceChart = JSONObject.toJSONString(dataRespListSort);
                    date = DateUtils.format(dataRespListSort.get(0).getTime(), "yyyy-MM-dd");
                    offer = dataRespListSort.get(0).getValue();
                    Map<String, Double> timeValueMap = dataRespList.stream().filter(resp -> null != resp && StringUtils.isNotEmpty(resp.getTime())).collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                    if (timeValueMap.size() > 0) {
                        offerMap.putAll(timeValueMap);
                    }
                }
                AggregatorResourceDateIssueOffer aggregatorResourceDateIssueOffer = new AggregatorResourceDateIssueOffer();
                aggregatorResourceDateIssueOffer.setAggregatorId(aggregatorId);
                aggregatorResourceDateIssueOffer.setResourceTypeId(resourceTypeId);
                aggregatorResourceDateIssueOffer.setPriceChart(priceChart);
                aggregatorResourceDateIssueOffer.setDate(date);
                aggregatorResourceDateIssueOffer.setOffer(offer);
                aggregatorResourceDateIssueOfferList.add(aggregatorResourceDateIssueOffer);
            }
            if (CollectionUtils.isNotEmpty(aggregatorResourceDateIssueOfferList)) {
                aggregatorResourceDateIssueOfferService.deleteAggregatorResourceDateDeliveryOffer(aggregatorId, date, resourceTypeId);
                aggregatorResourceDateIssueOfferService.batchInsertAggregatorResourceDateDeliveryOffer(aggregatorResourceDateIssueOfferList);

                log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "下发价格存库成功");
            }

        }
        return offerMap;
    }

    /**
     * 保存总下发基线数据
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    private void saveAggregatorBaseLineChartList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        dataList.forEach(data -> {
            String date = DateUtils.getNextDay();
            List<AggregatorBaseLineLoadChart> aggregatorBaseLineLoadChartList = Lists.newArrayList();
            String timeAndValue = data.get("BLD-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(timeAndValue)) {
                List<DataResp> dataRespList = Lists.newArrayList();
                data.values().forEach(timeValue -> {
                    String[] timeValues = timeValue.split(":");
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.stampToDate(timeValues[1]));
                    //单位转换
                    dataResp.setValue(MathUtils.mulDoubleNull(MathUtils.stringToDouble(timeValues[0]), 1000D, 8));
                    dataRespList.add(dataResp);
                });
                String issueChart = JSONObject.toJSONString(dataRespList);
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    List<DataResp> dataRespListSort = dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                    issueChart = JSONObject.toJSONString(dataRespListSort);
                    date = DateUtils.format(dataRespListSort.get(0).getTime(), "yyyy-MM-dd");
                }
                AggregatorBaseLineLoadChart aggregatorBaseLineLoadChart = new AggregatorBaseLineLoadChart();
                aggregatorBaseLineLoadChart.setAggregatorId(aggregatorId);
                aggregatorBaseLineLoadChart.setResourceType(resourceTypeId);
                aggregatorBaseLineLoadChart.setBaseDate(date);
                aggregatorBaseLineLoadChart.setBaseLineLoadChart(issueChart);
                aggregatorBaseLineLoadChartList.add(aggregatorBaseLineLoadChart);
                aggregatorBaseLineLoadChartService.delete(aggregatorId, date, resourceTypeId);
                if (CollectionUtils.isNotEmpty(aggregatorBaseLineLoadChartList)) {
                    aggregatorBaseLineLoadChartService.batchInsert(aggregatorBaseLineLoadChartList);
                    log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "基线存库成功");
                }
            }
        });
    }

    /**
     * 保存下发总聚合商火电平均负荷率数据
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    private void saveAggregatorAVGRTChartList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        dataList.forEach(data -> {
            String date = DateUtils.getNextDay();
            List<AggregatorAvgRtChart> aggregatorAvgRtChartList = Lists.newArrayList();
            String timeAndValue = data.get("AVGRT-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(timeAndValue)) {
                List<DataResp> dataRespList = Lists.newArrayList();
                data.values().forEach(timeValue -> {
                    String[] timeValues = timeValue.split(":");
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.stampToDate(timeValues[1]));
                    //单位转换
                    dataResp.setValue(MathUtils.mulDoubleNull(MathUtils.stringToDouble(timeValues[0]), 1D, 8));
                    dataRespList.add(dataResp);
                });
                String issueChart = JSONObject.toJSONString(dataRespList);
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    List<DataResp> dataRespListSort = dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                    issueChart = JSONObject.toJSONString(dataRespListSort);
                    date = DateUtils.format(dataRespListSort.get(0).getTime(), "yyyy-MM-dd");
                }
                AggregatorAvgRtChart aggregatorAvgRtChart = new AggregatorAvgRtChart();
                aggregatorAvgRtChart.setAggregatorId(aggregatorId);
                aggregatorAvgRtChart.setResourceType(resourceTypeId);
                aggregatorAvgRtChart.setDate(date);
                aggregatorAvgRtChart.setAvgRtChart(issueChart);
                aggregatorAvgRtChartList.add(aggregatorAvgRtChart);
                aggregatorAvgRtChartService.delete(aggregatorId, date, resourceTypeId);
                if (CollectionUtils.isNotEmpty(aggregatorAvgRtChartList)) {
                    aggregatorAvgRtChartService.batchInsert(aggregatorAvgRtChartList);
                    log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "火电平均负荷率数据存库成功");
                }
            }
        });
    }

    /**
     * 保存下发总聚合商cr数据
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    private void saveAggregatorCrChartList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        dataList.forEach(data -> {
            String date = DateUtils.getNextDay();
            List<AggregatorCrChart> aggregatorCrChartList = Lists.newArrayList();
            String timeAndValue = data.get("CR-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(timeAndValue)) {
                List<DataResp> dataRespList = Lists.newArrayList();
                data.values().forEach(timeValue -> {
                    String[] timeValues = timeValue.split(":");
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.stampToDate(timeValues[1]));
                    //单位转换
                    dataResp.setValue(MathUtils.mulDoubleNull(MathUtils.stringToDouble(timeValues[0]), 1D, 8));
                    dataRespList.add(dataResp);
                });
                String issueChart = JSONObject.toJSONString(dataRespList);
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    List<DataResp> dataRespListSort = dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                    issueChart = JSONObject.toJSONString(dataRespListSort);
                    date = DateUtils.format(dataRespListSort.get(0).getTime(), "yyyy-MM-dd");
                }
                AggregatorCrChart aggregatorCrChart = new AggregatorCrChart();
                aggregatorCrChart.setAggregatorId(aggregatorId);
                aggregatorCrChart.setResourceType(resourceTypeId);
                aggregatorCrChart.setCrDate(date);
                aggregatorCrChart.setCrLoadChart(issueChart);
                aggregatorCrChartList.add(aggregatorCrChart);
                aggregatorCrChartService.delete(aggregatorId, date, resourceTypeId);
                if (CollectionUtils.isNotEmpty(aggregatorCrChartList)) {
                    aggregatorCrChartService.batchInsert(aggregatorCrChartList);
                    log.info("聚合商" + aggregatorId + "资源id" + resourceTypeId + "Cr数据存库成功");
                }
            }
        });
    }

    /**
     * 保存总下发申报数据
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    private void saveAggregatorDateIssueChartList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId) {
        dataList.forEach(data -> {
            String date = DateUtils.getNextDay();
            List<AggregatorDateIssueChart> aggregatorDateIssueChartList = Lists.newArrayList();
            String timeAndValue = data.get("DAP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(timeAndValue)) {
                List<DataResp> dataRespList = Lists.newArrayList();
                data.values().forEach(timeValue -> {
                    String[] timeValues = timeValue.split(":");
                    DataResp dataResp = new DataResp();
                    dataResp.setTime(DateUtils.stampToDate(timeValues[1]));
                    //兆瓦数据转换千瓦
                    dataResp.setValue(MathUtils.mulDoubleNull(MathUtils.stringToDouble(timeValues[0]), 1000D, 8));
                    dataRespList.add(dataResp);
                });
                String issueChart = JSONObject.toJSONString(dataRespList);
                if (CollectionUtils.isNotEmpty(dataRespList)) {
                    List<DataResp> dataRespListSort = dataRespList.stream().sorted(Comparator.comparing(DataResp::getTime)).collect(Collectors.toList());
                    issueChart = JSONObject.toJSONString(dataRespListSort);
                    date = DateUtils.format(dataRespListSort.get(0).getTime(), "yyyy-MM-dd");
                }
                AggregatorDateIssueChart aggregatorDateIssueChart = new AggregatorDateIssueChart();
                aggregatorDateIssueChart.setAggregatorId(aggregatorId);
                aggregatorDateIssueChart.setResourceTypeId(resourceTypeId);
                aggregatorDateIssueChart.setDate(date);
                aggregatorDateIssueChart.setIssueChart(issueChart);
                aggregatorDateIssueChartList.add(aggregatorDateIssueChart);
                aggregatorDateIssueChartService.delete(aggregatorId, date, resourceTypeId);
                if (CollectionUtils.isNotEmpty(aggregatorDateIssueChartList)) {
                    aggregatorDateIssueChartService.batchInsert(aggregatorDateIssueChartList);
                    //写入设备下发功率
                    saveAggregatorDeviceDateIssueChartList(date, resourceTypeId, aggregatorDateIssueChartList);
                }
            }
        });
    }

    /**
     * 保存设备下发申报数据
     *
     * @param date
     * @param resourceTypeId
     * @param aggregatorDateIssueChartList
     */
    private void saveAggregatorDeviceDateIssueChartList(String date, String resourceTypeId, List<AggregatorDateIssueChart> aggregatorDateIssueChartList) {
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = Lists.newArrayList();
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartListByResourceTypeId(resourceTypeId, date);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateDeliveryChartList)) {
            aggregatorDeviceDateDeliveryChartList.forEach(aggregatorDeviceDateDeliveryChart -> {
                AggregatorDeviceDateIssueChart aggregatorDeviceDateIssueChart = new AggregatorDeviceDateIssueChart();
                BeanUtils.copyProperties(aggregatorDeviceDateDeliveryChart, aggregatorDeviceDateIssueChart);
                aggregatorDeviceDateIssueChart.setIssueChart(aggregatorDeviceDateDeliveryChart.getDeliveryChart());
                aggregatorDeviceDateIssueChartList.add(aggregatorDeviceDateIssueChart);
            });
        }
        aggregatorDeviceDateIssueChartService.deleteByResourceTypeId(resourceTypeId, date);
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateIssueChartList)) {
            aggregatorDeviceDateIssueChartService.batchInsert(aggregatorDeviceDateIssueChartList);
        }
    }


    /**
     * 保存聚合商级别的dap曲线
     *
     * @param aggreatorId
     * @param dataList
     * @param resourceTypeId
     */
    private void saveAggregatorDapLineChartList(String aggreatorId, List<Map<String, String>> dataList, String resourceTypeId) {
        log.info("聚合商:" + aggreatorId + "资源id:" + resourceTypeId + "保存日前计划start");
        //聚合商日前计划map
        Map<String, Double> aggregatorDapMap = dealDapChartMap(dataList, resourceTypeId);
        //处理时间
        AtomicReference<String> date = new AtomicReference<>();
        for (Map<String, String> data : dataList) {
            String offerAndTime = data.get("DAP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                String[] offerTimes = offerAndTime.split(":");
                date.set(DateUtils.format(DateUtils.stampToDate(offerTimes[1]), "yyyy-MM-dd"));
            }
        }
        List<String> minuteList = DateUtils.getMinuteList(date.get() + " 00:15:00", DateUtils.getAddDate(date.get(), 1) + " 00:00:00", 15);
        List<AggregatorDapChart> aggregatorDapLoadChartList = new ArrayList<>();

        AggregatorDapChart aggregatorDapChart = new AggregatorDapChart();
        aggregatorDapChart.setAggregatorId(aggreatorId);
        aggregatorDapChart.setResourceType(resourceTypeId);
        aggregatorDapChart.setDate(DateUtils.parse(date.get(), "yyyy-MM-dd"));
        List<DataResp> dapChartList = Lists.newArrayList();
        minuteList.forEach(minute -> {
            Double baseLine = MathUtils.mulDoubleNull(aggregatorDapMap.get(DateUtils.format(minute, "HH:mm")), 1d, 8);
            dapChartList.add(new DataResp(minute, baseLine));
        });
        aggregatorDapChart.setDapChart(JSONObject.toJSONString(dapChartList));
        aggregatorDapLoadChartList.add(aggregatorDapChart);

        if (CollectionUtils.isNotEmpty(aggregatorDapLoadChartList)) {
            aggregatorDapChartService.delete(aggreatorId, date.get(), resourceTypeId);
            aggregatorDapChartService.batchInsert(aggregatorDapLoadChartList);
        }
    }


    private void saveEntDapLineChartList(String aggreatorId, List<Map<String, String>> dataList, String resourceTypeId) {
        log.info("聚合商:" + aggreatorId + "资源id:" + resourceTypeId + "保存日前计划start");
        List<AggregatorEnt> aggregatorPlanRunEntList = aggregatorEntService.getAggregatorPlanRunEntList(aggreatorId);
        if (CollectionUtils.isEmpty(aggregatorPlanRunEntList)) {
            return;
        }
        List<AggregatorEntDevice> aggregatorEntDeviceListModel = aggregatorEntDeviceService.getAggregatorEntDeviceListModel(aggreatorId, null, null, resourceTypeId);
        if (CollectionUtils.isEmpty(aggregatorEntDeviceListModel)) {
            return;
        }
        //存在该资源类型的企业id
        List<String> resourceTypeEntIdList = aggregatorEntDeviceListModel.stream().map(e -> e.getEntId()).distinct().collect(toList());
        //存在该资源类型的企业信息
        List<AggregatorEnt> aggregatorPlanRunResEntList = aggregatorPlanRunEntList.stream().filter(e -> resourceTypeEntIdList.contains(e.getEntId())).collect(toList());
        if (CollectionUtils.isEmpty(aggregatorPlanRunResEntList)) {
            return;
        }
        Double sumInstallCap = aggregatorPlanRunResEntList.stream().mapToDouble(AggregatorEnt::getInstallCap).sum();
        Map<String, Double> entInstallMap = aggregatorPlanRunResEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getInstallCap, (v1, v2) -> v1));
        //聚合商日前计划map
        Map<String, Double> aggregatorDapMap = dealDapChartMap(dataList, resourceTypeId);
        //处理时间
        AtomicReference<String> date = new AtomicReference<>();
        for (Map<String, String> data : dataList) {
            String offerAndTime = data.get("DAP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(offerAndTime)) {
                String[] offerTimes = offerAndTime.split(":");
                log.info("offerAndTime:" + offerAndTime);
                date.set(DateUtils.format(DateUtils.stampToDate(offerTimes[1]), "yyyy-MM-dd"));
            }
        }
        log.info("date:" + date.get());
        List<String> minuteList = DateUtils.getMinuteList(date.get() + " 00:15:00", DateUtils.getAddDate(date.get(), 1) + " 00:00:00", 15);
        List<AggregatorEntDapChart> aggregatorEntDapLoadChartList = new ArrayList<>();
        for (AggregatorEnt aggregatorEnt : aggregatorPlanRunResEntList) {
            AggregatorEntDapChart aggregatorEntDapChart = new AggregatorEntDapChart();
            aggregatorEntDapChart.setAggregatorId(aggregatorEnt.getAggregatorId());
            aggregatorEntDapChart.setEntId(aggregatorEnt.getEntId());
            aggregatorEntDapChart.setStationId(aggregatorEnt.getStationId());
            aggregatorEntDapChart.setResourceType(resourceTypeId);
            aggregatorEntDapChart.setDapDate(date.get());
            List<DataResp> dapChartList = Lists.newArrayList();
            Double percent = MathUtils.divideNull(entInstallMap.get(aggregatorEnt.getEntId()), sumInstallCap, 8);
            minuteList.forEach(minute -> {
                Double baseLine = MathUtils.mulDoubleNull(aggregatorDapMap.get(DateUtils.format(minute, "HH:mm")), percent, 8);
                dapChartList.add(new DataResp(minute, baseLine));
            });
            aggregatorEntDapChart.setDapChart(JSONObject.toJSONString(dapChartList));
            aggregatorEntDapLoadChartList.add(aggregatorEntDapChart);
        }
        if (CollectionUtils.isNotEmpty(aggregatorEntDapLoadChartList)) {
            List<String> updateEntIdList = aggregatorPlanRunResEntList.stream().map(AggregatorEnt::getEntId).collect(toList());
            aggregatorEntDapChartService.batchDelete(updateEntIdList, date.get());
            aggregatorEntDapChartService.batchInsert(aggregatorEntDapLoadChartList);
        }
    }


    /**
     * 保存用户及聚合商基线
     *
     * @param aggreatorId
     * @param dataList
     * @param bigDataHistoryRespList
     * @param resourceTypeId
     * @param resourceTypeCode
     * @param date
     */
    private Map<String, Map<String, Double>> saveBaseLineChartList(String aggreatorId, List<Map<String, String>> dataList, List<BigDataHistoryResp> bigDataHistoryRespList, String resourceTypeId, String resourceTypeCode, String date, List<AggregatorEntDevice> aggregatorDeviceList) {
        log.info("聚合商:" + aggreatorId + "资源id:" + resourceTypeId + "保存用户基线start");
        Map<String, Map<String, Double>> resultMap = new HashMap<>();
        AtomicReference<Double> totalPower = new AtomicReference<>();
        Map<String, Double> devicePowerMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(bigDataHistoryRespList)) {
            //计算总实时功率、设备总实时功率
            bigDataHistoryRespList.stream().filter(bigDataHistoryResp -> null != bigDataHistoryResp && CollectionUtils.isNotEmpty(bigDataHistoryResp.getDataResp())).forEach(bigDataHistoryResp -> {
                Double devicePower = bigDataHistoryResp.getDataResp().stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).mapToDouble(DataResp::getValue).sum();
                totalPower.set(MathUtils.addDouble(totalPower.get(), devicePower, 8));
                devicePowerMap.put(bigDataHistoryResp.getEquipMK() + "_" + bigDataHistoryResp.getEquipID() + "," + bigDataHistoryResp.getStaId(), devicePower);
            });
        }
        log.info("聚合商:" + aggreatorId + "资源id:" + resourceTypeId + "聚合商总功率:" + totalPower);
        //根据聚合商查询下属企业信息
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggreatorId);
        if (CollectionUtils.isEmpty(aggregatorEntList)) {
            return resultMap;
        }
        //2024-0103
        //List<AggregatorEntDevice> aggregatorDeviceList = aggregatorEntDeviceService.getDeviceList(aggreatorId, resourceTypeId);

        List<String> aggregatorTypeEntIdList = aggregatorDeviceList.stream().map(e -> e.getEntId()).distinct().collect(toList());
        Map<String, Double> entPowerMap = new HashMap<>();
        for (String entId : aggregatorTypeEntIdList) {
            Double entResult = 0.0;
            List<AggregatorEntDevice> entDeviceList = aggregatorDeviceList.stream().filter(e -> StrUtil.equals(e.getEntId(), entId)).collect(toList());
            //计算用户总实时功率、设备总实时功率
            for (AggregatorEntDevice aggregatorEntDevice : entDeviceList) {
                Double aDouble = devicePowerMap.get(aggregatorEntDevice.getDeviceId() + "," + aggregatorEntDevice.getStationId());
                if (!Objects.isNull(aDouble)) {
                    entResult = entResult + aDouble;
                }
            }
            entPowerMap.put(entId, entResult);
        }
        log.info("聚合商:" + aggreatorId + "资源id:" + resourceTypeId + "用户总功率集合:" + entPowerMap);
        //处理总基线
        Map<String, Double> aggregatorBaseLineMap = dealBaseLineChartMap(dataList, resourceTypeId, resourceTypeCode, null);
        //处理时间
        List<String> minuteList = DateUtils.getMinuteList(date + " 00:15:00", DateUtils.getAddDate(date, 1) + " 00:00:00", 15);
        List<AggregatorEntBaseLineLoadChart> aggregatorEntBaseLineLoadChartList = new ArrayList<>();
        List<AggregatorEnt> aggregatorTypeEntList = aggregatorEntList.stream().filter(e -> aggregatorTypeEntIdList.contains(e.getEntId())).collect(toList());
        for (AggregatorEnt aggregatorEnt : aggregatorTypeEntList) {
            AggregatorEntBaseLineLoadChart aggregatorEntBaseLineLoadChart = new AggregatorEntBaseLineLoadChart();
            aggregatorEntBaseLineLoadChart.setAggregatorId(aggregatorEnt.getAggregatorId());
            aggregatorEntBaseLineLoadChart.setEntId(aggregatorEnt.getEntId());
            aggregatorEntBaseLineLoadChart.setStationId(aggregatorEnt.getStationId());
            aggregatorEntBaseLineLoadChart.setResourceType(resourceTypeId);
            aggregatorEntBaseLineLoadChart.setBaseDate(date);
            List<DataResp> baseLineChartList = Lists.newArrayList();
            // todo yanglei 临时测试用
//            Double percent = 0.25D;
            Double percent = MathUtils.divideNull(entPowerMap.get(aggregatorEnt.getEntId()), totalPower.get(), 8);
            minuteList.forEach(minute -> {
                Double baseLine = MathUtils.mulDoubleNull(aggregatorBaseLineMap.get(DateUtils.format(minute, "HH:mm")), percent, 8);
                baseLineChartList.add(new DataResp(minute, baseLine));
            });
            aggregatorEntBaseLineLoadChart.setBaseLineLoadChart(JSONObject.toJSONString(baseLineChartList));
            aggregatorEntBaseLineLoadChartList.add(aggregatorEntBaseLineLoadChart);
            Map<String, Double> baseLineChartMap = baseLineChartList.stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).collect(toMap(dateResp -> DateUtils.format(dateResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
            resultMap.put(aggregatorEnt.getEntId(), baseLineChartMap);
        }
        if (CollectionUtils.isNotEmpty(aggregatorEntBaseLineLoadChartList)) {
            List<String> updateEntIdList = aggregatorEntBaseLineLoadChartList.stream().map(AggregatorEntBaseLineLoadChart::getEntId).collect(toList());
            aggregatorEntBaseLineLoadChartService.batchDeleteByTypeId(aggreatorId, resourceTypeId, date);
            aggregatorEntBaseLineLoadChartService.batchInsert(aggregatorEntBaseLineLoadChartList);
        }
        return resultMap;
    }


    /**
     * 保存工业负荷用户及聚合商基线
     *
     * @param aggreatorId
     * @param dataList
     * @param bigDataHistoryRespList
     * @param resourceTypeId
     * @param resourceTypeCode
     * @param date
     */
    private Map<String, Map<String, Double>> saveVppBaseLineChartList(String aggreatorId, List<Map<String, String>> dataList, List<BigDataHistoryResp> bigDataHistoryRespList, String resourceTypeId, String resourceTypeCode, String date) {
        log.info("聚合商:" + aggreatorId + "资源id:" + resourceTypeId + "保存用户基线start");
        Map<String, Map<String, Double>> resultMap = new HashMap<>();
        AtomicReference<Double> totalPower = new AtomicReference<>();
        Map<String, Double> devicePowerMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(bigDataHistoryRespList)) {
            //计算设备总实时功率
            bigDataHistoryRespList.stream().filter(bigDataHistoryResp -> null != bigDataHistoryResp && CollectionUtils.isNotEmpty(bigDataHistoryResp.getDataResp())).forEach(bigDataHistoryResp -> {
                Double devicePower = bigDataHistoryResp.getDataResp().stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).mapToDouble(DataResp::getValue).sum();
//                totalPower.set(MathUtils.addDouble(totalPower.get(), devicePower, 8));
                devicePowerMap.put(bigDataHistoryResp.getEquipMK() + "_" + bigDataHistoryResp.getEquipID() + "," + bigDataHistoryResp.getStaId(), devicePower);
            });
        }
        //根据聚合商查询下属企业信息
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggreatorId);
        if (CollectionUtils.isEmpty(aggregatorEntList)) {
            return resultMap;
        }
        List<AggregatorEntDevice> aggregatorDeviceList = aggregatorEntDeviceService.getDeviceList(aggreatorId, resourceTypeId);
        List<String> aggregatorTypeEntIdList = aggregatorDeviceList.stream().map(e -> e.getEntId()).distinct().collect(toList());
        Map<String, Double> entPowerMap = new HashMap<>();
        for (String entId : aggregatorTypeEntIdList) {
            Double entResult = 0.0;
            List<AggregatorEntDevice> entDeviceList = aggregatorDeviceList.stream().filter(e -> StrUtil.equals(e.getEntId(), entId)).collect(toList());
            //计算用户总实时功率、设备总实时功率
            for (AggregatorEntDevice aggregatorEntDevice : entDeviceList) {
                Double aDouble = devicePowerMap.get(aggregatorEntDevice.getDeviceId() + "," + aggregatorEntDevice.getStationId());
                if (!Objects.isNull(aDouble)) {
                    entResult = entResult + aDouble;
                }
            }
            entPowerMap.put(entId, entResult);
        }
        log.info("聚合商:" + aggreatorId + "资源id:" + resourceTypeId + "用户总功率:" + entPowerMap);
        //处理时间
        List<String> minuteList = DateUtils.getMinuteList(date + " 00:15:00", DateUtils.getAddDate(date, 1) + " 00:00:00", 15);
        List<AggregatorEntBaseLineLoadChart> aggregatorEntBaseLineLoadChartList = new ArrayList<>();
        List<AggregatorEnt> aggregatorTypeEntList = aggregatorEntList.stream().filter(e -> aggregatorTypeEntIdList.contains(e.getEntId())).collect(toList());
        for (AggregatorEnt aggregatorEnt : aggregatorTypeEntList) {
            AggregatorEntBaseLineLoadChart aggregatorEntBaseLineLoadChart = new AggregatorEntBaseLineLoadChart();
            aggregatorEntBaseLineLoadChart.setAggregatorId(aggregatorEnt.getAggregatorId());
            aggregatorEntBaseLineLoadChart.setEntId(aggregatorEnt.getEntId());
            aggregatorEntBaseLineLoadChart.setStationId(aggregatorEnt.getStationId());
            aggregatorEntBaseLineLoadChart.setResourceType(resourceTypeId);
            aggregatorEntBaseLineLoadChart.setBaseDate(date);
            //获取用户基线
            List<AggregatorVppBasePowerChart> vppBasePowerList = aggregatorVppBasePowerChartService.getVppBasePowerBySystemCode(aggregatorEnt.getStationId());
            if (CollectionUtils.isEmpty(vppBasePowerList)) {
                continue;
            }
            AggregatorVppBasePowerChart aggregatorVppBasePowerChart = vppBasePowerList.get(0);
            totalPower.set(Double.valueOf(aggregatorVppBasePowerChart.getTotalPower()));
            //处理得到工业负荷用户基准曲线Map
            Map<String, Double> entBaseLineMap = dealVppBaseLineChartMap(resourceTypeId, aggregatorEnt.getEntId(), vppBasePowerList, minuteList);
            List<DataResp> baseLineChartList = Lists.newArrayList();
            Double percent = MathUtils.divideNull(entPowerMap.get(aggregatorEnt.getEntId()), totalPower.get(), 8);
            minuteList.forEach(minute -> {
                Double baseLine = MathUtils.mulDoubleNull(entBaseLineMap.get(DateUtils.format(minute, "HH:mm")), percent, 8);
                baseLineChartList.add(new DataResp(minute, baseLine));
            });
            aggregatorEntBaseLineLoadChart.setBaseLineLoadChart(JSONObject.toJSONString(baseLineChartList));
            aggregatorEntBaseLineLoadChartList.add(aggregatorEntBaseLineLoadChart);
            Map<String, Double> baseLineChartMap = baseLineChartList.stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).collect(toMap(dateResp -> DateUtils.format(dateResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
            resultMap.put(aggregatorEnt.getEntId(), baseLineChartMap);
        }
        if (CollectionUtils.isNotEmpty(aggregatorEntBaseLineLoadChartList)) {
            List<String> updateEntIdList = aggregatorEntBaseLineLoadChartList.stream().map(AggregatorEntBaseLineLoadChart::getEntId).collect(toList());
            aggregatorEntBaseLineLoadChartService.batchDelete(updateEntIdList, date);
            aggregatorEntBaseLineLoadChartService.batchInsert(aggregatorEntBaseLineLoadChartList);
        }
        return resultMap;
    }

    /**
     * 保存设备基线负荷
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     */
    private Map<String, Map<String, Double>> saveIssueBaseLineChartList(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId, List<AggregatorEntDevice> deviceList, List<BigDataHistoryResp> bigDataHistoryRespList, String date) {
        Map<String, Map<String, Double>> resultMap = new HashMap<>();
        AtomicReference<Double> totalPower = new AtomicReference<>();
        Map<String, Double> devicePowerMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(bigDataHistoryRespList)) {
            //计算总实时功率、设备总实时功率
            bigDataHistoryRespList.stream().filter(bigDataHistoryResp -> null != bigDataHistoryResp && CollectionUtils.isNotEmpty(bigDataHistoryResp.getDataResp())).forEach(bigDataHistoryResp -> {
                Double devicePower = bigDataHistoryResp.getDataResp().stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).mapToDouble(DataResp::getValue).sum();
                totalPower.set(MathUtils.addDouble(totalPower.get(), devicePower, 8));
                devicePowerMap.put(bigDataHistoryResp.getEquipMK() + "_" + bigDataHistoryResp.getEquipID() + "," + bigDataHistoryResp.getStaId(), devicePower);
            });
        }
        //处理时间
        List<String> minuteList = DateUtils.getMinuteList(date + " 00:15:00", DateUtils.getAddDate(date, 1) + " 00:00:00", 15);
        //总基线
        Map<String, Double> totalBaseMap = dealBaseLineChartMap(dataList, resourceTypeId, null, deviceList);
        //计算基线占比
        List<AggregatorDeviceDateBaseLineLoadChart> aggregatorDeviceDateBaseLineLoadChartList = Lists.newArrayList();
        String finalDate = date;
        deviceList.forEach(device -> {
            AggregatorDeviceDateBaseLineLoadChart aggregatorDeviceDateBaseLineLoadChart = new AggregatorDeviceDateBaseLineLoadChart();
            aggregatorDeviceDateBaseLineLoadChart.setAggregatorId(device.getAggregatorId());
            aggregatorDeviceDateBaseLineLoadChart.setEntId(device.getEntId());
            aggregatorDeviceDateBaseLineLoadChart.setStationId(device.getStationId());
            aggregatorDeviceDateBaseLineLoadChart.setDeviceBaseId(device.getDeviceBaseId());
            Double percent = MathUtils.divideNull(devicePowerMap.get(device.getDeviceId() + "," + device.getStationId()), totalPower.get(), 8);
            List<DataResp> baseLineChartList = Lists.newArrayList();
            minuteList.forEach(minute -> {
                Double baseLine = MathUtils.mulDoubleNull(totalBaseMap.get(DateUtils.format(minute, "HH:mm")), percent, 8);
                baseLineChartList.add(new DataResp(minute, baseLine));
            });
            aggregatorDeviceDateBaseLineLoadChart.setBaseLineLoadChart(JSONObject.toJSONString(baseLineChartList));
            aggregatorDeviceDateBaseLineLoadChart.setStartDate(finalDate);
            aggregatorDeviceDateBaseLineLoadChart.setEndDate(finalDate);
            aggregatorDeviceDateBaseLineLoadChartList.add(aggregatorDeviceDateBaseLineLoadChart);
            Map<String, Double> baseLineChartMap = baseLineChartList.stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).collect(toMap(dateResp -> DateUtils.format(dateResp.getTime(), "HH:mm"), DataResp::getValue, (v1, v2) -> v1));
            resultMap.put(device.getDeviceBaseId(), baseLineChartMap);
        });
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateBaseLineLoadChartList)) {
            List<String> updateDeviceIdList = aggregatorDeviceDateBaseLineLoadChartList.stream().map(AggregatorDeviceDateBaseLineLoadChart::getDeviceBaseId).collect(toList());
            aggregatorDeviceDateBaseLineLoadChartService.batchDelete(updateDeviceIdList, finalDate);
            aggregatorDeviceDateBaseLineLoadChartService.batchInsert(aggregatorDeviceDateBaseLineLoadChartList);
        }
        return resultMap;
    }

    /**
     * 从dataList获取聚合商出清map
     *
     * @param dataList
     * @param resourceTypeId
     * @return
     */
    private Map<String, Double> dealDapChartMap(List<Map<String, String>> dataList, String resourceTypeId) {
        Map<String, Double> totalDapMap = new HashMap<>();
        for (Map<String, String> data : dataList) {
            String timeAndValue = data.get("DAP-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(timeAndValue)) {
                for (String timeValue : data.values()) {
                    String[] timeValues = timeValue.split(":");
                    totalDapMap.put(DateUtils.format(DateUtils.stampToDate(timeValues[1]), "HH:mm"), MathUtils.mulDoubleNull(MathUtils.stringToDouble(timeValues[0]), 1000D, 8));
                }
            }
        }
        return totalDapMap;
    }

    private Map<String, Double> dealBaseLineChartMap(List<Map<String, String>> dataList, String resourceTypeId, String resourceTypeCode, List<AggregatorEntDevice> deviceList) {
        Map<String, Double> totalBaseMap = new HashMap<>();
        switch (resourceTypeCode) {
            case "EH":
                for (Map<String, String> data : dataList) {
                    String timeAndValue = data.get("BLD-" + resourceTypeId + "-1");
                    if (StringUtils.isNotEmpty(timeAndValue)) {
                        for (String timeValue : data.values()) {
                            String[] timeValues = timeValue.split(":");
                            totalBaseMap.put(DateUtils.format(DateUtils.stampToDate(timeValues[1]), "HH:mm"), MathUtils.mulDoubleNull(MathUtils.stringToDouble(timeValues[0]), 1000D, 8));
                        }
                    }
                }
                break;
            case "CP":
                for (Map<String, String> data : dataList) {
                    String timeAndValue = data.get("BLD-" + resourceTypeId + "-1");
                    if (StringUtils.isNotEmpty(timeAndValue)) {
                        for (String timeValue : data.values()) {
                            String[] timeValues = timeValue.split(":");
                            totalBaseMap.put(DateUtils.format(DateUtils.stampToDate(timeValues[1]), "HH:mm"), MathUtils.mulDoubleNull(MathUtils.stringToDouble(timeValues[0]), 1000D, 8));
                        }
                    }
                }
                break;
        }
        return totalBaseMap;
    }

    private Map<String, Double> dealVppBaseLineChartMap(String resourceTypeId, String entId, List<AggregatorVppBasePowerChart> vppBasePowerList, List<String> minuteList) {
        Map<String, Double> totalBaseMap = new HashMap<>();

        if (CollectionUtils.isEmpty(vppBasePowerList)) {
            return totalBaseMap;
        }
        AggregatorVppBasePowerChart aggregatorVppBasePowerChart = vppBasePowerList.get(0);

        String vppBasePowerChart = aggregatorVppBasePowerChart.getVppBasePowerChart();
        Map<String, String> entBldDataMap = JSON.parseObject(vppBasePowerChart, new TypeReference<Map<String, String>>() {
        });
        Integer num = 0;
        for (int i = 0; i < minuteList.size(); i++) {
            num = i + 1;
            String timeValue = entBldDataMap.getOrDefault("BLD-" + resourceTypeId + "-" + num, "0.0");
            totalBaseMap.put(DateUtils.format(minuteList.get(i), "HH:mm"), MathUtils.mulDoubleNull(MathUtils.stringToDouble(timeValue), 1D, 8));

        }
        return totalBaseMap;
    }

    /**
     * todo check
     * 处理用户收益数据
     *
     * @param dataList
     * @param aggregatorId
     * @param resourceTypeId
     * @param date
     * @param
     * @param offerMap
     * @param baseLinePowerMap
     * @param bigDataHistoryRespList
     * @return
     */
    private List<AggregatorEntDateAdjust> saveUserProfit(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId, String date, Map<String, Double> offerMap, Map<String, Map<String, Double>> baseLinePowerMap, List<BigDataHistoryResp> bigDataHistoryRespList, List<AggregatorEntDevice> aggregatorDeviceList) {
        String startTime = date + " 00:00:00";
        String endTime = DateUtils.getAddDate(date, 1) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        List<String> minuteListWith15 = DateUtils.getMinuteList(startTime, endTime, 15);

        //获取所有上报设备信息,改为外部统一传入
//        List<AggregatorEntDevice> aggregatorDeviceList = aggregatorEntDeviceService.getDeviceList(aggregatorId, resourceTypeId);

        //处理总收益
        Map<String, Double> profitMap = profitMap(dataList, resourceTypeId);

        //查询实时功率
        Map<String, Map<String, Double>> realTimeAvgPowerMap = getRealTimeAvgPower(aggregatorDeviceList, bigDataHistoryRespList, minuteList);
        List<String> entIdList = aggregatorDeviceList.stream().map(AggregatorEntDevice::getEntId).distinct().collect(toList());
        //根据设备功率获取用户实时功率
        Map<String, Map<String, Double>> userAvgPowerMap = dealPowerAndDeliveryMap(aggregatorId, resourceTypeId, realTimeAvgPowerMap, minuteListWith15);
        // 处理用户调节量
        // 电网出清收益先按比例分配到签约用户，签约用户再按分层比例与聚合商进行清算
        List<AggregatorEntDateAdjust> aggregatorEntDateAdjusts = dealUserProfitList(aggregatorDeviceList, date, minuteListWith15, userAvgPowerMap, baseLinePowerMap);
//        //处理设备调节量
//        List<AggregatorDeviceDateProfit> deviceProfitList = dealDeviceProfitList(aggregatorDeviceList, date, minuteListWith15, deliveryPowerMap, realTimeAvgPowerMap, issuePowerMap, baseLinePowerMap);
        //计算总调节量
        if (CollectionUtils.isNotEmpty(aggregatorEntDateAdjusts)) {
            //每个时间段总功率
            List<AggregatorEntDateAdjustResp> aggregatorEntDateAdjustRespLists = Lists.newArrayList();
            aggregatorEntDateAdjusts.forEach(AggregatorEntDateAdjust -> {
                if (StringUtils.isNotEmpty(AggregatorEntDateAdjust.getProfitDetail())) {
                    List<AggregatorEntDateAdjustResp> aggregatorEntDateAdjustRespList = JSONArray.parseArray(AggregatorEntDateAdjust.getProfitDetail(), AggregatorEntDateAdjustResp.class);
                    if (null != aggregatorEntDateAdjustRespList && aggregatorEntDateAdjustRespList.size() > 0) {
                        aggregatorEntDateAdjustRespLists.addAll(aggregatorEntDateAdjustRespList);
                    }
                }
            });
            // 聚合商每个时间的计算负荷
/*            Map<String, Double> totalChangeValue = aggregatorEntDateAdjustRespLists.stream().collect(toMap(AggregatorEntDateAdjustResp::getEndTime, AggregatorEntDateAdjustResp::getCountPower, (v1, v2) -> v1 + v2));
            //写入用户为分成前的收益
            aggregatorEntDateAdjustRespLists.forEach(entDateAdjustResp -> {
                Double totalPower = totalChangeValue.get(entDateAdjustResp.getEndTime());
                Double totalProfit = profitMap.get(entDateAdjustResp.getEndTime());
//                Double powerPercent = MathUtils.divideNullNotRounding(entDateAdjustResp.getCountPower(), totalPower, 8);
                BigDecimal powerPercent = BigDecimal.ZERO;
                //处理总调节量为0的情况
                if(Objects.isNull(totalPower)||totalPower==0.0){
                    powerPercent = BigDecimal.ONE.divide(BigDecimal.valueOf(CollectionUtil.size(aggregatorEntDateAdjusts)), 8, BigDecimal.ROUND_HALF_UP);
                }else{
                    powerPercent = BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(totalPower),8,BigDecimal.ROUND_HALF_UP);
                }


                entDateAdjustResp.setPowerPercent(powerPercent.doubleValue());

//                Double profit = MathUtils.mulDoubleNullNotRounding(totalProfit, powerPercent, 8);

                BigDecimal profit = BigDecimal.valueOf(totalProfit).multiply(powerPercent).setScale(8, BigDecimal.ROUND_HALF_UP);


//                log.info(entDateAdjustResp.getEndTime()+"  totalProfit: "+totalProfit+"  powerPercent:"+powerPercent+"  profit:"+profit);
                entDateAdjustResp.setProfit(profit.doubleValue());
                Double countPrice = offerMap.get(entDateAdjustResp.getEndTime());
                entDateAdjustResp.setCountPrice(countPrice);
                Double electricQuantity = MathUtils.divideNullNotRounding(entDateAdjustResp.getProfit(), entDateAdjustResp.getCountPrice(), 8);
                entDateAdjustResp.setElectricQuantity(electricQuantity);
            });*/
            // 每15分钟的各企业调节量列表
            Map<String, List<AggregatorEntDateAdjustResp>> entAdjustOfQuarterMap = aggregatorEntDateAdjustRespLists.stream().collect(groupingBy(AggregatorEntDateAdjustResp::getEndTime));

            entAdjustOfQuarterMap.forEach((entTime, entDateAdjustRespList) -> {

                // 各时刻的聚合商总调节量和总收益
                double currentAggTotalPower = entDateAdjustRespList.stream().mapToDouble(AggregatorEntDateAdjustResp::getCountPower).sum();
                double currentAggTotalProfit = profitMap.get(entTime);

                double positiveEntAdjustPower = entDateAdjustRespList.stream().filter(x -> Double.compare(x.getCountPower(), 0.0) > 0).mapToDouble(AggregatorEntDateAdjustResp::getCountPower).sum();
                double negativeEntAdjustPower = entDateAdjustRespList.stream().filter(x -> Double.compare(x.getCountPower(), 0.0) < 0).mapToDouble(AggregatorEntDateAdjustResp::getCountPower).sum();

                entDateAdjustRespList.forEach(
                        entDateAdjustResp -> {
                            // 各时刻的企业调节信息
                            BigDecimal entPowerPercent = BigDecimal.ZERO;

                            /**
                             * case 01
                             * 聚合商各点收益为+
                             * 用户某点有效调节量为+
                             * 聚合商各点有效调节量为+
                             */
                            if (currentAggTotalProfit > 0.0 && Double.compare(negativeEntAdjustPower, 0.0) == 0 && currentAggTotalPower > 0.0) {
                                entPowerPercent = BigDecimal.valueOf(entDateAdjustResp.getCountPower())
                                        .divide(BigDecimal.valueOf(positiveEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                            }


                            /**
                             * case 10
                             * 聚合商各点收益为+
                             * 用户某点有效调节量为-
                             * 聚合商各点有效调节量为+
                             */
                            if (currentAggTotalProfit > 0.0 && Double.compare(positiveEntAdjustPower, 0.0) == 0 && currentAggTotalPower < 0.0) {
                                entPowerPercent = BigDecimal.valueOf(entDateAdjustResp.getCountPower())
                                        .divide(BigDecimal.valueOf(negativeEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                            }

                            /**
                             * case 02
                             * 聚合商各点收益为+
                             * 用户某点有效调节量为+-
                             * 聚合商各点有效调节量为+
                             */
                            if (currentAggTotalProfit > 0.0
                                    && Double.compare(positiveEntAdjustPower, 0.0) > 0
                                    && Double.compare(negativeEntAdjustPower, 0.0) < 0
                                    && currentAggTotalPower > 0.0) {
                                entPowerPercent = BigDecimal.valueOf(entDateAdjustResp.getCountPower())
                                        .divide(BigDecimal.valueOf(currentAggTotalPower), 8, BigDecimal.ROUND_HALF_UP);
                            }

                            /**
                             * case 03
                             * 聚合商各点收益为+
                             * 用户某点有效调节量为+-
                             * 聚合商各点有效调节量为-
                             */
                            if (currentAggTotalProfit > 0.0
                                    && Double.compare(positiveEntAdjustPower, 0.0) > 0
                                    && Double.compare(negativeEntAdjustPower, 0.0) < 0
                                    && currentAggTotalPower < 0.0) {
                                entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(), 0) <= 0
                                        ? BigDecimal.ZERO
                                        : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(positiveEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                            }

                            /**
                             * case 04
                             * 聚合商各点收益为+(包括0)
                             * 用户某点有效调节量为+-
                             * 聚合商各点有效调节量为0
                             */
                            if (currentAggTotalProfit >= 0.0
                                    && Double.compare(positiveEntAdjustPower, 0.0) > 0
                                    && Double.compare(negativeEntAdjustPower, 0.0) < 0
                                    && currentAggTotalPower == 0.0) {
                                entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(), 0) <= 0
                                        ? BigDecimal.ZERO
                                        : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(positiveEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                            }

                            /**
                             * case 05
                             * 聚合商各点收益为-
                             * 用户某点有效调节量为+-
                             * 聚合商各点有效调节量为-
                             */
                            if (currentAggTotalProfit < 0.0
                                    && Double.compare(positiveEntAdjustPower, 0.0) > 0
                                    && Double.compare(negativeEntAdjustPower, 0.0) < 0
                                    && currentAggTotalPower < 0.0) {
                                entPowerPercent = BigDecimal.valueOf(entDateAdjustResp.getCountPower())
                                        .divide(BigDecimal.valueOf(currentAggTotalPower), 8, BigDecimal.ROUND_HALF_UP);
                            }

                            /**
                             * case 06
                             * 聚合商各点收益为-
                             * 用户某点有效调节量为-
                             * 聚合商各点有效调节量为-
                             */
                            if (currentAggTotalProfit < 0.0
                                    && Double.compare(positiveEntAdjustPower, 0.0) == 0
                                    && Double.compare(negativeEntAdjustPower, 0.0) < 0
                                    && currentAggTotalPower < 0.0) {
                                entPowerPercent = BigDecimal.valueOf(entDateAdjustResp.getCountPower())
                                        .divide(BigDecimal.valueOf(currentAggTotalPower), 8, BigDecimal.ROUND_HALF_UP);
                            }

                            /**
                             * case 09
                             * 聚合商各点收益为-
                             * 用户某点有效调节量为+
                             * 聚合商各点有效调节量为+
                             */
                            if (currentAggTotalProfit < 0.0
                                    && Double.compare(positiveEntAdjustPower, 0.0) > 0
                                    && Double.compare(negativeEntAdjustPower, 0.0) == 0
                                    && currentAggTotalPower > 0.0) {
                                entPowerPercent = BigDecimal.valueOf(entDateAdjustResp.getCountPower())
                                        .divide(BigDecimal.valueOf(currentAggTotalPower), 8, BigDecimal.ROUND_HALF_UP);
                            }

                            /**
                             * case 07
                             * 聚合商各点收益为-
                             * 用户某点有效调节量为+-
                             * 聚合商各点有效调节量为+
                             */
                            if (currentAggTotalProfit < 0.0
                                    && Double.compare(positiveEntAdjustPower, 0.0) > 0
                                    && Double.compare(negativeEntAdjustPower, 0.0) < 0
                                    && currentAggTotalPower > 0.0) {
                                entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(), 0) >= 0
                                        ? BigDecimal.ZERO
                                        : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(negativeEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                            }


                            /**
                             * case 08
                             * 聚合商各点收益为-
                             * 用户某点有效调节量为+-
                             * 聚合商各点有效调节量为0
                             */
                            if (currentAggTotalProfit < 0.0
                                    && Double.compare(positiveEntAdjustPower, 0.0) > 0
                                    && Double.compare(negativeEntAdjustPower, 0.0) < 0
                                    && currentAggTotalPower == 0.0) {
                                entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(), 0) >= 0
                                        ? BigDecimal.ZERO
                                        : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(negativeEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                            }

                            BigDecimal profit = BigDecimal.valueOf(currentAggTotalProfit).multiply(entPowerPercent).setScale(8, BigDecimal.ROUND_HALF_UP);
                            entDateAdjustResp.setPowerPercent(entPowerPercent.doubleValue());
                            entDateAdjustResp.setProfit(profit.doubleValue());
                            Double countPrice = offerMap.get(entDateAdjustResp.getEndTime());
                            entDateAdjustResp.setCountPrice(countPrice);
                            Double electricQuantity = MathUtils.divideNullNotRounding(entDateAdjustResp.getProfit(), entDateAdjustResp.getCountPrice(), 8);
                            entDateAdjustResp.setElectricQuantity(electricQuantity);
                        }
                );


/*                entDateAdjustRespList.forEach(
                        entDateAdjustResp -> {
                            // 各时刻的企业调节信息
                            BigDecimal entPowerPercent = BigDecimal.ZERO;
                            // case 1 聚合商总调节量T=0
                            if(currentAggTotalPower == 0.0){
                                // case 1.1 : 聚合商总调节量T=0,聚合商总收益Q=0
                                if (currentAggTotalProfit == 0.0){
                                // case 1.2 : 聚合商总调节量T=0,聚合商总收益Q<0
                                }else if (currentAggTotalProfit < 0.0){
                                    entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(),0) >= 0
                                            ? BigDecimal.ZERO
                                            : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(negativeEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                                // case 1.3 : 聚合商总调节量T=0,聚合商总收益Q>0
                                }else if (currentAggTotalProfit > 0.0){
                                    entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(),0) <= 0
                                            ? BigDecimal.ZERO
                                            : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(positiveEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                                }
                            // case 2 聚合商总调节量T<0
                            }else if(currentAggTotalPower < 0){
                                // case 2.1 : 聚合商总调节量T<0,聚合商总收益Q=0
                                if (currentAggTotalProfit == 0.0){
                                    // case 1.2 : 聚合商总调节量T<0,聚合商总收益Q<0
                                }else if (currentAggTotalProfit < 0.0){
                                    entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(),0) == 0
                                            ? BigDecimal.ZERO
                                            : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(currentAggTotalPower), 8, BigDecimal.ROUND_HALF_UP);
                                    // case 1.3 : 聚合商总调节量T<0,聚合商总收益Q>0
                                }else if (currentAggTotalProfit > 0.0){
                                    entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(),0) <= 0
                                            ? BigDecimal.ZERO
                                            : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(positiveEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                                }
                            // case 3 聚合商总调节量T>0
                            }else if(currentAggTotalPower > 0){
                                // case 2.1 : 聚合商总调节量T>0,聚合商总收益Q=0
                                if (currentAggTotalProfit == 0.0){
                                    // case 1.2 : 聚合商总调节量T>0,聚合商总收益Q<0
                                }else if (currentAggTotalProfit < 0.0){
                                    entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(),0) >= 0
                                            ? BigDecimal.ZERO
                                            : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(negativeEntAdjustPower), 8, BigDecimal.ROUND_HALF_UP);
                                    // case 1.3 : 聚合商总调节量T>0,聚合商总收益Q>0
                                }else if (currentAggTotalProfit > 0.0){
                                    entPowerPercent = Double.compare(entDateAdjustResp.getCountPower(),0) < 0
                                            ? BigDecimal.ZERO
                                            : BigDecimal.valueOf(entDateAdjustResp.getCountPower()).divide(BigDecimal.valueOf(currentAggTotalPower), 8, BigDecimal.ROUND_HALF_UP);
                                }
                            }

                            BigDecimal profit = BigDecimal.valueOf(currentAggTotalProfit).multiply(entPowerPercent).setScale(8, BigDecimal.ROUND_HALF_UP);
                            entDateAdjustResp.setPowerPercent(entPowerPercent.doubleValue());
                            entDateAdjustResp.setProfit(profit.doubleValue());
                            Double countPrice = offerMap.get(entDateAdjustResp.getEndTime());
                            entDateAdjustResp.setCountPrice(countPrice);
                            Double electricQuantity = MathUtils.divideNullNotRounding(entDateAdjustResp.getProfit(), entDateAdjustResp.getCountPrice(), 8);
                            entDateAdjustResp.setElectricQuantity(electricQuantity);
                        }
                );*/
            });

            Map<String, List<AggregatorEntDateAdjustResp>> entIdMap = aggregatorEntDateAdjustRespLists.stream().collect(groupingBy(AggregatorEntDateAdjustResp::getEntId));
            aggregatorEntDateAdjusts.forEach(adjust -> {
                List<AggregatorEntDateAdjustResp> aggregatorEntDateAdjustRespList = entIdMap.get(adjust.getEntId());
                String adjustDetail = JSONObject.toJSONString(aggregatorEntDateAdjustRespList);
                adjust.setProfitDetail(adjustDetail);
                adjust.setProfitDetailByte(GZIPUtil.compressString(adjust.getProfitDetail()));
            });
            aggregatorEntDateAdjustService.save(aggregatorId, resourceTypeId, date, aggregatorEntDateAdjusts);
        }
        return aggregatorEntDateAdjusts;
    }

    private Map<String, Map<String, Double>> dealPowerAndDeliveryMap(String aggregatorId, String resourceTypeId, Map<String, Map<String, Double>> dataMap, List<String> minuteListWith15) {
        Map<String, Map<String, Double>> resultMap = new HashMap<>();
        //获取所有设备信息
        List<AggregatorEntDevice> aggregatorDeviceList = aggregatorEntDeviceService.getDeviceList(aggregatorId, resourceTypeId);

        if (CollectionUtils.isEmpty(aggregatorDeviceList)) {
            return resultMap;
        }
        List<String> entIdList = aggregatorDeviceList.stream().map(e -> e.getEntId()).distinct().collect(toList());
        for (String entId : entIdList) {
            List<AggregatorEntDevice> userDevices = aggregatorDeviceList.stream().filter(e -> StrUtil.equals(e.getEntId(), entId)).collect(toList());
            Map<String, Double> userMap = new HashMap<>();
            for (String minute : minuteListWith15) {
//                String format = DateUtils.format(minute, "HH:mm");
                Double minuteData = 0.0;
                for (AggregatorEntDevice userDevice : userDevices) {
                    String deviceBaseId = userDevice.getDeviceBaseId();
                    Map<String, Double> deviceData = dataMap.get(deviceBaseId);
                    if (MapUtil.isEmpty(deviceData)) {
                        continue;
                    }
                    Double aDouble = deviceData.get(minute);
                    if (!Objects.isNull(aDouble)) {
                        minuteData = minuteData + aDouble;
                    }
                }
                userMap.put(minute, minuteData);
            }
            resultMap.put(entId, userMap);
        }
        return resultMap;
    }

    private List<AggregatorDeviceDateProfit> saveDeviceProfit(List<Map<String, String>> dataList, String aggregatorId, String resourceTypeId, String date, List<AggregatorEntDevice> deviceList, Map<String, Double> offerMap, Map<String, Map<String, Double>> baseLinePowerMap, List<BigDataHistoryResp> bigDataHistoryRespList) {
        String startTime = date + " 00:00:00";
        String endTime = DateUtils.getAddDate(date, 1) + " 00:00:00";
        List<String> minuteList = DateUtils.getMinuteList(startTime, endTime);
        List<String> minuteListWith15 = DateUtils.getMinuteList(startTime, endTime, 15);
        //处理总收益
        Map<String, Double> profitMap = profitMap(dataList, resourceTypeId);
        //查询设备申报数据
        Map<String, Map<String, Double>> deliveryPowerMap = getDeliveryPowerMap(resourceTypeId, date);
        //查询设备下发数据
        Map<String, Map<String, Double>> issuePowerMap = getIssuePowerMap(resourceTypeId, date);
        //查询实时功率
        Map<String, Map<String, Double>> realTimeAvgPowerMap = getRealTimeAvgPower(deviceList, bigDataHistoryRespList, minuteList);
        List<String> deviceBaseIdList = deviceList.stream().map(AggregatorEntDevice::getDeviceBaseId).collect(toList());
        //处理设备调节量
        List<AggregatorDeviceDateProfit> deviceProfitList = dealDeviceProfitList(deviceList, date, minuteListWith15, deliveryPowerMap, realTimeAvgPowerMap, issuePowerMap, baseLinePowerMap);
        //计算总调节量
        if (CollectionUtils.isNotEmpty(deviceProfitList)) {
            //每个时间段总功率
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
            deviceProfitList.forEach(aggregatorDeviceDateProfit -> {
                if (StringUtils.isNotEmpty(aggregatorDeviceDateProfit.getProfitDetail())) {
                    List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitResps = JSONArray.parseArray(aggregatorDeviceDateProfit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
                    if (null != aggregatorDeviceDateProfitResps && aggregatorDeviceDateProfitResps.size() > 0) {
                        aggregatorDeviceDateProfitRespList.addAll(aggregatorDeviceDateProfitResps);
                    }
                }
            });
            Map<String, Double> totalChangeValue = aggregatorDeviceDateProfitRespList.stream().collect(toMap(AggregatorDeviceDateProfitResp::getEndTime, AggregatorDeviceDateProfitResp::getCountPower, (v1, v2) -> v1 + v2));
            //写入设备收益
            aggregatorDeviceDateProfitRespList.forEach(deviceProfit -> {
                Double totalPower = totalChangeValue.get(deviceProfit.getEndTime());
                Double totalProfit = profitMap.get(deviceProfit.getEndTime());
                Double powerPercent = MathUtils.divideNullNotRounding(deviceProfit.getCountPower(), totalPower, 8);
                deviceProfit.setPowerPercent(powerPercent);
                Double profit = MathUtils.mulDoubleNullNotRounding(totalProfit, powerPercent, 8);
                deviceProfit.setProfit(profit);
                Double countPrice = offerMap.get(deviceProfit.getEndTime());
                deviceProfit.setCountPrice(countPrice);
                Double electricQuantity = MathUtils.divideNullNotRounding(deviceProfit.getProfit(), deviceProfit.getCountPrice(), 8);
                deviceProfit.setElectricQuantity(electricQuantity);
            });
            Map<String, List<AggregatorDeviceDateProfitResp>> deviceMap = aggregatorDeviceDateProfitRespList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getDeviceBaseId));
            deviceProfitList.forEach(profit -> {
                List<AggregatorDeviceDateProfitResp> deviceDateProfitRespList = deviceMap.get(profit.getDeviceBaseId());
                String profitDetail = JSONObject.toJSONString(deviceDateProfitRespList);
                profit.setProfitDetail(profitDetail);
                profit.setProfitDetailByte(GZIPUtil.compressString(profit.getProfitDetail()));
            });
            aggregatorDeviceDateProfitService.save(deviceBaseIdList, date, deviceProfitList);
        }
        return deviceProfitList;
    }

    /**
     * 处理用户调节量
     *
     * @param deviceList
     * @param date
     * @param minuteListWith15
     * @param realTimeAvgPowerMap
     * @param baseLinePowerMap
     * @return
     */
    private List<AggregatorEntDateAdjust> dealUserProfitList(
            List<AggregatorEntDevice> deviceList,
            String date,
            List<String> minuteListWith15,
            Map<String, Map<String, Double>> realTimeAvgPowerMap,
            Map<String, Map<String, Double>> baseLinePowerMap) {
        List<AggregatorEntDateAdjust> adjustList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(deviceList)) {
            return adjustList;
        }
        //根据entId对设备进行去重，只保留一行entId信息
        List<AggregatorEntDevice> entInfoList = deviceList.stream()
                .collect(toMap(
                        AggregatorEntDevice::getEntId,  // 用entId作为key
                        e -> e,  // 保留原始对象
                        (existing, replacement) -> existing  // 处理重复键，保留已有的对象
                )).values().stream().collect(toList());
        entInfoList.forEach(entInfo -> {
            AggregatorEntDateAdjust adjust = new AggregatorEntDateAdjust();
            adjust.setAggregatorId(entInfo.getAggregatorId());
            adjust.setEntId(entInfo.getEntId());
            adjust.setResourceTypeId(entInfo.getResourceTypeId());
            adjust.setDate(date);
            List<AggregatorEntDateAdjustResp> aggregatorEntDateAdjustRespList = Lists.newArrayList();
            for (int i = 0; i < minuteListWith15.size() - 1; i++) {
                String minute = minuteListWith15.get(i);
                String minuteNext = minuteListWith15.get(i + 1);
                AggregatorEntDateAdjustResp aggregatorEntDateAdjustResp = new AggregatorEntDateAdjustResp();
                aggregatorEntDateAdjustResp.setAggregatorId(entInfo.getAggregatorId());
                aggregatorEntDateAdjustResp.setEntId(entInfo.getEntId());
                aggregatorEntDateAdjustResp.setResourceTypeId(entInfo.getResourceTypeId());
                aggregatorEntDateAdjustResp.setDate(date);
                aggregatorEntDateAdjustResp.setStartTime(minute);
                aggregatorEntDateAdjustResp.setEndTime(minuteNext);
                Map<String, Double> realTimeMap = realTimeAvgPowerMap.get(entInfo.getEntId());
                if (null != realTimeMap && realTimeMap.size() > 0) {
                    aggregatorEntDateAdjustResp.setReallyPower(realTimeMap.get(minuteNext));
                }
                aggregatorEntDateAdjustResp.setMinPower(MathUtils.compareReturnMinABS(aggregatorEntDateAdjustResp.getReallyPower(), aggregatorEntDateAdjustResp.getMinPower()));
                Map<String, Double> baseLineTimeMap = baseLinePowerMap.get(entInfo.getEntId());
                if (null != baseLineTimeMap && baseLineTimeMap.size() > 0) {
                    aggregatorEntDateAdjustResp.setBaseLinePower(baseLineTimeMap.get(DateUtils.format(minuteNext, "HH:mm")));
                }
                Double subDouble = null;
                subDouble = MathUtils.subDoubleABS(aggregatorEntDateAdjustResp.getMinPower(), aggregatorEntDateAdjustResp.getBaseLinePower());
//                if (null == subDouble || subDouble.compareTo(0D) < 0) {
//                    subDouble = 0D;
//                }
                // 上述三行原逻辑 是对计算负荷空值、赋值进行兜底处理，实际应保留正负，只对空值进行赋零处理
                // update by yangyangllei @ 2025/2/18
                if (null == subDouble) {
                    subDouble = 0D;
                }

                aggregatorEntDateAdjustResp.setCountPower(MathUtils.doublePoint(subDouble, 8));

                //1分钟功率 乘以 15 分钟 除以 60 分钟
                aggregatorEntDateAdjustResp.setCountElectricQuantity(MathUtils.mulDoubleNull(aggregatorEntDateAdjustResp.getCountPower(), 0.25D, 8));

                aggregatorEntDateAdjustRespList.add(aggregatorEntDateAdjustResp);
            }
            String profitDetail = JSONObject.toJSONString(aggregatorEntDateAdjustRespList);
            adjust.setProfitDetail(profitDetail);
            adjust.setProfitDetailByte(GZIPUtil.compressString(adjust.getProfitDetail()));
            adjustList.add(adjust);
        });
        return adjustList;
    }

    /**
     * 处理设备调节量
     *
     * @param deviceList
     * @param date
     * @param minuteListWith15
     * @param deliveryPowerMap
     * @param realTimeAvgPowerMap
     * @param issuePowerMap
     * @param baseLinePowerMap
     * @return
     */
    private List<AggregatorDeviceDateProfit> dealDeviceProfitList(
            List<AggregatorEntDevice> deviceList,
            String date,
            List<String> minuteListWith15,
            Map<String, Map<String, Double>> deliveryPowerMap,
            Map<String, Map<String, Double>> realTimeAvgPowerMap,
            Map<String, Map<String, Double>> issuePowerMap,
            Map<String, Map<String, Double>> baseLinePowerMap) {
        List<AggregatorDeviceDateProfit> profitList = Lists.newArrayList();
        deviceList.forEach(device -> {
            AggregatorDeviceDateProfit profit = new AggregatorDeviceDateProfit();
            profit.setAggregatorId(device.getAggregatorId());
            profit.setEntId(device.getEntId());
            profit.setResourceTypeId(device.getResourceTypeId());
            profit.setDeviceBaseId(device.getDeviceBaseId());
            profit.setDate(date);
            List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
            for (int i = 0; i < minuteListWith15.size() - 1; i++) {
                String minute = minuteListWith15.get(i);
                String minuteNext = minuteListWith15.get(i + 1);
                AggregatorDeviceDateProfitResp aggregatorDeviceDateProfitResp = new AggregatorDeviceDateProfitResp();
                aggregatorDeviceDateProfitResp.setAggregatorId(device.getAggregatorId());
                aggregatorDeviceDateProfitResp.setEntId(device.getEntId());
                aggregatorDeviceDateProfitResp.setResourceTypeId(device.getResourceTypeId());
                aggregatorDeviceDateProfitResp.setDeviceBaseId(device.getDeviceBaseId());
                aggregatorDeviceDateProfitResp.setDate(date);
                aggregatorDeviceDateProfitResp.setStartTime(minute);
                aggregatorDeviceDateProfitResp.setEndTime(minuteNext);
                Map<String, Double> deliveryTimeMap = deliveryPowerMap.get(device.getDeviceBaseId());
                if (null != deliveryTimeMap && deliveryTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setDeliveryPower(deliveryTimeMap.get(minuteNext));
                }
                Map<String, Double> realTimeMap = realTimeAvgPowerMap.get(device.getDeviceBaseId());
                if (null != realTimeMap && realTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setReallyPower(realTimeMap.get(minuteNext));
                }
                Map<String, Double> issueTimeMap = issuePowerMap.get(device.getDeviceBaseId());
                if (null != issueTimeMap && issueTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setIssuePower(issueTimeMap.get(minuteNext));
                }
                aggregatorDeviceDateProfitResp.setMinPower(MathUtils.compareReturnMinABS(aggregatorDeviceDateProfitResp.getReallyPower(), aggregatorDeviceDateProfitResp.getMinPower()));
                Map<String, Double> baseLineTimeMap = baseLinePowerMap.get(device.getDeviceBaseId());
                if (null != baseLineTimeMap && baseLineTimeMap.size() > 0) {
                    aggregatorDeviceDateProfitResp.setBaseLinePower(baseLineTimeMap.get(DateUtils.format(minuteNext, "HH:mm")));
                }
                Double subDouble = null;
                Double estimateSubDouble = null;
                if (device.getResourceTypeId().equals("27")) {
                    if (null != aggregatorDeviceDateProfitResp.getMinPower() && aggregatorDeviceDateProfitResp.getMinPower() < 0) {
                        if (null == aggregatorDeviceDateProfitResp.getBaseLinePower() || aggregatorDeviceDateProfitResp.getBaseLinePower() < 0) {
                            subDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getMinPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                            if (null == subDouble || subDouble.compareTo(0D) < 0) {
                                subDouble = 0D;
                            }
                        } else {
                            subDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getMinPower(), 0D);
                        }
                    } else {
                        subDouble = 0D;
                    }
                    if (null != aggregatorDeviceDateProfitResp.getDeliveryPower() && aggregatorDeviceDateProfitResp.getDeliveryPower() < 0) {
                        if (null == aggregatorDeviceDateProfitResp.getBaseLinePower() && aggregatorDeviceDateProfitResp.getBaseLinePower() < 0) {
                            estimateSubDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getDeliveryPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                            if (null == estimateSubDouble || estimateSubDouble.compareTo(0D) < 0) {
                                estimateSubDouble = 0D;
                            }
                        } else {
                            estimateSubDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getDeliveryPower(), 0D);
                        }
                    } else {
                        estimateSubDouble = 0D;
                    }
                } else {
                    subDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getMinPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                    if (null == subDouble || subDouble.compareTo(0D) < 0) {
                        subDouble = 0D;
                    }
                    estimateSubDouble = MathUtils.subDoubleABS(aggregatorDeviceDateProfitResp.getDeliveryPower(), aggregatorDeviceDateProfitResp.getBaseLinePower());
                    if (null == estimateSubDouble || estimateSubDouble.compareTo(0D) < 0) {
                        estimateSubDouble = 0D;
                    }
                }
                aggregatorDeviceDateProfitResp.setCountPower(MathUtils.doublePoint(subDouble, 8));
                aggregatorDeviceDateProfitResp.setEstimatePower(MathUtils.doublePoint(estimateSubDouble, 8));
                //1分钟功率 乘以 15 分钟 除以 60 分钟
                aggregatorDeviceDateProfitResp.setCountElectricQuantity(MathUtils.mulDoubleNull(aggregatorDeviceDateProfitResp.getCountPower(), 0.25D, 8));
                aggregatorDeviceDateProfitResp.setEstimateElectricQuantity(MathUtils.mulDoubleNull(aggregatorDeviceDateProfitResp.getEstimatePower(), 0.25D, 8));
                aggregatorDeviceDateProfitRespList.add(aggregatorDeviceDateProfitResp);
            }
            String profitDetail = JSONObject.toJSONString(aggregatorDeviceDateProfitRespList);
            profit.setProfitDetail(profitDetail);
            profit.setProfitDetailByte(GZIPUtil.compressString(profit.getProfitDetail()));
            profitList.add(profit);
        });
        return profitList;
    }

    /**
     * 处理收益数据
     *
     * @param dataList
     * @param resourceTypeId
     * @return
     */
    private Map<String, Double> profitMap(List<Map<String, String>> dataList, String resourceTypeId) {
        Map<String, Double> profitMap = new HashMap<>();
        dataList.forEach(data -> {
            String profitAndTime = data.get("FEE-" + resourceTypeId + "-1");
            if (StringUtils.isNotEmpty(profitAndTime)) {
                data.values().forEach(profitTime -> {
                    String[] profitAndTimes = profitTime.split(":");
                    profitMap.put(DateUtils.stampToDate(profitAndTimes[1]), MathUtils.stringToDouble(profitAndTimes[0]));
                });
            }
        });
        return profitMap;
    }

    /**
     * 查询申报功率
     *
     * @param resourceTypeId
     * @param date
     * @return
     */
    private Map<String, Map<String, Double>> getDeliveryPowerMap(String resourceTypeId, String date) {
        Map<String, Map<String, Double>> deliveryPowerMap = new HashMap<>();
        List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList = aggregatorDeviceDateDeliveryChartService.getAggregatorDeviceDateDeliveryChartListByResourceTypeId(resourceTypeId, date);
        if (CollectionUtils.isEmpty(aggregatorDeviceDateDeliveryChartList)) {
            return deliveryPowerMap;
        }
        aggregatorDeviceDateDeliveryChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getDeliveryChart())).forEach(chart -> {
            List<DataResp> dataRespList = JSONArray.parseArray(chart.getDeliveryChart(), DataResp.class);
            if (CollectionUtils.isNotEmpty(dataRespList)) {
                Map<String, Double> timeMap = dataRespList.stream().collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                deliveryPowerMap.put(chart.getDeviceBaseId(), timeMap);
            }
        });
        return deliveryPowerMap;
    }


    /**
     * 查询下发功率
     *
     * @param resourceTypeId
     * @param date
     * @return
     */
    private Map<String, Map<String, Double>> getIssuePowerMap(String resourceTypeId, String date) {
        Map<String, Map<String, Double>> issuePowerMap = new HashMap<>();
        List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList = aggregatorDeviceDateIssueChartService.getAggregatorDeviceDateIssueChartListByResourceTypeId(resourceTypeId, date);
        if (CollectionUtils.isEmpty(aggregatorDeviceDateIssueChartList)) {
            return issuePowerMap;
        }
        aggregatorDeviceDateIssueChartList.stream().filter(chart -> null != chart && StringUtils.isNotEmpty(chart.getIssueChart())).forEach(chart -> {
            List<DataResp> dataRespList = JSONArray.parseArray(chart.getIssueChart(), DataResp.class);
            if (null != dataRespList && dataRespList.size() > 0) {
                Map<String, Double> timeMap = dataRespList.stream().collect(toMap(DataResp::getTime, DataResp::getValue));
                issuePowerMap.put(chart.getDeviceBaseId(), timeMap);
            }
        });
        return issuePowerMap;
    }

    /**
     * 查询大数据
     *
     * @param deviceList
     * @param minuteList
     * @return
     */
    private Map<String, Map<String, Double>> getRealTimeAvgPower(List<AggregatorEntDevice> deviceList, List<BigDataHistoryResp> bigDataHistoryRespList, List<String> minuteList) {
        Map<String, Map<String, Double>> deviceBaseIdTimeValueMap = new HashMap<>();
        if (CollectionUtils.isEmpty(bigDataHistoryRespList)) {
            return deviceBaseIdTimeValueMap;
        }
        Map<String, List<DataResp>> deviceIdStationIdMap = bigDataHistoryRespList.stream().filter(e -> null != e.getDataResp()).collect(toMap(bigDataHistoryResp -> bigDataHistoryResp.getEquipMK() + "_" + bigDataHistoryResp.getEquipID() + "," + bigDataHistoryResp.getStaId(), bigDataHistoryResp -> bigDataHistoryResp.getDataResp(), (v1, v2) -> v1));
        deviceList.forEach(device -> {
            Map<String, Double> timeMap = new HashMap<>();
            String key = device.getDeviceId() + "," + device.getStationId();
            List<DataResp> dataRespList = deviceIdStationIdMap.get(key);
            if (null != dataRespList && dataRespList.size() > 0) {
                Map<String, Double> dateRespMap = dataRespList.stream().filter(dataResp -> null != dataResp && null != dataResp.getValue()).collect(toMap(DataResp::getTime, DataResp::getValue, (v1, v2) -> v1));
                for (int i = 1; i < minuteList.size() - 1; i += 15) {
                    AtomicReference<Double> totalValue = new AtomicReference<>();
                    AtomicReference<Integer> num = new AtomicReference<>(0);
                    minuteList.subList(i, i + 15).forEach(minute -> {
                        if (null != dateRespMap.get(minute)) {
                            num.getAndSet(num.get() + 1);
                            if (null == totalValue.get()) {
                                totalValue.set(dateRespMap.get(minute));
                            } else {
                                totalValue.updateAndGet(v -> v + dateRespMap.get(minute));
                            }
                        }
                    });
                    if (null != totalValue.get() && !num.get().equals(0)) {
                        Double avgPower = MathUtils.divideNull(totalValue.get(), Double.valueOf(String.valueOf(num)), 8);
                        timeMap.put(minuteList.get(i + 14), avgPower);
                        if (StringUtils.isNotEmpty(device.getResourceTypeId()) && device.getResourceTypeId().equals("27")) {
                            timeMap.put(minuteList.get(i + 14), null == avgPower ? null : 0 - avgPower);
                        }
                    }
                }
            }
            deviceBaseIdTimeValueMap.put(device.getDeviceBaseId(), timeMap);
        });
        return deviceBaseIdTimeValueMap;
    }

    /**
     * 根据2023新申报逻辑计算用户及聚合商收益
     *
     * @param aggregatorId
     * @param date
     * @param aggregatorEntDateAdjustList todo check update
     */
    private void dealAggregatorAndEntProfitNew(String aggregatorId, String date, List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList) {
        if (CollectionUtils.isEmpty(aggregatorEntDateAdjustList)) {
            return;
        }
        //查询企业有效时间配置
        Map<String, List<AggregatorEntProfitTime>> entTimeMap = aggregatorEntProfitTimeService.getEntMap(aggregatorId);
        //写入企业收益
        List<AggregatorEntDateAdjustResp> aggregatorEntDateAdjustRespList = Lists.newArrayList();
        aggregatorEntDateAdjustList.stream().filter(adjust -> null != adjust && StringUtils.isNotEmpty(adjust.getProfitDetail())).forEach(adjust -> {
            List<AggregatorEntDateAdjustResp> profitRespList = JSONArray.parseArray(adjust.getProfitDetail(), AggregatorEntDateAdjustResp.class);
            aggregatorEntDateAdjustRespList.addAll(profitRespList);
        });
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = Lists.newArrayList();
        List<String> entIdList = aggregatorEntDateAdjustList.stream().map(AggregatorEntDateAdjust::getEntId).distinct().collect(toList());
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(entIdList);
        //用户分成
        Map<String, Double> entIdProfitPercentMap = aggregatorEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getPercent, (v1, v2) -> v1));
        Map<String, Double> entIdCountPriceMapNew = getEntIdCountPriceMapNew(aggregatorEntDateAdjustRespList);
        Map<String, Double> entIdElectricQuantityMapNew = getEntIdElectricQuantityMapNew(aggregatorEntDateAdjustRespList, entTimeMap);
        Map<String, Double> entIdDateProfitMap = aggregatorEntDateAdjustRespList.stream().filter(adjust -> null != adjust.getProfit()).collect(groupingBy(AggregatorEntDateAdjustResp::getEntId, summingDouble(AggregatorEntDateAdjustResp::getProfit)));
        entIdDateProfitMap.entrySet().forEach(entIdDateProfitMapEntry -> {
            AggregatorEntDateProfit aggregatorEntDateProfit = new AggregatorEntDateProfit();
            aggregatorEntDateProfit.setAggregatorId(aggregatorId);
            aggregatorEntDateProfit.setEntId(entIdDateProfitMapEntry.getKey());
            aggregatorEntDateProfit.setDate(date);
            Double entProfitPercent = entIdProfitPercentMap.get(entIdDateProfitMapEntry.getKey());
            Double entProfit = MathUtils.mulDoubleNull(entIdDateProfitMapEntry.getValue(), entProfitPercent, 8);
            aggregatorEntDateProfit.setEntProfit(null == entProfit ? 0 : MathUtils.doublePoint(entProfit, 8));
            aggregatorEntDateProfit.setElectricQuantity(null == entIdElectricQuantityMapNew.get(entIdDateProfitMapEntry.getKey()) ? 0 : MathUtils.doublePoint(entIdElectricQuantityMapNew.get(entIdDateProfitMapEntry.getKey()), 8));
            aggregatorEntDateProfit.setAveragePrice(entIdCountPriceMapNew.get(entIdDateProfitMapEntry.getKey()));
            aggregatorEntDateProfit.setCountProfit(entIdDateProfitMapEntry.getValue());
            aggregatorEntDateProfit.setCountPrice(MathUtils.divideZero(aggregatorEntDateProfit.getCountProfit(), aggregatorEntDateProfit.getElectricQuantity(), 8));
            aggregatorEntDateProfitList.add(aggregatorEntDateProfit);
        });
        aggregatorEntDateProfitService.saveByAggregatorId(aggregatorId, date, aggregatorEntDateProfitList);
        //写入聚合商收益
        Double totalIssueProfit = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getCountProfit()).collect(summingDouble(AggregatorEntDateProfit::getCountProfit));
        Double totalEntProfit = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getEntProfit()).collect(summingDouble(AggregatorEntDateProfit::getEntProfit));
        Double totalAggregatorProfit = MathUtils.subDouble(totalIssueProfit, totalEntProfit);
        Double totalElectricQuantity = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getElectricQuantity()).collect(summingDouble(AggregatorEntDateProfit::getElectricQuantity));
        AggregatorDateProfit aggregatorDateProfit = new AggregatorDateProfit();
        aggregatorDateProfit.setAggregatorId(aggregatorId);
        aggregatorDateProfit.setDate(date);
        aggregatorDateProfit.setIssueProfit(MathUtils.doublePoint(totalIssueProfit, 8));
        aggregatorDateProfit.setAggregatorProfit(null == totalAggregatorProfit ? 0 : MathUtils.doublePoint(totalAggregatorProfit, 8));
        aggregatorDateProfit.setEntProfit(MathUtils.doublePoint(totalEntProfit, 8));
        aggregatorDateProfit.setElectricQuantity(MathUtils.doublePoint(totalElectricQuantity, 8));
        aggregatorDateProfitService.save(aggregatorId, date, Arrays.asList(aggregatorDateProfit));
    }

    private void dealAggregatorAndEntProfit(String aggregatorId, String date, List<AggregatorDeviceDateProfit> aggregatorDeviceDateProfitList) {
        if (CollectionUtils.isEmpty(aggregatorDeviceDateProfitList)) {
            return;
        }
        //查询企业有效时间配置
        Map<String, List<AggregatorEntProfitTime>> entTimeMap = aggregatorEntProfitTimeService.getEntMap(aggregatorId);
        //写入企业收益
        List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList = Lists.newArrayList();
        aggregatorDeviceDateProfitList.stream().filter(profit -> null != profit && StringUtils.isNotEmpty(profit.getProfitDetail())).forEach(profit -> {
            List<AggregatorDeviceDateProfitResp> profitRespList = JSONArray.parseArray(profit.getProfitDetail(), AggregatorDeviceDateProfitResp.class);
            aggregatorDeviceDateProfitRespList.addAll(profitRespList);
        });
        List<AggregatorEntDateProfit> aggregatorEntDateProfitList = Lists.newArrayList();
        List<String> entIdList = aggregatorDeviceDateProfitList.stream().map(AggregatorDeviceDateProfit::getEntId).distinct().collect(toList());
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(entIdList);
        Map<String, Double> entIdProfitPercentMap = aggregatorEntList.stream().collect(toMap(AggregatorEnt::getEntId, AggregatorEnt::getPercent));
        Map<String, Double> entIdCountPriceMap = getEntIdCountPriceMap(aggregatorDeviceDateProfitRespList);
        Map<String, Double> entIdElectricQuantityMap = getEntIdElectricQuantityMap(aggregatorDeviceDateProfitRespList, entTimeMap);
        Map<String, Double> entIdDateProfitMap = aggregatorDeviceDateProfitRespList.stream().filter(profit -> null != profit.getProfit()).collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(AggregatorDeviceDateProfitResp::getProfit)));
        entIdDateProfitMap.entrySet().forEach(entIdDateProfitMapEntry -> {
            AggregatorEntDateProfit aggregatorEntDateProfit = new AggregatorEntDateProfit();
            aggregatorEntDateProfit.setAggregatorId(aggregatorId);
            aggregatorEntDateProfit.setEntId(entIdDateProfitMapEntry.getKey());
            aggregatorEntDateProfit.setDate(date);
            Double entProfitPercent = entIdProfitPercentMap.get(entIdDateProfitMapEntry.getKey());
            Double entProfit = MathUtils.mulDoubleNull(entIdDateProfitMapEntry.getValue(), entProfitPercent, 8);
            aggregatorEntDateProfit.setEntProfit(null == entProfit ? 0 : MathUtils.doublePoint(entProfit, 8));
            aggregatorEntDateProfit.setElectricQuantity(null == entIdElectricQuantityMap.get(entIdDateProfitMapEntry.getKey()) ? 0 : MathUtils.doublePoint(entIdElectricQuantityMap.get(entIdDateProfitMapEntry.getKey()), 8));
            aggregatorEntDateProfit.setAveragePrice(entIdCountPriceMap.get(entIdDateProfitMapEntry.getKey()));
            aggregatorEntDateProfit.setCountProfit(entIdDateProfitMapEntry.getValue());
            aggregatorEntDateProfit.setCountPrice(MathUtils.divideZero(aggregatorEntDateProfit.getCountProfit(), aggregatorEntDateProfit.getElectricQuantity(), 8));
            aggregatorEntDateProfitList.add(aggregatorEntDateProfit);
        });
        aggregatorEntDateProfitService.saveByAggregatorId(aggregatorId, date, aggregatorEntDateProfitList);
        //写入聚合商收益
        Double totalIssueProfit = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getCountProfit()).collect(summingDouble(AggregatorEntDateProfit::getCountProfit));
        Double totalEntProfit = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getEntProfit()).collect(summingDouble(AggregatorEntDateProfit::getEntProfit));
        Double totalAggregatorProfit = MathUtils.subDouble(totalIssueProfit, totalEntProfit);
        Double totalElectricQuantity = aggregatorEntDateProfitList.stream().filter(profit -> null != profit && null != profit.getElectricQuantity()).collect(summingDouble(AggregatorEntDateProfit::getElectricQuantity));
        AggregatorDateProfit aggregatorDateProfit = new AggregatorDateProfit();
        aggregatorDateProfit.setAggregatorId(aggregatorId);
        aggregatorDateProfit.setDate(date);
        aggregatorDateProfit.setIssueProfit(MathUtils.doublePoint(totalIssueProfit, 8));
        aggregatorDateProfit.setAggregatorProfit(null == totalAggregatorProfit ? 0 : MathUtils.doublePoint(totalAggregatorProfit, 8));
        aggregatorDateProfit.setEntProfit(MathUtils.doublePoint(totalEntProfit, 8));
        aggregatorDateProfit.setElectricQuantity(MathUtils.doublePoint(totalElectricQuantity, 8));
        aggregatorDateProfitService.save(aggregatorId, date, Arrays.asList(aggregatorDateProfit));
    }


    /**
     * 根据用户调节表查询企业平均价格
     *
     * @param aggregatorEntDateAdjustRespList
     * @return
     */
    private Map<String, Double> getEntIdCountPriceMapNew(List<AggregatorEntDateAdjustResp> aggregatorEntDateAdjustRespList) {
        Map<String, Double> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(aggregatorEntDateAdjustRespList)) {
            Map<String, List<AggregatorEntDateAdjustResp>> entMap = aggregatorEntDateAdjustRespList.stream().collect(groupingBy(AggregatorEntDateAdjustResp::getEntId));
            entMap.entrySet().forEach(entMapEntry -> {
                Double value = 0D;
                List<AggregatorEntDateAdjustResp> resultList = entMapEntry.getValue().stream().filter(resp -> null != resp && null != resp.getCountPrice() && 0 != resp.getCountPrice()).collect(toList());
                if (CollectionUtils.isNotEmpty(resultList)) {
                    DoubleSummaryStatistics doubleSummaryStatistics = resultList.stream().collect(summarizingDouble(AggregatorEntDateAdjustResp::getCountPrice));
                    if (null != doubleSummaryStatistics) {
                        value = MathUtils.doublePoint(doubleSummaryStatistics.getAverage(), 8);
                    }
                }
                resultMap.put(entMapEntry.getKey(), value);
            });
        }
        return resultMap;
    }

    /**
     * 根据用户调节表处理有效用电量
     *
     * @param aggregatorEntDateAdjustRespList
     * @param entTimeMap
     * @return
     */
    private Map<String, Double> getEntIdElectricQuantityMapNew(List<AggregatorEntDateAdjustResp> aggregatorEntDateAdjustRespList, Map<String, List<AggregatorEntProfitTime>> entTimeMap) {
        if (null == entTimeMap) {
            return aggregatorEntDateAdjustRespList.stream().collect(groupingBy(AggregatorEntDateAdjustResp::getEntId, summingDouble(adjustResp -> null == adjustResp.getElectricQuantity() ? 0 : adjustResp.getElectricQuantity())));
        } else {
            List<AggregatorEntDateAdjustResp> resultList = Lists.newArrayList();
            aggregatorEntDateAdjustRespList.stream().forEach(adjust -> {
                if (null != adjust) {
                    List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = entTimeMap.get(adjust.getEntId());
                    if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
                        aggregatorEntProfitTimeList.forEach(time -> {
                            if (DateUtils.format(adjust.getEndTime(), "HH:mm").compareTo(time.getStartTime()) >= 0 && DateUtils.format(adjust.getEndTime(), "HH:mm").compareTo(time.getEndTime()) <= 0) {
                                resultList.add(adjust);
                            }
                        });
                    } else {
                        resultList.add(adjust);
                    }
                }
            });
            return resultList.stream().collect(groupingBy(AggregatorEntDateAdjustResp::getEntId, summingDouble(adjustResp -> null == adjustResp.getElectricQuantity() ? 0 : adjustResp.getElectricQuantity())));
        }
    }

    /**
     * 查询企业平均价格
     *
     * @param aggregatorDeviceDateProfitRespList
     * @return
     */
    private Map<String, Double> getEntIdCountPriceMap(List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList) {
        Map<String, Double> resultMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(aggregatorDeviceDateProfitRespList)) {
            Map<String, List<AggregatorDeviceDateProfitResp>> entMap = aggregatorDeviceDateProfitRespList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId));
            entMap.entrySet().forEach(entMapEntry -> {
                Double value = 0D;
                List<AggregatorDeviceDateProfitResp> resultList = entMapEntry.getValue().stream().filter(resp -> null != resp && null != resp.getCountPrice() && 0 != resp.getCountPrice()).collect(toList());
                if (CollectionUtils.isNotEmpty(resultList)) {
                    DoubleSummaryStatistics doubleSummaryStatistics = resultList.stream().collect(summarizingDouble(AggregatorDeviceDateProfitResp::getCountPrice));
                    if (null != doubleSummaryStatistics) {
                        value = MathUtils.doublePoint(doubleSummaryStatistics.getAverage(), 8);
                    }
                }
                resultMap.put(entMapEntry.getKey(), value);
            });
        }
        return resultMap;
    }

    /**
     * 处理有效用电量
     *
     * @param aggregatorDeviceDateProfitRespList
     * @param entTimeMap
     * @return
     */
    private Map<String, Double> getEntIdElectricQuantityMap(List<AggregatorDeviceDateProfitResp> aggregatorDeviceDateProfitRespList, Map<String, List<AggregatorEntProfitTime>> entTimeMap) {
        if (null == entTimeMap) {
            return aggregatorDeviceDateProfitRespList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(deviceProfitResp -> null == deviceProfitResp.getElectricQuantity() ? 0 : deviceProfitResp.getElectricQuantity())));
        } else {
            List<AggregatorDeviceDateProfitResp> resultList = Lists.newArrayList();
            aggregatorDeviceDateProfitRespList.stream().forEach(profit -> {
                if (null != profit) {
                    List<AggregatorEntProfitTime> aggregatorEntProfitTimeList = entTimeMap.get(profit.getEntId());
                    if (CollectionUtils.isNotEmpty(aggregatorEntProfitTimeList)) {
                        aggregatorEntProfitTimeList.forEach(time -> {
                            if (DateUtils.format(profit.getEndTime(), "HH:mm").compareTo(time.getStartTime()) >= 0 && DateUtils.format(profit.getEndTime(), "HH:mm").compareTo(time.getEndTime()) <= 0) {
                                resultList.add(profit);
                            }
                        });
                    } else {
                        resultList.add(profit);
                    }
                }
            });
            return resultList.stream().collect(groupingBy(AggregatorDeviceDateProfitResp::getEntId, summingDouble(deviceProfitResp -> null == deviceProfitResp.getElectricQuantity() ? 0 : deviceProfitResp.getElectricQuantity())));
        }
    }
}
