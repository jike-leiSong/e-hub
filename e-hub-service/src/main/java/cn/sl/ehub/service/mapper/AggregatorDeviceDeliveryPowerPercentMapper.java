package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorDeviceDeliveryPowerPercent;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 设备申报功率比例Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorDeviceDeliveryPowerPercentMapper extends Mapper<AggregatorDeviceDeliveryPowerPercent> {

    /**
     * 批量添加数据
     *
     * @param aggregatorDeviceDeliveryPowerPercentList
     * @return
     */
    int batchInsert(List<AggregatorDeviceDeliveryPowerPercent> aggregatorDeviceDeliveryPowerPercentList);
}
