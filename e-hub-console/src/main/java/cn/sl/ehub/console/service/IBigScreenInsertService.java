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
public interface IBigScreenInsertService {

    /**
     * 添加数据
     *
     * @return
     */
    Boolean insertAllAndEntProfit();

    /**
     * 全局概况
     *
     * @return
     */
    Boolean insertAll();

    /**
     * 查询数据
     *
     * @return
     */
    Boolean insertBigScreenEntProfit();

    /**
     * 查询数据
     *
     * @param date
     * @return
     */
    Boolean insertBigScreenTodayRate(String date);

    /**
     * 查询数据
     *
     * @param startDate
     * @param endDate
     * @return
     */
    Boolean insertBigScreenWeekDayAverageChart(String startDate, String endDate);
}
