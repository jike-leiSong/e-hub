package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorEntDeviceIotLog;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 设备信息
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorEntDeviceIotLogMapper extends Mapper<AggregatorEntDeviceIotLog> {

    /**
     * 批量添加数据
     *
     * @param aggregatorEntDeviceIotLogList
     * @return
     */
    int batchInsert(List<AggregatorEntDeviceIotLog> aggregatorEntDeviceIotLogList);
}
