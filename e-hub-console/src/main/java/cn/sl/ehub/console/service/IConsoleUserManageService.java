package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.ConsoleUserPageReq;
import cn.sl.ehub.console.model.req.ConsoleUserUpsertReq;
import cn.sl.ehub.console.model.req.UserRoleSaveReq;
import cn.sl.ehub.console.model.req.UserStatusUpdateReq;
import cn.sl.ehub.console.model.resp.ConsoleUserPageItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;

import java.util.List;

public interface IConsoleUserManageService {

    PageResultVO<ConsoleUserPageItemResp> page(ConsoleUserPageReq req);

    ConsoleUserPageItemResp create(ConsoleUserUpsertReq req);

    ConsoleUserPageItemResp update(String userId, ConsoleUserUpsertReq req);

    Boolean updateStatus(String userId, UserStatusUpdateReq req);

    Boolean saveRoles(String userId, UserRoleSaveReq req);

    List<ConsoleUserPageItemResp> listByTenantId(String tenantId);
}
