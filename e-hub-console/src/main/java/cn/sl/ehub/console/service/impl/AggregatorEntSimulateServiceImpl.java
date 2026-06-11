package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.service.IAggregatorEntSimulateService;
import cn.sl.ehub.service.mapper.AggregatorEntSimulateMapper;
import cn.sl.ehub.service.vo.AggregatorEntSimulate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 企业仿真配置ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorEntSimulateServiceImpl implements IAggregatorEntSimulateService {

    private final AggregatorEntSimulateMapper aggregatorEntSimulateMapper;

    @Override
    public List<AggregatorEntSimulate> getAggregatorEntSimulateList() {
        return aggregatorEntSimulateMapper.selectAll();
    }
}
