package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDateIssueChart;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 调度下发功率曲线
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorDateIssueChartMapper extends Mapper<AggregatorDateIssueChart> {

    /**
     * 批量添加数据
     *
     * @param aggregatorDateIssueChartList
     * @return
     */
    int batchInsert(List<AggregatorDateIssueChart> aggregatorDateIssueChartList);
}
