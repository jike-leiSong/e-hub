package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorDeviceDateProfit;
import cn.sl.ehub.service.vo.AggregatorEntDateAdjust;

import java.util.List;
import java.util.Map;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface IAggregatorEntDateAdjustService {

    /**
     * 删除数据
     *
     * @param entId
     * @param date
     * @return
     */
    int delete(String entId, String date);



    int delete(List<String> entIdList, String date);


    /**
     *
     * @param aggregatorEntDateAdjustList
     * @return
     */
    int batchInsert(List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList);


    /**
     *
     * @param entId
     * @param date
     * @param aggregatorEntDateAdjustList
     * @return
     */
    int save(String entId, String date, List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList);


    /**
     * 保存数据
     *
     * @param
     * @param date
     * @param aggregatorEntDateAdjustList
     * @return
     */
    int save(String aggregatorId, String typeId,String date, List<AggregatorEntDateAdjust> aggregatorEntDateAdjustList);




    /**
     * @description 根据企业+资源类型+时间区间查询
     * @param 
     * @return 
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorEntDateAdjust> getEntAdjust(String entId,String startDate,String endDate,String sourceTypeId);
    /**
     * @description 根据企业id集合+资源类型+时间区间查询
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    Map<String, List<AggregatorEntDateAdjust>> getMoreEntAdjust(List<String> entIdList, String startDate, String endDate, String sourceTypeId);


    List<AggregatorEntDateAdjust> getAggregatorEntDateAdjustList(String aggregatorId, String date);

}
