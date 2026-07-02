package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.vo.AggregatorSingleModelData;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface AggregatorSingleModelDataMapper extends Mapper<AggregatorSingleModelData> {

    List<AggregatorSingleModelData> selectModelList(@Param("aggregatorId") String aggregatorId,
                                                    @Param("entId") String entId,
                                                    @Param("resourceTypeId") String resourceTypeId,
                                                    @Param("energyStationCode") String energyStationCode,
                                                    @Param("energyStation") String energyStation,
                                                    @Param("energyStationCodes") List<String> energyStationCodes);

    AggregatorSingleModelData selectModelById(@Param("id") Integer id);

    int countByEnergyStationCode(@Param("energyStationCode") String energyStationCode,
                                 @Param("excludeId") Integer excludeId);
}
