package cn.sl.ehub.upstream.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.sl.ehub.service.mapper.PeakPlanDeliveryLogMapper;
import cn.sl.ehub.service.vo.PeakPlanDeliveryLog;
import lombok.extern.slf4j.Slf4j;

/**
 * 调峰计划申报电网上送日志Service
 *
 * @author sl
 * @date 2026-05-28
 */
@Slf4j
@Service
public class PeakPlanDeliveryLogService {

    @Resource
    private PeakPlanDeliveryLogMapper peakPlanDeliveryLogMapper;

    /**
     * 添加日志
     *
     * @param deliveryLog 日志对象
     */
    public void addLog(PeakPlanDeliveryLog deliveryLog) {
        try {
            if (deliveryLog.getCreateTime() == null) {
                deliveryLog.setCreateTime(new Date());
            }
            peakPlanDeliveryLogMapper.insertSelective(deliveryLog);
        } catch (Exception e) {
            log.error("添加调峰计划申报上送日志异常", e);
        }
    }

    /**
     * 批量添加日志
     *
     * @param logList 日志列表
     */
    public void batchAddLog(List<PeakPlanDeliveryLog> logList) {
        try {
            if (logList != null && !logList.isEmpty()) {
                for (PeakPlanDeliveryLog deliveryLog : logList) {
                    if (deliveryLog.getCreateTime() == null) {
                        deliveryLog.setCreateTime(new Date());
                    }
                }
                peakPlanDeliveryLogMapper.batchInsert(logList);
            }
        } catch (Exception e) {
            log.error("批量添加调峰计划申报上送日志异常", e);
        }
    }

    /**
     * 查询日志
     *
     * @param aggregatorId 聚合商ID
     * @param sourceId     资源ID
     * @param dataDate     数据日期
     * @return 日志列表
     */
    public List<PeakPlanDeliveryLog> queryLog(String aggregatorId, String sourceId, Date dataDate) {
        try {
            return peakPlanDeliveryLogMapper.selectByAggregatorAndSourceAndDate(aggregatorId, sourceId, dataDate);
        } catch (Exception e) {
            log.error("查询调峰计划申报上送日志异常", e);
            return null;
        }
    }
}
