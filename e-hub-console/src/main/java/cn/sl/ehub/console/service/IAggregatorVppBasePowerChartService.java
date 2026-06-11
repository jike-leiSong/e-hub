package cn.sl.ehub.console.service;

import cn.sl.ehub.service.vo.AggregatorEntDevice;
import cn.sl.ehub.service.vo.AggregatorVppBasePowerChart;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface IAggregatorVppBasePowerChartService {

    /**
     * 根据电站查询数据
     *
     * @param systemCode
     * @return
     */
    List<AggregatorVppBasePowerChart> getVppBasePowerBySystemCode(String systemCode);

    /**
     * 根据电站查询数据
     *
     * @param entId
     * @return
     */
    List<AggregatorVppBasePowerChart> getVppBasePowerByEntId(String entId);
}
