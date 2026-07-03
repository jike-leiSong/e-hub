package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.model.req.ConfigItemPageReq;
import cn.sl.ehub.console.model.req.ConfigItemUpsertReq;
import cn.sl.ehub.console.model.resp.ConfigItemResp;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.console.service.IPlatformAuditLogService;
import cn.sl.ehub.console.service.IPlatformConfigService;
import cn.sl.ehub.service.mapper.ConsoleConfigItemMapper;
import cn.sl.ehub.service.vo.ConsoleConfigItem;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformConfigServiceImpl implements IPlatformConfigService {

    private final ConsoleConfigItemMapper consoleConfigItemMapper;
    private final IPlatformAuditLogService platformAuditLogService;

    @Override
    public PageResultVO<ConfigItemResp> items(ConfigItemPageReq req) {
        Integer pageIndex = req.getPageIndex() == null || req.getPageIndex() < 1 ? 1 : req.getPageIndex();
        Integer pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 20 : req.getPageSize();
        PageHelper.startPage(pageIndex, pageSize);
        List<ConsoleConfigItem> list = consoleConfigItemMapper.page(
                StringUtils.trimToNull(req.getKeyword()),
                StringUtils.trimToNull(req.getConfigGroup()),
                req.getStatus()
        );
        List<ConfigItemResp> respList = new ArrayList<>();
        for (ConsoleConfigItem item : list) {
            respList.add(toResp(item));
        }
        PageInfo<ConsoleConfigItem> pageInfo = new PageInfo<>(list);
        PageResultVO<ConfigItemResp> page = new PageResultVO<>();
        page.setList(respList);
        page.setTotal((int) pageInfo.getTotal());
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public ConfigItemResp create(ConfigItemUpsertReq req) {
        validate(req);
        if (consoleConfigItemMapper.getByConfigKey(req.getConfigKey()) != null) {
            throw new BaseException(StatusCode.C.getCode(), "配置键已存在");
        }
        String now = DateUtils.getTime();
        ConsoleConfigItem entity = new ConsoleConfigItem();
        entity.setConfigKey(StringUtils.trim(req.getConfigKey()));
        entity.setConfigName(StringUtils.trim(req.getConfigName()));
        entity.setConfigValue(StringUtils.defaultString(req.getConfigValue()));
        entity.setConfigGroup(StringUtils.trim(req.getConfigGroup()));
        entity.setValueType(StringUtils.defaultIfBlank(StringUtils.trim(req.getValueType()), "STRING"));
        entity.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        entity.setRemark(StringUtils.trimToNull(req.getRemark()));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        consoleConfigItemMapper.insertSelective(entity);
        platformAuditLogService.record("CONFIG", entity.getConfigKey(), "CREATE", null, entity, "SUCCESS", null);
        return toResp(entity);
    }

    @Override
    public ConfigItemResp update(Long id, ConfigItemUpsertReq req) {
        validate(req);
        ConsoleConfigItem existing = consoleConfigItemMapper.selectByPrimaryKey(id);
        if (existing == null) {
            throw new BaseException(StatusCode.C.getCode(), "配置项不存在");
        }
        ConsoleConfigItem duplicated = consoleConfigItemMapper.getByConfigKey(req.getConfigKey());
        if (duplicated != null && !duplicated.getId().equals(existing.getId())) {
            throw new BaseException(StatusCode.C.getCode(), "配置键已存在");
        }
        ConsoleConfigItem before = copy(existing);
        existing.setConfigKey(StringUtils.trim(req.getConfigKey()));
        existing.setConfigName(StringUtils.trim(req.getConfigName()));
        existing.setConfigValue(StringUtils.defaultString(req.getConfigValue()));
        existing.setConfigGroup(StringUtils.trim(req.getConfigGroup()));
        existing.setValueType(StringUtils.defaultIfBlank(StringUtils.trim(req.getValueType()), "STRING"));
        existing.setStatus(req.getStatus() == null ? existing.getStatus() : req.getStatus());
        existing.setRemark(StringUtils.trimToNull(req.getRemark()));
        existing.setUpdateTime(DateUtils.getTime());
        consoleConfigItemMapper.updateByPrimaryKeySelective(existing);
        platformAuditLogService.record("CONFIG", existing.getConfigKey(), "UPDATE", before, existing, "SUCCESS", null);
        return toResp(existing);
    }

    private void validate(ConfigItemUpsertReq req) {
        if (req == null
                || StringUtils.isBlank(req.getConfigKey())
                || StringUtils.isBlank(req.getConfigName())
                || StringUtils.isBlank(req.getConfigGroup())) {
            throw new BaseException(StatusCode.C.getCode(), "配置项参数不完整");
        }
    }

    private ConfigItemResp toResp(ConsoleConfigItem item) {
        ConfigItemResp resp = new ConfigItemResp();
        resp.setId(item.getId());
        resp.setConfigKey(item.getConfigKey());
        resp.setConfigName(item.getConfigName());
        resp.setConfigValue(item.getConfigValue());
        resp.setConfigGroup(item.getConfigGroup());
        resp.setValueType(item.getValueType());
        resp.setStatus(item.getStatus());
        resp.setRemark(item.getRemark());
        resp.setUpdateTime(item.getUpdateTime());
        return resp;
    }

    private ConsoleConfigItem copy(ConsoleConfigItem source) {
        ConsoleConfigItem target = new ConsoleConfigItem();
        target.setId(source.getId());
        target.setConfigKey(source.getConfigKey());
        target.setConfigName(source.getConfigName());
        target.setConfigValue(source.getConfigValue());
        target.setConfigGroup(source.getConfigGroup());
        target.setValueType(source.getValueType());
        target.setStatus(source.getStatus());
        target.setRemark(source.getRemark());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        return target;
    }
}
