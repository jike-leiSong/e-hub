package cn.sl.ehub.console.service;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.TripartDataSynchronLog;

public interface ITripartDataSynchronService {

    TripartDataSynchronLog getLogByStatus(String status);

    TripartDataSynchronLog getLogByCreateTime(String createTime);

    int updateLogById(TripartDataSynchronLog log);

    void addLog(TripartDataSynchronLog log);

    ResultVO<String> synchronRetry(String createTime);

}
