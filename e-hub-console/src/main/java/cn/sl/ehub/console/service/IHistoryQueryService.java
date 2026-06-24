package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.*;
import cn.sl.ehub.console.model.resp.*;
import cn.sl.ehub.console.model.vo.*;
import cn.sl.ehub.service.req.AdjustSituationExcelRep;
import cn.sl.ehub.service.req.IndexOverviewBaseTableResp;
import cn.sl.ehub.service.req.IndexOverviewTableResp;
import cn.sl.ehub.service.resp.HistoryQueryDeviceMetricResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 历史查询service
 * @Author sl
 * @Date 2026-05-28
 */
public interface IHistoryQueryService {

    /**
     * 用户完成调节情况曲线图接口
     *
     * @param userAdjustmentGraphReq
     * @return
     */
    HistoryQueryGraphVO userAdjustmentGraph(UserAdjustmentGraphReq userAdjustmentGraphReq);

    /**
     * @description 用户完成调节情况曲线图接口-新
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    HistoryQueryGraphVO userAdjustmentGraphNew(NewUserAdjustmentGraphReq userAdjustmentGraphReq);

    /**
     * 用户完成调节情况图表
     *
     * @param userAdjustmentTableReq
     * @return
     */
    PageResultVO<HistoryQueryTableVO> userAdjustmentTable(UserAdjustmentTableReq userAdjustmentTableReq);

    /**
     * 设备运行情况曲线图
     *
     * @param deviceRunStatusReq
     * @return
     */
    List<LineDataGraphResp> deviceRunStatusChart(DeviceRunStatusReq deviceRunStatusReq);

    /**
     * 收益统计
     *
     * @param profitStatisticsReq
     * @return
     */
    ProfitStatisticsVO profitStatistics(ProfitStatisticsReq profitStatisticsReq);

    /**
     * 用户收益统计
     *
     * @param profitStatisticsReq
     * @return
     */
    UserProfitStatisticsVO userProfitStatistics(ProfitStatisticsReq profitStatisticsReq);

    /**
     * 查询汇总功率曲线
     *
     * @param aggregatorId
     * @param resourceTypeId
     * @param startDate
     * @param endDate
     * @return
     */
    IndexOverviewResp getTotalPowerChart(String aggregatorId, String resourceTypeId, String startDate, String endDate);

    /**
     * 查询出清价格
     *
     * @param profitStatisticsReq
     * @return
     */
    IndexOverviewResp getPrice(ProfitStatisticsReq profitStatisticsReq);


    /**
     * 查询出清价格表格
     *
     * @param profitStatisticsReq
     * @return
     */
    IndexOverviewTableResp getPriceTable(ProfitStatisticsReq profitStatisticsReq);

    /**
     * 出清价格导出
     *
     * @param profitStatisticsReq
     * @return
     */
    List<PriceExcelDateResp> getPriceExcel(ProfitStatisticsReq profitStatisticsReq);

    /**
     * 查询历史设备运行测点列表
     *
     * @return
     */
    List<HistoryQueryDeviceMetricResp> getMetricList();

    /**
     * 查询数据
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @return
     */
    List<HistoryProfitCalculationExcelResp> getProfitCalculation(String entId, String startDate, String endDate);

    /**
     * 查询数据
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @return
     */
    LinkedHashMap<String, List<HistoryProfitCalculationTimeExcelResp>> getProfitCalculationMap(String entId, String startDate, String endDate);

    /**
     * @description 调节效果excel导出
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    HistoryAdjustExcelResp exportAdjustSituationExcel(AdjustSituationExcelRep adjustSituationExcelRep);


    /**
     * 获取聚合商补招数据
     * @param req
     * @return
     */
    HistoryAdjustExcelResp exportBuZhaoUploadData(AdjustSituationExcelRep req);
}
