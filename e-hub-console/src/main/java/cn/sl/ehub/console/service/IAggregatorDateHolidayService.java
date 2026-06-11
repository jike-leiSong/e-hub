package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDateHoliday;

import java.util.List;

/**
 * 节假日Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorDateHolidayService {

    /**
     * 查询节假时间范围
     *
     * @param date
     * @param flag 是否包含假期前的最后一个工作日
     * @return
     */
    List<String> getApplyDateList(String date, Boolean flag);

    /**
     * 查询节假时间是否存在
     *
     * @param date
     * @return
     */
    Boolean getApplyDateCheck(String date);

    /**
     * 查询节假时间是否存在(法定节假日除外)
     *
     * @param date
     * @return
     */
    Boolean getWeekend(String date);
    
    /**
     * 查询法定节假日下数据
     * @author sl
     * @date 2026-05-28
     * @param date 
     * @return java.util.List<cn.sl.ehub.upstream.vo.AggregatorDateHoliday>
     */
    List<AggregatorDateHoliday> getDateList(String date);
    /**
     * 通过年份和节假日标识取出符合条件的日期
     * @author sl
     * @date 2026-05-28
     * @param year
     * @param legal
     * @return java.util.List<java.lang.String>
     */
    List<String> getLegalHoliday(String year,String legal);


    /**
     * @description 初始化假期
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    Boolean initHoliday();
}
