package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.TotalDeliveryLogMapper;
import cn.sl.ehub.service.vo.TotalDeliveryLog;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: 总加数据上送接口
 * @Author sl
 * @Date 2026-05-28
 */

@Service
public class TotalDeliveryLogService {

    @Resource
    private TotalDeliveryLogMapper totalDeliveryLogMapper;

    public void addLog(TotalDeliveryLog log) {
        totalDeliveryLogMapper.insertSelective(log);
    }

    public List<TotalDeliveryLog> getLogsByCreateTimeAsc(String beginTime, String endTime) {
        return totalDeliveryLogMapper.selectLogsByCreateTimeAsc(beginTime, endTime);
    }

    public List<TotalDeliveryLog> getLogsByCreateTimeDesc(String beginTime, String endTime) {
        return totalDeliveryLogMapper.selectLogsByCreateTimeDesc(beginTime, endTime);
    }

    public List<TotalDeliveryLog> getLogsByDeliveryTimeAsc(String beginTime, String endTime) {
        return totalDeliveryLogMapper.selectLogsByDeliveryTimeAsc(beginTime, endTime);
    }

    public List<TotalDeliveryLog> getLogsByDeliveryTimeDesc(String beginTime, String endTime) {
        return totalDeliveryLogMapper.selectLogsByDeliveryTimeDesc(beginTime, endTime);
    }


}
