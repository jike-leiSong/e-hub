package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorEntDateDeviceStartStopPlan;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 设备启停计划Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorEntDateDeviceStartStopPlanMapper extends Mapper<AggregatorEntDateDeviceStartStopPlan> {

    /**
     * 批量添加数据
     *
     * @param aggregatorEntDateDeviceStartStopPlanList
     * @return
     */
    int batchInsert(List<AggregatorEntDateDeviceStartStopPlan> aggregatorEntDateDeviceStartStopPlanList);
}
