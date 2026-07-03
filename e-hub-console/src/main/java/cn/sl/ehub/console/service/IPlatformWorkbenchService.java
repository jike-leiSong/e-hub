package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.resp.OperationLogSimpleResp;
import cn.sl.ehub.console.model.resp.WorkbenchSummaryResp;
import cn.sl.ehub.console.model.resp.WorkbenchTodoResp;

import java.util.List;

public interface IPlatformWorkbenchService {

    WorkbenchSummaryResp summary();

    List<WorkbenchTodoResp> todos();

    List<OperationLogSimpleResp> recentLogs(Integer limit);
}
