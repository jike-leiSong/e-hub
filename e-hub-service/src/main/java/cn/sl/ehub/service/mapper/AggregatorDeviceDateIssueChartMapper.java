package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDeviceDateIssueChart;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 调度下发设备功率曲线Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorDeviceDateIssueChartMapper extends Mapper<AggregatorDeviceDateIssueChart> {

    /**
     * 批量添加数据
     *
     * @param aggregatorDeviceDateIssueChartList
     * @return
     */
    int batchInsert(List<AggregatorDeviceDateIssueChart> aggregatorDeviceDateIssueChartList);
}
