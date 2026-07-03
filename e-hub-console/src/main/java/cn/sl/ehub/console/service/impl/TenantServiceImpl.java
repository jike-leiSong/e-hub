package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.auth.ConsoleProductService;
import cn.sl.ehub.console.model.req.TenantPageReq;
import cn.sl.ehub.console.model.req.TenantProductItemReq;
import cn.sl.ehub.console.model.req.TenantProductSaveReq;
import cn.sl.ehub.console.model.req.TenantStatusUpdateReq;
import cn.sl.ehub.console.model.req.TenantUpsertReq;
import cn.sl.ehub.console.model.resp.ConsoleUserPageItemResp;
import cn.sl.ehub.console.model.resp.TenantDetailResp;
import cn.sl.ehub.console.model.resp.TenantPageItemResp;
import cn.sl.ehub.console.model.resp.TenantProductResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IConsoleUserManageService;
import cn.sl.ehub.console.service.IPlatformAuditLogService;
import cn.sl.ehub.console.service.ITenantService;
import cn.sl.ehub.service.mapper.ConsoleCustomerProductMapper;
import cn.sl.ehub.service.mapper.ConsoleTenantMapper;
import cn.sl.ehub.service.mapper.ConsoleTenantProductMapper;
import cn.sl.ehub.service.mapper.ConsoleUserMapper;
import cn.sl.ehub.service.vo.ConsoleCustomerProduct;
import cn.sl.ehub.service.vo.ConsoleTenant;
import cn.sl.ehub.service.vo.ConsoleTenantProduct;
import cn.sl.ehub.service.vo.ConsoleUser;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements ITenantService {

    private static final Set<String> VALID_TENANT_TYPES = new HashSet<>(Arrays.asList("PLATFORM", "AGGREGATOR", "ENT", "ACCOUNT"));

    private final ConsoleTenantMapper consoleTenantMapper;
    private final ConsoleTenantProductMapper consoleTenantProductMapper;
    private final ConsoleCustomerProductMapper consoleCustomerProductMapper;
    private final ConsoleUserMapper consoleUserMapper;
    private final ConsoleProductService consoleProductService;
    private final IConsoleUserManageService consoleUserManageService;
    private final IPlatformAuditLogService platformAuditLogService;

    @Override
    public PageResultVO<TenantPageItemResp> page(TenantPageReq req) {
        Integer pageIndex = req.getPageIndex() == null || req.getPageIndex() < 1 ? 1 : req.getPageIndex();
        Integer pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 20 : req.getPageSize();
        PageHelper.startPage(pageIndex, pageSize);
        List<ConsoleTenant> list = consoleTenantMapper.page(
                StringUtils.trimToNull(req.getKeyword()),
                StringUtils.trimToNull(req.getTenantType()),
                req.getStatus(),
                StringUtils.trimToNull(req.getProductCode())
        );
        List<TenantPageItemResp> respList = enrichPageItems(list);
        PageInfo<ConsoleTenant> pageInfo = new PageInfo<>(list);
        PageResultVO<TenantPageItemResp> page = new PageResultVO<>();
        page.setList(respList);
        page.setTotal((int) pageInfo.getTotal());
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public TenantDetailResp detail(String tenantId) {
        ConsoleTenant tenant = requireTenant(tenantId);
        TenantDetailResp resp = new TenantDetailResp();
        resp.setTenantId(tenant.getTenantId());
        resp.setTenantName(tenant.getTenantName());
        resp.setTenantType(tenant.getTenantType());
        resp.setStatus(tenant.getStatus());
        resp.setAggregatorId(tenant.getAggregatorId());
        resp.setEntId(tenant.getEntId());
        resp.setOwnerUserId(tenant.getOwnerUserId());
        ConsoleUser owner = StringUtils.isBlank(tenant.getOwnerUserId()) ? null : consoleUserMapper.getByUserId(tenant.getOwnerUserId());
        resp.setOwnerDisplayName(owner == null ? null : owner.getDisplayName());
        resp.setContactName(tenant.getContactName());
        resp.setContactPhone(tenant.getContactPhone());
        resp.setRemark(tenant.getRemark());
        resp.setProducts(products(tenantId));
        resp.setUsers(consoleUserManageService.listByTenantId(tenantId));
        return resp;
    }

    @Override
    public TenantDetailResp create(TenantUpsertReq req) {
        validate(req);
        String tenantId = generateTenantId(req);
        if (consoleTenantMapper.getByTenantId(tenantId) != null) {
            throw new BaseException(StatusCode.C.getCode(), "租户ID已存在");
        }
        if (safeCount(consoleTenantMapper.countByTenantName(req.getTenantName(), null)) > 0) {
            throw new BaseException(StatusCode.C.getCode(), "租户名称已存在");
        }
        ConsoleUser owner = resolveOwner(req.getOwnerUserId());
        String now = DateUtils.getTime();
        ConsoleTenant entity = new ConsoleTenant();
        entity.setTenantId(tenantId);
        entity.setTenantName(StringUtils.trim(req.getTenantName()));
        entity.setTenantType(StringUtils.upperCase(StringUtils.trim(req.getTenantType())));
        entity.setStatus(1);
        entity.setAggregatorId(StringUtils.trimToNull(req.getAggregatorId()));
        entity.setEntId(StringUtils.trimToNull(req.getEntId()));
        entity.setOwnerUserId(owner == null ? null : owner.getUserId());
        entity.setContactName(StringUtils.trimToNull(req.getContactName()));
        entity.setContactPhone(StringUtils.trimToNull(req.getContactPhone()));
        entity.setRemark(StringUtils.trimToNull(req.getRemark()));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        consoleTenantMapper.insertSelective(entity);
        if (owner != null) {
            bindUserToTenant(owner, tenantId);
        }
        platformAuditLogService.record("TENANT", tenantId, "CREATE", null, entity, "SUCCESS", null);
        return detail(tenantId);
    }

    @Override
    public TenantDetailResp update(String tenantId, TenantUpsertReq req) {
        validate(req);
        ConsoleTenant existing = requireTenant(tenantId);
        if (safeCount(consoleTenantMapper.countByTenantName(req.getTenantName(), tenantId)) > 0) {
            throw new BaseException(StatusCode.C.getCode(), "租户名称已存在");
        }
        if (StringUtils.isNotBlank(existing.getAggregatorId())
                && StringUtils.isNotBlank(req.getAggregatorId())
                && !StringUtils.equals(existing.getAggregatorId(), req.getAggregatorId())) {
            throw new BaseException(StatusCode.C.getCode(), "当前版本不支持变更租户聚合商ID");
        }
        if (StringUtils.isNotBlank(existing.getEntId())
                && StringUtils.isNotBlank(req.getEntId())
                && !StringUtils.equals(existing.getEntId(), req.getEntId())) {
            throw new BaseException(StatusCode.C.getCode(), "当前版本不支持变更租户企业ID");
        }
        ConsoleUser owner = resolveOwner(req.getOwnerUserId());
        ConsoleTenant before = copy(existing);
        existing.setTenantName(StringUtils.trim(req.getTenantName()));
        existing.setTenantType(StringUtils.upperCase(StringUtils.trim(req.getTenantType())));
        existing.setOwnerUserId(owner == null ? null : owner.getUserId());
        existing.setContactName(StringUtils.trimToNull(req.getContactName()));
        existing.setContactPhone(StringUtils.trimToNull(req.getContactPhone()));
        existing.setRemark(StringUtils.trimToNull(req.getRemark()));
        existing.setUpdateTime(DateUtils.getTime());
        consoleTenantMapper.updateByPrimaryKeySelective(existing);
        if (owner != null) {
            bindUserToTenant(owner, tenantId);
        }
        platformAuditLogService.record("TENANT", tenantId, "UPDATE", before, existing, "SUCCESS", null);
        return detail(tenantId);
    }

    @Override
    public Boolean updateStatus(String tenantId, TenantStatusUpdateReq req) {
        if (req == null || req.getStatus() == null) {
            throw new BaseException(StatusCode.C.getCode(), "状态不能为空");
        }
        ConsoleTenant existing = requireTenant(tenantId);
        ConsoleTenant before = copy(existing);
        existing.setStatus(req.getStatus());
        existing.setUpdateTime(DateUtils.getTime());
        consoleTenantMapper.updateByPrimaryKeySelective(existing);
        platformAuditLogService.record("TENANT", tenantId, "STATUS", before, existing, "SUCCESS", null);
        return true;
    }

    @Override
    public List<TenantProductResp> products(String tenantId) {
        requireTenant(tenantId);
        List<ConsoleTenantProduct> list = consoleTenantProductMapper.listByTenantId(tenantId);
        if (list == null || list.isEmpty()) {
            List<ConsoleCustomerProduct> compatList = consoleCustomerProductMapper.listByCustomerId(tenantId);
            List<TenantProductResp> compatRespList = new ArrayList<>();
            for (ConsoleCustomerProduct item : compatList) {
                TenantProductResp resp = new TenantProductResp();
                resp.setProductCode(item.getProductCode());
                resp.setEnabled(item.getEnabled());
                resp.setValidFrom(item.getValidFrom());
                resp.setValidTo(item.getValidTo());
                compatRespList.add(resp);
            }
            return compatRespList;
        }
        List<TenantProductResp> respList = new ArrayList<>();
        for (ConsoleTenantProduct item : list) {
            TenantProductResp resp = new TenantProductResp();
            resp.setProductCode(item.getProductCode());
            resp.setEnabled(item.getEnabled());
            resp.setValidFrom(item.getValidFrom());
            resp.setValidTo(item.getValidTo());
            resp.setConfigJson(item.getConfigJson());
            respList.add(resp);
        }
        return respList;
    }

    @Override
    public Boolean saveProducts(String tenantId, TenantProductSaveReq req) {
        requireTenant(tenantId);
        List<TenantProductResp> before = products(tenantId);
        Set<String> validProductCodes = new LinkedHashSet<>(consoleProductService.allProductCodes());
        List<TenantProductItemReq> items = req == null || req.getProducts() == null ? Collections.emptyList() : req.getProducts();
        for (TenantProductItemReq item : items) {
            if (item == null || StringUtils.isBlank(item.getProductCode()) || !validProductCodes.contains(StringUtils.trim(item.getProductCode()))) {
                throw new BaseException(StatusCode.C.getCode(), "存在无效产品编码");
            }
        }
        consoleTenantProductMapper.deleteByTenantId(tenantId);
        consoleCustomerProductMapper.deleteByCustomerId(tenantId);
        String now = DateUtils.getTime();
        for (TenantProductItemReq item : items) {
            ConsoleTenantProduct tenantProduct = new ConsoleTenantProduct();
            tenantProduct.setTenantId(tenantId);
            tenantProduct.setProductCode(StringUtils.trim(item.getProductCode()));
            tenantProduct.setEnabled(item.getEnabled() == null ? 1 : item.getEnabled());
            tenantProduct.setValidFrom(StringUtils.trimToNull(item.getValidFrom()));
            tenantProduct.setValidTo(StringUtils.trimToNull(item.getValidTo()));
            tenantProduct.setConfigJson(StringUtils.trimToNull(item.getConfigJson()));
            tenantProduct.setCreateTime(now);
            tenantProduct.setUpdateTime(now);
            consoleTenantProductMapper.insertSelective(tenantProduct);

            ConsoleCustomerProduct compat = new ConsoleCustomerProduct();
            compat.setUserId(null);
            compat.setCustomerId(tenantId);
            compat.setProductCode(tenantProduct.getProductCode());
            compat.setEnabled(tenantProduct.getEnabled());
            compat.setValidFrom(tenantProduct.getValidFrom());
            compat.setValidTo(tenantProduct.getValidTo());
            compat.setCreateTime(now);
            compat.setUpdateTime(now);
            consoleCustomerProductMapper.insertSelective(compat);
        }
        List<TenantProductResp> after = products(tenantId);
        platformAuditLogService.record("TENANT_PRODUCT", tenantId, "SAVE", before, after, "SUCCESS", null);
        return true;
    }

    private List<TenantPageItemResp> enrichPageItems(List<ConsoleTenant> tenants) {
        if (tenants == null || tenants.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> ownerUserIds = new LinkedHashSet<>();
        Set<String> tenantIds = new LinkedHashSet<>();
        for (ConsoleTenant tenant : tenants) {
            if (StringUtils.isNotBlank(tenant.getOwnerUserId())) {
                ownerUserIds.add(tenant.getOwnerUserId());
            }
            tenantIds.add(tenant.getTenantId());
        }
        Map<String, ConsoleUser> owners = new HashMap<>();
        if (!ownerUserIds.isEmpty()) {
            for (ConsoleUser user : consoleUserMapper.listByUserIds(new ArrayList<>(ownerUserIds))) {
                owners.put(user.getUserId(), user);
            }
        }
        Map<String, List<String>> productCodesByTenant = new HashMap<>();
        if (!tenantIds.isEmpty()) {
            for (ConsoleTenantProduct product : consoleTenantProductMapper.listByTenantIds(new ArrayList<>(tenantIds))) {
                productCodesByTenant.computeIfAbsent(product.getTenantId(), item -> new ArrayList<>()).add(product.getProductCode());
            }
        }
        List<TenantPageItemResp> respList = new ArrayList<>();
        for (ConsoleTenant tenant : tenants) {
            TenantPageItemResp resp = new TenantPageItemResp();
            resp.setTenantId(tenant.getTenantId());
            resp.setTenantName(tenant.getTenantName());
            resp.setTenantType(tenant.getTenantType());
            resp.setStatus(tenant.getStatus());
            resp.setAggregatorId(tenant.getAggregatorId());
            resp.setEntId(tenant.getEntId());
            resp.setOwnerUserId(tenant.getOwnerUserId());
            ConsoleUser owner = owners.get(tenant.getOwnerUserId());
            resp.setOwnerDisplayName(owner == null ? null : owner.getDisplayName());
            resp.setProductCodes(productCodesByTenant.getOrDefault(tenant.getTenantId(), Collections.emptyList()));
            resp.setUpdateTime(tenant.getUpdateTime());
            respList.add(resp);
        }
        return respList;
    }

    private ConsoleTenant requireTenant(String tenantId) {
        ConsoleTenant tenant = consoleTenantMapper.getByTenantId(tenantId);
        if (tenant == null) {
            throw new BaseException(StatusCode.C.getCode(), "租户不存在");
        }
        return tenant;
    }

    private void validate(TenantUpsertReq req) {
        if (req == null || StringUtils.isBlank(req.getTenantName()) || StringUtils.isBlank(req.getTenantType())) {
            throw new BaseException(StatusCode.C.getCode(), "租户参数不完整");
        }
        String tenantType = StringUtils.upperCase(StringUtils.trim(req.getTenantType()));
        if (!VALID_TENANT_TYPES.contains(tenantType)) {
            throw new BaseException(StatusCode.C.getCode(), "租户类型不支持");
        }
    }

    private String generateTenantId(TenantUpsertReq req) {
        if (StringUtils.isNotBlank(req.getEntId())) {
            return StringUtils.trim(req.getEntId());
        }
        if (StringUtils.isNotBlank(req.getAggregatorId())) {
            return StringUtils.trim(req.getAggregatorId());
        }
        return "TENANT" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    private ConsoleUser resolveOwner(String ownerUserId) {
        if (StringUtils.isBlank(ownerUserId)) {
            return null;
        }
        ConsoleUser user = consoleUserMapper.getByUserId(ownerUserId);
        if (user == null) {
            throw new BaseException(StatusCode.C.getCode(), "管理员账号不存在");
        }
        return user;
    }

    private void bindUserToTenant(ConsoleUser user, String tenantId) {
        if (user == null || StringUtils.equals(user.getTenantId(), tenantId)) {
            return;
        }
        ConsoleUser update = new ConsoleUser();
        update.setId(user.getId());
        update.setTenantId(tenantId);
        update.setUpdateTime(DateUtils.getTime());
        consoleUserMapper.updateByPrimaryKeySelective(update);
    }

    private ConsoleTenant copy(ConsoleTenant source) {
        ConsoleTenant target = new ConsoleTenant();
        target.setId(source.getId());
        target.setTenantId(source.getTenantId());
        target.setTenantName(source.getTenantName());
        target.setTenantType(source.getTenantType());
        target.setStatus(source.getStatus());
        target.setAggregatorId(source.getAggregatorId());
        target.setEntId(source.getEntId());
        target.setOwnerUserId(source.getOwnerUserId());
        target.setContactName(source.getContactName());
        target.setContactPhone(source.getContactPhone());
        target.setRemark(source.getRemark());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }
}
