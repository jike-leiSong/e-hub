package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.vo.AggregatorDevicesVO;
import cn.sl.ehub.console.model.vo.UserInfoAndDevice;
import cn.sl.ehub.service.resp.AggregatorEntDeviceIotLogResp;
import cn.sl.ehub.service.resp.EntUserDeviceTodayChartResp;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 曲线图Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface ITodayService {

    /**
     * 查询设备曲线
     *
     * @param deviceBaseId
     * @return
     */
    EntUserDeviceTodayChartResp getEntUserDeviceTodayChartResp(String deviceBaseId);


    EntUserDeviceTodayChartResp getDeviceTreeTodayChartResp(String deviceBaseId,String energyStationcode,String systemCode);
    /**
     * 查询执行记录
     *
     * @param entId
     * @param stationId
     * @param resourceTypeId
     * @param deviceBaseId
     * @return
     */
    List<AggregatorEntDeviceIotLogResp> getIotLog(String entId, String stationId, String resourceTypeId, String deviceBaseId);


    List<UserInfoAndDevice> getDevices(String aggregatorId, String resourceType);
}
