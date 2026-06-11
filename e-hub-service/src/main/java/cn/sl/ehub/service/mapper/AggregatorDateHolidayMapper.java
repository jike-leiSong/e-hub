package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDateHoliday;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 节假日Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorDateHolidayMapper extends Mapper<AggregatorDateHoliday> {

    /**
     * 批量添加数据
     *
     * @param aggregatorDateHolidayList
     * @return
     */
    int batchInsert(List<AggregatorDateHoliday> aggregatorDateHolidayList);


}
