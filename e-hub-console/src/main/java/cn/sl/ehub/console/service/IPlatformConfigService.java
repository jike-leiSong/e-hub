package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.ConfigItemPageReq;
import cn.sl.ehub.console.model.req.ConfigItemUpsertReq;
import cn.sl.ehub.console.model.resp.ConfigItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;

public interface IPlatformConfigService {

    PageResultVO<ConfigItemResp> items(ConfigItemPageReq req);

    ConfigItemResp create(ConfigItemUpsertReq req);

    ConfigItemResp update(Long id, ConfigItemUpsertReq req);
}
