package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDateDeliveryChart;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 用户申报功率曲线
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorDateDeliveryChartMapper extends Mapper<AggregatorDateDeliveryChart> {

    /**
     * 批量添加数据
     *
     * @param aggregatorDateDeliveryChartList
     * @return
     */
    int batchInsert(List<AggregatorDateDeliveryChart> aggregatorDateDeliveryChartList);

    List<String> getPlanApplyEntNum(@Param("aggregatorId") String aggregatorId, @Param("date") String date);
}
