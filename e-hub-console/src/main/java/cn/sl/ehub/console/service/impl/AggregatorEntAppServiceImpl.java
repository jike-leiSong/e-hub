package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.model.req.RunPlanTodayReq;
import cn.sl.ehub.console.model.vo.EntExistResourceTypeVO;
import cn.sl.ehub.console.model.vo.RunPlanTodayVO;
import cn.sl.ehub.console.service.IAggregatorEntAppService;
import cn.sl.ehub.console.service.IAggregatorEntDapChartService;
import cn.sl.ehub.console.service.IAggregatorEntDeviceService;
import cn.sl.ehub.console.service.IAggregatorResourceTypeService;
import cn.sl.ehub.service.vo.*;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
@RequiredArgsConstructor
@Slf4j
@Service
public class AggregatorEntAppServiceImpl implements IAggregatorEntAppService {
    private final IAggregatorEntDapChartService aggregatorEntDapChartService;
    private final IAggregatorEntDeviceService aggregatorEntDeviceService;
    private final IAggregatorResourceTypeService aggregatorResourceTypeService;
    @Override
    public RunPlanTodayVO getRunPlanChart(RunPlanTodayReq runPlanTodayReq) {
        RunPlanTodayVO result = new RunPlanTodayVO();
        String entId = runPlanTodayReq.getEntId();
        String resourceTypeId = runPlanTodayReq.getResourceTypeId();
        String dateTime = runPlanTodayReq.getDateTime();
        List<AggregatorEntDapChart> entDapLineList = aggregatorEntDapChartService.getEntDapLineByDate(entId, resourceTypeId, dateTime);
        if(CollectionUtil.isNotEmpty(entDapLineList)){
            List<DataResp> baseLineDataList = entDapLineList.stream().flatMap(a -> {
                String baseLineLoadChart = a.getDapChart();
                List<DataResp> list = JSONObject.parseArray(baseLineLoadChart, DataResp.class);
                return list.stream();

            }).collect(toList());
            result.setDapChart(baseLineDataList);
        }
        return result;
    }

    @Override
    public List<EntExistResourceTypeVO> getEntExistResourceTypeVO(String entId) {
        List<EntExistResourceTypeVO> result = new ArrayList<>();
        List<AggregatorEntDevice> aggregatorEntDeviceListModel = aggregatorEntDeviceService.getAggregatorEntDeviceListModel(null, entId, null, null);
        if(CollectionUtil.isEmpty(aggregatorEntDeviceListModel)){
            return result;
        }
        List<String> resourceTypeList = aggregatorEntDeviceListModel.stream().map(e -> e.getResourceTypeId()).distinct().collect(toList());
        if(CollectionUtil.isEmpty(resourceTypeList)){
            return result;
        }
        for (String resourceTypeId : resourceTypeList) {
            AggregatorResourceType aggregatorResourceTypeInfo = aggregatorResourceTypeService.getTypeById(resourceTypeId);
            EntExistResourceTypeVO entExistResourceTypeVO = new EntExistResourceTypeVO();
            entExistResourceTypeVO.setEntId(entId);
            entExistResourceTypeVO.setResourceTypeId(resourceTypeId);
            entExistResourceTypeVO.setResourceTypeName(aggregatorResourceTypeInfo.getName());
            result.add(entExistResourceTypeVO);
        }
        return result;
    }

}
