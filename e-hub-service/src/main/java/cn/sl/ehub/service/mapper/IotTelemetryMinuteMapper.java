package cn.sl.ehub.service.mapper;

import cn.sl.ehub.common.vo.DataResp;
import org.apache.ibatis.annotations.Param;
import cn.sl.ehub.service.vo.IotTelemetryMinute;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

@Repository
public interface IotTelemetryMinuteMapper extends Mapper<IotTelemetryMinute> {

    int upsertMinute(IotTelemetryMinute record);

    List<DataResp> sumPointValueByMinute(@Param("aggregatorId") String aggregatorId,
                                         @Param("deviceIds") List<Long> deviceIds,
                                         @Param("deviceCodes") List<String> deviceCodes,
                                         @Param("pointCode") String pointCode,
                                         @Param("startTime") Date startTime,
                                         @Param("endTime") Date endTime);
}
