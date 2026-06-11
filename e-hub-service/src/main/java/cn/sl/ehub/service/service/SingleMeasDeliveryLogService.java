package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.SingleMeasDeliveryLogMapper;
import cn.sl.ehub.service.vo.SingleMeasDeliveryLog;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description: 单体量测数据上送
 * @Author sl
 * @Date 2026-05-28
 */
@Service
public class SingleMeasDeliveryLogService {

    @Resource
    private SingleMeasDeliveryLogMapper singleMeasDeliveryLogMapper;

    public void addLog(SingleMeasDeliveryLog log){
        singleMeasDeliveryLogMapper.insertSelective(log);
    }
}
