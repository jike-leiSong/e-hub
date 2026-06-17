package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.model.req.*;
import cn.sl.ehub.console.model.resp.*;
import cn.sl.ehub.console.model.vo.*;
import cn.sl.ehub.console.service.IHistoryQueryService;
import cn.sl.ehub.service.req.AdjustSituationExcelRep;
import cn.sl.ehub.service.req.IndexOverviewTableResp;
import cn.sl.ehub.service.resp.HistoryQueryDeviceMetricResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 历史查询服务实现 (空实现)
 *
 * @Author sl
 * @Date 2026-06-15
 */
@Slf4j
@Service
public class HistoryQueryServiceImpl implements IHistoryQueryService {

    @Override
    public HistoryQueryGraphVO userAdjustmentGraph(UserAdjustmentGraphReq userAdjustmentGraphReq, String simulate) {
        log.warn("userAdjustmentGraph called - empty implementation");
        return new HistoryQueryGraphVO();
    }

    @Override
    public HistoryQueryGraphVO userAdjustmentGraphNew(NewUserAdjustmentGraphReq userAdjustmentGraphReq, String simulate) {
        log.warn("userAdjustmentGraphNew called - empty implementation");
        return new HistoryQueryGraphVO();
    }

    @Override
    public PageResultVO<HistoryQueryTableVO> userAdjustmentTable(UserAdjustmentTableReq userAdjustmentTableReq) {
        log.warn("userAdjustmentTable called - empty implementation");
        return new PageResultVO<>();
    }

    @Override
    public List<LineDataGraphResp> deviceRunStatusChart(DeviceRunStatusReq deviceRunStatusReq, String simulate) {
        log.warn("deviceRunStatusChart called - empty implementation");
        return new ArrayList<>();
    }

    @Override
    public ProfitStatisticsVO profitStatistics(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("profitStatistics called - empty implementation");
        return new ProfitStatisticsVO();
    }

    @Override
    public UserProfitStatisticsVO userProfitStatistics(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("userProfitStatistics called - empty implementation");
        return new UserProfitStatisticsVO();
    }

    @Override
    public IndexOverviewResp getTotalPowerChart(String simulate, String aggregatorId, String resourceTypeId, String startDate, String endDate) {
        log.warn("getTotalPowerChart called - empty implementation");
        return new IndexOverviewResp();
    }

    @Override
    public IndexOverviewResp getPrice(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("getPrice called - empty implementation");
        return new IndexOverviewResp();
    }

    @Override
    public IndexOverviewTableResp getPriceTable(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("getPriceTable called - empty implementation");
        return new IndexOverviewTableResp();
    }

    @Override
    public List<PriceExcelDateResp> getPriceExcel(ProfitStatisticsReq profitStatisticsReq) {
        log.warn("getPriceExcel called - empty implementation");
        return new ArrayList<>();
    }

    @Override
    public List<HistoryQueryDeviceMetricResp> getMetricList() {
        log.warn("getMetricList called - empty implementation");
        return new ArrayList<>();
    }

    @Override
    public List<HistoryProfitCalculationExcelResp> getProfitCalculation(String entId, String startDate, String endDate) {
        log.warn("getProfitCalculation called - empty implementation");
        return new ArrayList<>();
    }

    @Override
    public LinkedHashMap<String, List<HistoryProfitCalculationTimeExcelResp>> getProfitCalculationMap(String entId, String startDate, String endDate) {
        log.warn("getProfitCalculationMap called - empty implementation");
        return new LinkedHashMap<>();
    }

    @Override
    public HistoryAdjustExcelResp exportAdjustSituationExcel(AdjustSituationExcelRep adjustSituationExcelRep) {
        log.warn("exportAdjustSituationExcel called - empty implementation");
        return new HistoryAdjustExcelResp();
    }

    @Override
    public HistoryAdjustExcelResp exportBuZhaoUploadData(AdjustSituationExcelRep req) {
        log.warn("exportBuZhaoUploadData called - empty implementation");
        return new HistoryAdjustExcelResp();
    }
}
