package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.req.UpdateEntDeviceReq;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface AggregatorEntDeviceMapper extends Mapper<AggregatorEntDevice> {

    int updateDeviceInfo(UpdateEntDeviceReq device);

    /**
     * 批量添加数据
     *
     * @param aggregatorEntDeviceList
     * @return
     */
    int batchInsert(List<AggregatorEntDevice> aggregatorEntDeviceList);

    List<AggregatorEntDevice> getOnlineAggregatorEntDeviceList();

    List<AggregatorEntDevice> getModelAggregatorEntDeviceList();

    List<AggregatorEntDevice> getOnlineEntDevicesByAggregatorId(@Param("aggregatorId") String aggregatorId, @Param("stationIds")List<String> stationIds);
    List<AggregatorEntDevice> getModelAggregatorEntDeviceListByAggregatorId(String aggregatorId);
}