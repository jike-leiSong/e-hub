package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorAvgRtChart;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface AggregatorAvgRtChartMapper extends Mapper<AggregatorAvgRtChart> {

    /**
     * 批量添加数据
     *
     * @param aggregatorAvgRtChartList
     * @return
     */
    int batchInsert(List<AggregatorAvgRtChart> aggregatorAvgRtChartList);
}
