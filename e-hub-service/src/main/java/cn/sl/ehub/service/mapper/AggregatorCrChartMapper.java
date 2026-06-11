package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorCrChart;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface AggregatorCrChartMapper extends Mapper<AggregatorCrChart> {

    /**
     * 批量添加数据
     *
     * @param aggregatorCrChartList
     * @return
     */
    int batchInsert(List<AggregatorCrChart> aggregatorCrChartList);
}
