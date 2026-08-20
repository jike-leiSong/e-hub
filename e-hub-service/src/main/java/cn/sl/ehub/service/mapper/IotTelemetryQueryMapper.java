package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.dto.iot.IotDeviceSummaryPointResp;
import cn.sl.ehub.service.dto.iot.IotDeviceSummaryReq;
import cn.sl.ehub.service.dto.iot.IotTelemetryAggResp;
import cn.sl.ehub.service.dto.iot.IotTelemetryDataResp;
import cn.sl.ehub.service.dto.iot.IotTelemetryQueryReq;
import cn.sl.ehub.service.dto.iot.IotTelemetryRawQueryReq;
import cn.sl.ehub.service.dto.iot.IotTelemetryRawResp;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Date;

@Repository
public interface IotTelemetryQueryMapper {

    List<IotTelemetryDataResp> selectMinuteData(@Param("req") IotTelemetryQueryReq req);

    Long countMinuteData(@Param("req") IotTelemetryQueryReq req);

    List<IotTelemetryAggResp> selectMinuteAgg(@Param("req") IotTelemetryQueryReq req);

    Long countMinuteAgg(@Param("req") IotTelemetryQueryReq req);

    List<IotTelemetryRawResp> selectRawData(@Param("req") IotTelemetryRawQueryReq req);

    Long countRawData(@Param("req") IotTelemetryRawQueryReq req);

    List<IotDeviceSummaryPointResp> selectDeviceSummary(@Param("req") IotDeviceSummaryReq req);

    List<IotTelemetryDataResp> selectRunStatusData(@Param("aggregatorId") String aggregatorId,
                                                    @Param("entId") String entId,
                                                    @Param("deviceIds") List<Long> deviceIds,
                                                    @Param("deviceCodes") List<String> deviceCodes,
                                                    @Param("pointCodes") List<String> pointCodes,
                                                    @Param("startTime") Date startTime,
                                                    @Param("endTime") Date endTime);
}
