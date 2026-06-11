package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.AggregatorEntDeviceMapper;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: 聚合商、企业、设备
 * @Author sl
 * @Date 2026-05-28
 */
@Service
public class AggregatorEntDeviceService {

    @Resource
    private AggregatorEntDeviceMapper aggregatorEntDeviceMapper;

    /**
     * 获取子企业下所有的设备
     * @param aggregatorId
     * @param entId
     * @return
     */
    public List<AggregatorEntDevice> getEntDeviceList(String aggregatorId, String entId) {
        Example example = new Example(AggregatorEntDevice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("aggregatorId", aggregatorId);
        criteria.andEqualTo("entId", entId);
        return aggregatorEntDeviceMapper.selectByExample(example);
    }

    /**
     * 获取所有在线的设备 status=1
     * @return
     */
    public List<AggregatorEntDevice> getOnlineAggregatorEntDeviceList() {
        return aggregatorEntDeviceMapper.getOnlineAggregatorEntDeviceList();
    }

    /**
     * 获取聚合商所有在线的设备 status=1
     * @return
     */
    public List<AggregatorEntDevice> getOnlineEntDeviceListByAggregatorId(String aggregatorId, List<String> stationIds) {
        return aggregatorEntDeviceMapper.getOnlineEntDevicesByAggregatorId(aggregatorId, stationIds);
    }

    /**
     * 获取所有模型的设备 modelFlag=1
     * 上传模型时，设备不能上送总加等数据，配置时需要用modelFlag和status区分
     * @return
     */
    public List<AggregatorEntDevice> getModelAggregatorEntDeviceList() {
        return aggregatorEntDeviceMapper.getModelAggregatorEntDeviceList();
    }

    /**
     * 获取聚合商所有模型的设备 modelFlag=1
     * 上传模型时，设备不能上送总加等数据，配置时需要用modelFlag和status区分
     * @return
     */
    public List<AggregatorEntDevice> getModelAggregatorEntDeviceListByAggregatorId(String aggregatorId) {
        return aggregatorEntDeviceMapper.getModelAggregatorEntDeviceListByAggregatorId(aggregatorId);
    }

}
