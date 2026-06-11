package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.SingleModelDeliveryLogMapper;
import cn.sl.ehub.service.vo.SingleModelDeliveryLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description: 单体模型数据下发
 * @Author sl
 * @Date 2026-05-28
 */

@Service
public class SingleModelDeliveryLogService {

    @Resource
    private SingleModelDeliveryLogMapper singleModelDeliveryLogMapper;

    @Async
    public void addLog(SingleModelDeliveryLog log){
        singleModelDeliveryLogMapper.insertSelective(log);
    }
}
