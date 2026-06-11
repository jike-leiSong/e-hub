package cn.sl.ehub.service.service;

import cn.sl.ehub.service.vo.IotCmdSetLog;

/**
 * IOT指令下发日志Service
 * @author sl
 * @date 2026-06-04
 */
public interface IotCmdSetLogService {

    int save(IotCmdSetLog iotCmdSetLog);

    IotCmdSetLog getByRequestId(String requestId);
}
