package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.resp.AggregatorDeviceChartResp;
import cn.sl.ehub.service.resp.IndexOverviewResp;

import java.util.List;

/**
 * 调峰辅助服务Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IPeakShavingAuxiliaryService {

    /**
     * 查询数据
     *
     * @param simulate
     * @param deviceBaseId
     * @param startDate
     * @param endDate
     * @param historyStatus
     * @return
     */
    AggregatorDeviceChartResp getPowerChartResp(String simulate, String deviceBaseId, String startDate, String endDate, boolean historyStatus);

    /**
     * 查询数据
     *
     * @param simulate
     * @param entId
     * @param startDate
     * @param endDate
     * @return
     */
    List<AggregatorDeviceChartResp> getNowPowerChartResp(String simulate, String entId, String startDate, String endDate);

    /**
     * 查询数据
     *
     * @param simulate
     * @param entId
     * @param startDate
     * @param endDate
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageResultVO<AggregatorDeviceChartResp> getNowPowerChartRespPage(String simulate, String entId, String startDate, String endDate, Integer pageNo, Integer pageSize);
}
