package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDeviceDateBaseLineLoadChart;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 设备基线负荷曲线Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorDeviceDateBaseLineLoadChartMapper extends Mapper<AggregatorDeviceDateBaseLineLoadChart> {

    /**
     * 查询数据
     *
     * @param deviceBaseIdList
     * @param dateList
     * @return
     */
    List<AggregatorDeviceDateBaseLineLoadChart> getAggregatorDeviceDateBaseLineLoadChartList(@Param("deviceBaseIdList") List<String> deviceBaseIdList, @Param("dateList") List<String> dateList);
}
