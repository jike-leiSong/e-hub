package cn.sl.ehub.service.service;

import cn.sl.ehub.service.mapper.PlanDeliveryLogMapper;
import cn.sl.ehub.service.vo.PlanDeliveryLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Service
public class PlanDeliveryLogService {

    @Resource
    private PlanDeliveryLogMapper planDeliveryLogMapper;

    public void addLog(PlanDeliveryLog log) {
        planDeliveryLogMapper.insertSelective(log);
    }

    public PlanDeliveryLog getLastedLog() {
        Example example = new Example(PlanDeliveryLog.class);
        example.setOrderByClause("create_time desc");
        List<PlanDeliveryLog> planDeliveryLog = planDeliveryLogMapper.selectByExampleAndRowBounds(example, new RowBounds(0, 1));
        if (CollectionUtils.isEmpty(planDeliveryLog)) {
            return new PlanDeliveryLog();
        } else {
            return planDeliveryLog.get(0);
        }
    }
}
