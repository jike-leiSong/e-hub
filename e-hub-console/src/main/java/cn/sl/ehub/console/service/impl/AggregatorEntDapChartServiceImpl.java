package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.service.IAggregatorEntDapChartService;
import cn.sl.ehub.service.vo.AggregatorEntDapChart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 企业日前计划服务实现 (空实现)
 *
 * @Author sl
 * @Date 2026-06-15
 **/
@Slf4j
@Service
public class AggregatorEntDapChartServiceImpl implements IAggregatorEntDapChartService {

    @Override
    public void batchDelete(List<String> entIdList, String date) {
        log.warn("batchDelete called with entIdList size: {}, date: {} - empty implementation",
                entIdList != null ? entIdList.size() : 0, date);
    }

    @Override
    public int batchInsert(List<AggregatorEntDapChart> AggregatorEntDapChartList) {
        log.warn("batchInsert called with list size: {} - empty implementation",
                AggregatorEntDapChartList != null ? AggregatorEntDapChartList.size() : 0);
        return 0;
    }

    @Override
    public List<AggregatorEntDapChart> getEntDapLine(String entId, String resourceTypeId, String startDate, String endDate) {
        log.warn("getEntDapLine called with entId: {}, resourceTypeId: {}, startDate: {}, endDate: {} - empty implementation",
                entId, resourceTypeId, startDate, endDate);
        return new ArrayList<>();
    }

    @Override
    public Map<String, List<AggregatorEntDapChart>> getMoreEntDapLine(List<String> entIdList, String resourceTypeId, String startDate, String endDate) {
        log.warn("getMoreEntDapLine called with entIdList size: {}, resourceTypeId: {}, startDate: {}, endDate: {} - empty implementation",
                entIdList != null ? entIdList.size() : 0, resourceTypeId, startDate, endDate);
        return new HashMap<>();
    }

    @Override
    public List<AggregatorEntDapChart> getEntDapLineByDate(String entId, String resourceTypeId, String date) {
        log.warn("getEntDapLineByDate called with entId: {}, resourceTypeId: {}, date: {} - empty implementation",
                entId, resourceTypeId, date);
        return new ArrayList<>();
    }

    @Override
    public List<AggregatorEntDapChart> getAggregatorDapLineByDate(String aggregatorId, String resourceTypeId, String date) {
        log.warn("getAggregatorDapLineByDate called with aggregatorId: {}, resourceTypeId: {}, date:  - empty implementation",
                aggregatorId, resourceTypeId, date);
        return new ArrayList<>();
    }

    @Override
    public List<AggregatorEntDapChart> getBatchDapLineByEntId(List<String> entIds, String date) {
        log.warn("getBatchDapLineByEntId called with entIds size: {}, date: {} - empty implementation",
                entIds != null ? entIds.size() : 0, date);
        return new ArrayList<>();
    }
}
