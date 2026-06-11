package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDeviceDateDeliveryChart;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 申报设备功率曲线Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorDeviceDateDeliveryChartMapper extends Mapper<AggregatorDeviceDateDeliveryChart> {

    /**
     * 批量添加数据
     *
     * @param aggregatorDeviceDateDeliveryChartList
     * @return
     */
    int batchInsert(List<AggregatorDeviceDateDeliveryChart> aggregatorDeviceDateDeliveryChartList);

    List<String> getApplyResourceType(@Param("aggregatorId") String aggregatorId, @Param("date") String date);
}
