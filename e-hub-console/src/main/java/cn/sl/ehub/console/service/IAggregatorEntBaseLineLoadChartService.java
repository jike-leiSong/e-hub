package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorDeviceDateBaseLineLoadChart;
import cn.sl.ehub.service.vo.AggregatorEntBaseLineLoadChart;

import java.util.List;
import java.util.Map;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface IAggregatorEntBaseLineLoadChartService {

    /**
     * 批量删除数据
     *
     * @param entIdList
     * @param date
     * @return
     */
    void batchDelete(List<String> entIdList, String date);


    void batchDeleteByTypeId(String aggregatorId, String typeId, String date);

    /**
     * 批量新增数据
     *
     * @param aggregatorEntBaseLineLoadChartList
     * @return
     */
    int batchInsert(List<AggregatorEntBaseLineLoadChart> aggregatorEntBaseLineLoadChartList);


    /**
     * @description 查询企业基线
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorEntBaseLineLoadChart> getEntBaseLine(String entId, String resourceTypeId, String startDate, String endDate);

    /**
     * @description 查询多个企业基线
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    Map<String,List<AggregatorEntBaseLineLoadChart>> getMoreEntBaseLine(List<String> entIdList, String resourceTypeId, String startDate, String endDate);


    /**
     * @description 查询企业基线
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorEntBaseLineLoadChart> getEntBaseLineBySystemCode(String systemCode, String date);
}
