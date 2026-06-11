package cn.sl.ehub.console.service;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.TripartDataSynchronLog;

/**
 * 檀山第三方数据同步服务
 * @author sl
 * @date 2026-06-04
 */
public interface TripartTanShanService {

    /**
     * 数据同步重试
     * @param log 同步日志
     * @return 结果
     */
    ResultVO<String> dataSynchronizeRetry(TripartDataSynchronLog log);

    /**
     * 无日志情况下的数据同步重试
     * @param createTime 创建时间
     * @return 结果
     */
    ResultVO<String> dataSynchronizeRetryWithoutLog(String createTime);
}
