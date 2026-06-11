package cn.sl.ehub.service.mapper;

import java.util.Date;
import java.util.List;

import cn.sl.ehub.service.vo.PeakPlanDeliveryLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import tk.mybatis.mapper.common.Mapper;

/**
 * 调峰计划申报电网上送日志Mapper
 *
 * @author sl
 * @date 2026-05-28
 */
@Repository
public interface PeakPlanDeliveryLogMapper extends Mapper<PeakPlanDeliveryLog> {

    /**
     * 批量插入日志
     *
     * @param logList 日志列表
     * @return 影响行数
     */
    int batchInsert(List<PeakPlanDeliveryLog> logList);

    /**
     * 根据聚合商ID、资源ID和日期查询日志
     *
     * @param aggregatorId 聚合商ID
     * @param sourceId     资源ID
     * @param dataDate     数据日期
     * @return 日志列表
     */
    List<PeakPlanDeliveryLog> selectByAggregatorAndSourceAndDate(@Param("aggregatorId") String aggregatorId,
            @Param("sourceId") String sourceId,
            @Param("dataDate") Date dataDate);
}
