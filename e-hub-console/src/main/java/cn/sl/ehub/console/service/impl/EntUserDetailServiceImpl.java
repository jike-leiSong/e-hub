package cn.sl.ehub.console.service.impl;

import cn.enn.cim.resp.DeviceBaseInfo;
import cn.enn.cim.resp.SystemBaseInfo;
import cn.enn.cim.service.CimBaseService;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.service.mapper.AggregatorEntMapper;
import cn.sl.ehub.console.model.vo.OptionVO;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.req.UpdateEntDeviceReq;
import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.service.resp.EntUserDetailResp;
import cn.sl.ehub.service.resp.EntUserDeviceResp;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.service.vo.*;
import cn.enn.uac.resp.UacDevopsEntInfo;
import cn.enn.uac.resp.UacEntInfo;
import cn.enn.uac.service.UacAdminService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 企业用户查询
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class EntUserDetailServiceImpl implements IEntUserDetailService {

    private final IAggregatorEntService aggregatorEntService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    private final CimBaseService cimBaseService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorSmsService aggregatorSmsService;
    private final UacAdminService uacAdminService;

    @Override
    public List<EntUserDetailResp> getEntUserDetailRespList(String aggregatorId) {
        return aggregatorEntService.getEntUserDetailRespList(aggregatorId);
    }

    @Override
    public List<OptionVO> getEntOptions(String aggregatorId, String resourceTypeId) {
        List<AggregatorEnt> aggregatorEntList = aggregatorEntService.getAggregatorEntList(aggregatorId);
        if (aggregatorEntList == null) {
            return new ArrayList<>();
        }
        // 20240405 增加企业按拥有资源过滤--查询设备判断资源类型
        if (StringUtils.isNotEmpty(resourceTypeId)) {
            List<AggregatorEntDevice> deviceListByAggregatorId = aggregatorEntDeviceService.getDeviceListByAggregatorId(aggregatorId);
            if (CollectionUtils.isEmpty(deviceListByAggregatorId)) {
                return new ArrayList<>();
            }
            // 筛选该能源类型下设备数量大于0的企业
            List<String> entidList = deviceListByAggregatorId.stream()
                    .filter(device -> null != device && device.getResourceTypeId().equals(resourceTypeId))
                    .collect(Collectors.groupingBy(AggregatorEntDevice::getEntId, Collectors.counting()))
                    .entrySet().stream().filter(entry -> entry.getValue() > 0)
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(entidList)) {
                return new ArrayList<>();
            }
            return aggregatorEntList.stream().filter(ent -> null != ent && entidList.contains(ent.getEntId()))
                    .map(e -> new OptionVO(e.getEntName(), e.getEntId())).collect(Collectors.toList());
        }
        return aggregatorEntList.stream().map(e -> new OptionVO(e.getEntName(), e.getEntId())).collect(Collectors.toList());
    }

    @Override
    public PageResultVO<EntUserDetailResp> getEntUserDetailWithDevice(String aggregatorId, String entId,
                                                                      Double powerGetterThan, Double powerLessThan,
                                                                      Double percent,
                                                                      Integer pageIndex, Integer pageSize) {
        if (pageIndex == null || pageSize == null) {
            pageIndex = 1;
            pageSize = 10;
        }
        List<AggregatorResourceType> aggregatorResourceTypeList = aggregatorResourceTypeService.getAggregatorResourceTypeList();
        Page<EntUserDetailResp> page = PageHelper.startPage(pageIndex, pageSize)
                .doSelectPage(() -> getEntUserDetailRespList(aggregatorId,
                        entId, powerGetterThan, powerLessThan, percent, aggregatorResourceTypeList));
        return PageResultVO.<EntUserDetailResp>builder().list(page.getResult())
                .pageIndex(pageIndex).pageSize(pageSize).total((int) page.getTotal()).build();
    }

    @Override
    public List<OptionVO> getPercentOptions(String aggregatorId) {
        List<OptionVO> optionVOList = new ArrayList<>();
        List<Double> percentList = aggregatorEntService.selectPercentDistinct(aggregatorId);
        if (CollectionUtils.isEmpty(percentList)) {
            return optionVOList;
        }
        for (Double percent : percentList) {
            if (percent == null || percent.isNaN()) {
                continue;
            }
            int entPercent = (int) (percent * 100);
            int aggregatorPercent = 100 - entPercent;
            int multiple = 100;
            if (entPercent % 10 == 0 && aggregatorPercent % 10 == 0) {
                entPercent = entPercent / 10;
                aggregatorPercent = aggregatorPercent / 10;
                multiple = multiple / 10;
            }
            optionVOList.add(new OptionVO(aggregatorPercent + ":" + entPercent,
                    String.valueOf((double) entPercent / multiple)));
        }
        return optionVOList;
    }

    @Override
    public List<EntUserDetailResp> getEntAndDeviceRespList(String aggregatorId,String entId, String startYear, String endYear) {
        List<EntUserDetailResp> respList = Lists.newArrayList();
        if (StringUtils.isNotEmpty(startYear) && StringUtils.isNotEmpty(endYear)) {
//            respList = aggregatorEntService.selectEntAndDeviceList(entId, DateUtils.getYearList(startYear, endYear));
            respList = aggregatorEntService.selectEntAndDeviceListByAggregatorId(aggregatorId,entId, DateUtils.getYearList(startYear, endYear));

        } else if (StringUtils.isNotEmpty(startYear)) {
//            respList = aggregatorEntService.selectEntAndDeviceListByStartYear(entId, startYear);
            respList = aggregatorEntService.selectEntAndDeviceListByStartYearAggregatorId(aggregatorId,entId, startYear);
        } else if (StringUtils.isNotEmpty(endYear)) {
//            respList = aggregatorEntService.selectEntAndDeviceListByEndYear(entId, endYear);
            respList = aggregatorEntService.selectEntAndDeviceListByEndYearAggregatorId(aggregatorId,entId, startYear);
        } else {
//            respList = aggregatorEntService.selectEntAndDeviceList(entId, Lists.newArrayList());
            respList = aggregatorEntService.selectEntAndDeviceListByAggregatorId(aggregatorId,entId, Lists.newArrayList());
        }
//        if (CollectionUtils.isNotEmpty(respList)) {
//            respList.stream().filter(resp -> null != resp).forEach(resp -> {
//                resp.setAgreement(StringUtils.isEmpty(resp.getAgreement()) ? null : resp.getAgreement() + "?filename=" + resp.getEntName() + ".docx");
//            });
//        }

        // 设置聚合商percent
        if (!CollectionUtils.isEmpty(respList)) {
            BigDecimal bd = new BigDecimal(100);
            respList.stream().forEach(r -> {
                Double percent = r.getPercent();
                if (percent != null) {
                    percent = BigDecimal.ONE.subtract(new BigDecimal(percent)).multiply(bd).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    r.setPercent(percent);
                }
            });
        }
        return respList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAgreement(String entId) {
        aggregatorEntService.deleteAgreement(entId);
        return true;
    }

    @Override
    public List<UpdateEntDeviceReq> getCimDeviceList(String aggregatorId, String entId, String stationId) {
        List<UpdateEntDeviceReq> deviceList = Lists.newArrayList();
        ResultVO<List<DeviceBaseInfo>> resultVO = cimBaseService.getDevices("METE", stationId);
        if (null != resultVO && resultVO.getCode().equals(StatusCode.SUCCESS.getCode()) && CollectionUtils.isNotEmpty(resultVO.getData())) {
            resultVO.getData().forEach(device -> {
                UpdateEntDeviceReq req = new UpdateEntDeviceReq();
                req.setAggregatorId(aggregatorId);
                req.setEntId(entId);
                req.setStationId(stationId);
                req.setDeviceBaseId(device.getId().toString());
                req.setDeviceId(device.getDeviceId());
                req.setDeviceName(device.getName());
                deviceList.add(req);
            });
        }
        ResultVO<List<DeviceBaseInfo>> resultVO1 = cimBaseService.getDevices("CP", stationId);
        if (null != resultVO1 && resultVO1.getCode().equals(StatusCode.SUCCESS.getCode()) && CollectionUtils.isNotEmpty(resultVO.getData())) {
            resultVO1.getData().forEach(device -> {
                UpdateEntDeviceReq req = new UpdateEntDeviceReq();
                req.setAggregatorId(aggregatorId);
                req.setEntId(entId);
                req.setStationId(stationId);
                req.setDeviceBaseId(device.getId().toString());
                req.setDeviceId(device.getDeviceId());
                req.setDeviceName(device.getName());
                deviceList.add(req);
            });
        }
        return deviceList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEnt(UpdateEntReq req) {
        Double percent = req.getPercent();
        // 聚合商分成比例 = 1- 用户比例
        percent = new BigDecimal(100).subtract(new BigDecimal(percent)).multiply(new BigDecimal(0.01)).doubleValue();
        req.setPercent(percent);
        //更新企业信息
        aggregatorEntService.updateAggregatorEntAgreementInfo(req);
        //更新设备信息
        aggregatorEntDeviceService.updateDeviceInfoList(req.getDevices(), req.getEntId());
        //更新联系人
        aggregatorSmsService.save(req.getPhones(), req.getEntId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean autoUpdateEnt(String aggregatorId) {
        ResultVO<UacDevopsEntInfo> uacResult = uacAdminService.getNewEntIdByDevopsId(aggregatorId);
        if (null != uacResult
                && uacResult.getCode().equals(StatusCode.SUCCESS.getCode())
                && null != uacResult.getData()
                && CollectionUtils.isNotEmpty(uacResult.getData().getEnts())) {
            List<AggregatorEnt> aggregatorEntList = Lists.newArrayList();
            List<UacEntInfo> uacEntInfoList = uacResult.getData().getEnts();
            uacEntInfoList.forEach(ent -> {
                AggregatorEnt aggregatorEnt = new AggregatorEnt();
                aggregatorEnt.setAggregatorId(aggregatorId);
                aggregatorEnt.setEntId(ent.getId());
                ResultVO<SystemBaseInfo> cimEntResult = cimBaseService.systemInfo(ent.getId());
                if (null != cimEntResult && cimEntResult.getCode().equals(StatusCode.SUCCESS.getCode()) && null != cimEntResult.getData()) {
                    aggregatorEnt.setStationId(cimEntResult.getData().getCode());
                }
                aggregatorEnt.setEntName(ent.getEntName());
                aggregatorEnt.setPercent(0.9);
                aggregatorEnt.setAllowApplyTime("08:30:00");
                aggregatorEnt.setWinTime("18:30:00");
                aggregatorEnt.setStateGridCode("HUABEI");
                aggregatorEnt.setStateGridName("华北电网");
                aggregatorEntList.add(aggregatorEnt);
            });
            aggregatorEntService.addAggregatorEntList(aggregatorEntList);
        }
        return true;
    }

    /**
     * 处理资源类型名称
     *
     * @param aggregatorId
     * @param entId
     * @param powerGetterThan
     * @param powerLessThan
     * @param percent
     * @return
     */
    private List<EntUserDetailResp> getEntUserDetailRespList(String aggregatorId, String entId, Double powerGetterThan, Double powerLessThan, Double percent, List<AggregatorResourceType> aggregatorResourceTypeList) {
        List<EntUserDetailResp> respList = aggregatorEntService.selectEntUserDetailWithDevice(aggregatorId, entId, powerGetterThan, powerLessThan, percent);
        if (null != respList && respList.size() > 0) {
            respList.stream().filter(resp -> null != resp).forEach(resp -> {
                resp.setAgreement(StringUtils.isEmpty(resp.getAgreement()) ? null : resp.getAgreement() + "?filename=" + resp.getEntName() + ".docx");
            });
        }
        return respList;
    }
}
