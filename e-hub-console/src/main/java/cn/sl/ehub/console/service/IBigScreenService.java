package cn.sl.ehub.console.service;

import cn.sl.ehub.service.resp.BigScreenWeekDayAverageChartResp;
import cn.sl.ehub.service.vo.*;

import java.util.List;

/**
 * 大屏接口Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IBigScreenService {

    /**
     * 全局概况
     *
     * @return
     */
    BigScreenGeneralSituation getAll();

    /**
     * 查询数据
     *
     * @return
     */
    List<BigScreenWeekDate> getBigScreenWeekDateList();

    /**
     * 查询数据
     *
     * @return
     */
    List<BigScreenEntProfit> getBigScreenEntProfitList();

    /**
     * 查询数据
     *
     * @return
     */
    BigScreenWeekDayAverageChartResp getBigScreenWeekDayAverageChart();

    /**
     * 查询数据
     *
     * @return
     */
    List<BigScreenDayLog> getDayLog();

    /**
     * 查询数据
     *
     * @return
     */
    List<BigScreenEntTodayRate> getBigScreenTodayRate();
}
