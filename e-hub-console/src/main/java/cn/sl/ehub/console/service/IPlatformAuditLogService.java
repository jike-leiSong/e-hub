package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.OperationLogPageReq;
import cn.sl.ehub.console.model.resp.OperationLogPageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;

public interface IPlatformAuditLogService {

    PageResultVO<OperationLogPageItemResp> logs(OperationLogPageReq req);

    void record(String bizType, String bizId, String action, Object beforeValue, Object afterValue, String result, String errorMsg);
}
