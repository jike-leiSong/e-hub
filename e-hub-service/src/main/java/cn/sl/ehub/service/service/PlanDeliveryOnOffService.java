package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.PlanDeliveryOnOffMapper;
import cn.sl.ehub.service.vo.PlanDeliveryOnOff;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: 联调时申报控制开关
 * @Author sl
 * @Date 2026-05-28
 */

@Service
public class PlanDeliveryOnOffService {

    @Resource
    private PlanDeliveryOnOffMapper planDeliveryOnOffMapper;

    public Boolean getMark() {

        List<PlanDeliveryOnOff> planDeliveryOnOffs = planDeliveryOnOffMapper.selectAll();
        if (CollectionUtils.isEmpty(planDeliveryOnOffs)) {
            return true;
        }
        return planDeliveryOnOffs.get(0).getMark();
    }

}
