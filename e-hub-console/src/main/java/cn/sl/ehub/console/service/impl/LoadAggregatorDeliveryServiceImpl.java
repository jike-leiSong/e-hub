package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.service.LoadAggregatorDeliveryService;
import cn.sl.ehub.common.vo.ResultVO;
import org.springframework.stereotype.Service;

@Service
public class LoadAggregatorDeliveryServiceImpl implements LoadAggregatorDeliveryService {

    @Override
    public ResultVO<String> getSuccess() {
        return ResultVO.success("success");
    }
}
