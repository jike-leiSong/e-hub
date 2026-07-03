package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.TenantPageReq;
import cn.sl.ehub.console.model.req.TenantProductSaveReq;
import cn.sl.ehub.console.model.req.TenantStatusUpdateReq;
import cn.sl.ehub.console.model.req.TenantUpsertReq;
import cn.sl.ehub.console.model.resp.TenantDetailResp;
import cn.sl.ehub.console.model.resp.TenantPageItemResp;
import cn.sl.ehub.console.model.resp.TenantProductResp;
import cn.sl.ehub.console.model.vo.PageResultVO;

import java.util.List;

public interface ITenantService {

    PageResultVO<TenantPageItemResp> page(TenantPageReq req);

    TenantDetailResp detail(String tenantId);

    TenantDetailResp create(TenantUpsertReq req);

    TenantDetailResp update(String tenantId, TenantUpsertReq req);

    Boolean updateStatus(String tenantId, TenantStatusUpdateReq req);

    List<TenantProductResp> products(String tenantId);

    Boolean saveProducts(String tenantId, TenantProductSaveReq req);
}
