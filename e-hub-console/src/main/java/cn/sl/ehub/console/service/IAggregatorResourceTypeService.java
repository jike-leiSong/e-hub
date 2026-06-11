package cn.sl.ehub.console.service;

import cn.sl.ehub.service.resp.AggregatorResourceTypeResp;
import cn.sl.ehub.service.vo.AggregatorResourceType;

import java.util.List;

/**
 * 资源类型Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorResourceTypeService {

    /**
     * 查询资源类型
     *
     * @return
     */
    List<AggregatorResourceType> getAggregatorResourceTypeList();

    /**
     * 查询聚合商拥有的资源类型
     *
     * @return
     */
    List<AggregatorResourceType> getAggregatorShowResourceTypeList();

    /**
     * @description 根据聚合商id查询所有资源类型
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorResourceType> getAggregatorResourceTypeListByAggregatorId(String aggregatorId);

    /**
     * @param
     * @return
     * @description 根据聚合商id查询所有资源类型
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorResourceType> getAggregatorResourceTypeListByAggregatorId(String aggregatorId, String entId);


    /**
     * @description 根据聚合商id查询已有的资源类型
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    List<AggregatorResourceType> getAggregatorDisplayResourceTypeList(String aggregatorId);

    /**
     * @description 根据主键id查询
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    AggregatorResourceType getTypeById(String id);


}
