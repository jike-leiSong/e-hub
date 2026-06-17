package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.service.TripartTanShanService;
import cn.sl.ehub.service.vo.TripartDataSynchronLog;
import org.springframework.stereotype.Service;

@Service
public class TripartTanShanServiceImpl implements TripartTanShanService {

    @Override
    public ResultVO<String> dataSynchronizeRetry(TripartDataSynchronLog log) {
        return ResultVO.success("TripartTanShanService retry is not configured");
    }

    @Override
    public ResultVO<String> dataSynchronizeRetryWithoutLog(String createTime) {
        return ResultVO.success("TripartTanShanService retry is not configured");
    }
}
