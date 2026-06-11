package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorEntDeviceApplyPlan;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 设备申报计划Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorEntDeviceApplyPlanMapper extends Mapper<AggregatorEntDeviceApplyPlan> {

    /**
     * 批量添加数据
     *
     * @param planList
     * @return
     */
    int batchInsert(List<AggregatorEntDeviceApplyPlan> planList);
}
