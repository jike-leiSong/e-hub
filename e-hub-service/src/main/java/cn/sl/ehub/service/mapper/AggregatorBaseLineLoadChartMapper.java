package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorBaseLineLoadChart;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface AggregatorBaseLineLoadChartMapper extends Mapper<AggregatorBaseLineLoadChart> {
    /**
     * 批量添加数据
     *
     * @param aggregatorDateIssueChartList
     * @return
     */
    int batchInsert(List<AggregatorBaseLineLoadChart> aggregatorDateIssueChartList);
}
