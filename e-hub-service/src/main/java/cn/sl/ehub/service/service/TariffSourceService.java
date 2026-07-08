package cn.sl.ehub.service.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.dto.tariff.TariffSourceConfigQueryReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceConfigResp;
import cn.sl.ehub.service.dto.tariff.TariffSourceConfigSaveReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentQueryReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentResp;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentSaveReq;
import cn.sl.ehub.service.mapper.TariffSourceMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TariffSourceService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    private final TariffSourceMapper tariffSourceMapper;

    public TariffSourceService(TariffSourceMapper tariffSourceMapper) {
        this.tariffSourceMapper = tariffSourceMapper;
    }

    public List<TariffSourceConfigResp> listSourceConfigs(TariffSourceConfigQueryReq req) {
        return tariffSourceMapper.selectSourceConfigs(req == null ? new TariffSourceConfigQueryReq() : req);
    }

    public TariffSourceConfigResp getSourceConfig(Long id) {
        if (id == null) {
            throwParam("来源配置ID不能为空");
        }
        TariffSourceConfigResp config = tariffSourceMapper.selectSourceConfigById(id);
        if (config == null) {
            throwParam("来源配置不存在");
        }
        return config;
    }

    @Transactional(rollbackFor = Exception.class)
    public TariffSourceConfigResp createSourceConfig(TariffSourceConfigSaveReq req) {
        validateSourceConfig(req, false);
        normalizeSourceConfig(req);
        tariffSourceMapper.insertSourceConfig(req);
        return tariffSourceMapper.selectSourceConfigById(req.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public TariffSourceConfigResp updateSourceConfig(Long id, TariffSourceConfigSaveReq req) {
        if (id == null) {
            throwParam("来源配置ID不能为空");
        }
        if (req != null) {
            req.setId(id);
        }
        validateSourceConfig(req, true);
        getSourceConfig(id);
        normalizeSourceConfig(req);
        tariffSourceMapper.updateSourceConfig(req);
        return tariffSourceMapper.selectSourceConfigById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setSourceConfigEnabled(Long id, Integer enabled) {
        getSourceConfig(id);
        tariffSourceMapper.updateSourceConfigEnabled(id, enabled == null ? 0 : enabled);
    }

    public List<TariffSourceDocumentResp> listSourceDocuments(TariffSourceDocumentQueryReq req) {
        return tariffSourceMapper.selectSourceDocuments(req == null ? new TariffSourceDocumentQueryReq() : req);
    }

    public TariffSourceDocumentResp getSourceDocument(Long id) {
        if (id == null) {
            throwParam("来源文档ID不能为空");
        }
        TariffSourceDocumentResp document = tariffSourceMapper.selectSourceDocumentById(id);
        if (document == null) {
            throwParam("来源文档不存在");
        }
        return document;
    }

    @Transactional(rollbackFor = Exception.class)
    public TariffSourceDocumentResp createSourceDocument(TariffSourceDocumentSaveReq req) {
        validateSourceDocument(req, false);
        normalizeSourceDocument(req);
        fillDocumentSourceFromConfig(req);
        tariffSourceMapper.insertSourceDocument(req);
        return tariffSourceMapper.selectSourceDocumentById(req.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public TariffSourceDocumentResp updateSourceDocument(Long id, TariffSourceDocumentSaveReq req) {
        if (id == null) {
            throwParam("来源文档ID不能为空");
        }
        if (req != null) {
            req.setId(id);
        }
        validateSourceDocument(req, true);
        getSourceDocument(id);
        normalizeSourceDocument(req);
        fillDocumentSourceFromConfig(req);
        tariffSourceMapper.updateSourceDocument(req);
        return tariffSourceMapper.selectSourceDocumentById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public TariffSourceDocumentResp updateSourceDocumentStatus(Long id, String status) {
        getSourceDocument(id);
        String normalizedStatus = normalizeStatus(status);
        tariffSourceMapper.updateSourceDocumentStatus(id, normalizedStatus);
        return tariffSourceMapper.selectSourceDocumentById(id);
    }

    private void validateSourceConfig(TariffSourceConfigSaveReq req, boolean update) {
        if (req == null) {
            throwParam("来源配置不能为空");
        }
        if (update && req.getId() == null) {
            throwParam("来源配置ID不能为空");
        }
        if (StringUtils.isBlank(req.getProvinceCode())) {
            throwParam("省份编码不能为空");
        }
        if (StringUtils.isBlank(req.getProvinceName())) {
            throwParam("省份名称不能为空");
        }
        if (StringUtils.isBlank(req.getSourceName())) {
            throwParam("来源名称不能为空");
        }
        if (StringUtils.isBlank(req.getSourceType())) {
            throwParam("来源类型不能为空");
        }
    }

    private void validateSourceDocument(TariffSourceDocumentSaveReq req, boolean update) {
        if (req == null) {
            throwParam("来源文档不能为空");
        }
        if (update && req.getId() == null) {
            throwParam("来源文档ID不能为空");
        }
        if (StringUtils.isBlank(req.getYearMonth())) {
            throwParam("电价月份不能为空");
        }
        if (StringUtils.isBlank(req.getVersion())) {
            throwParam("电价版本不能为空");
        }
        if (StringUtils.isBlank(req.getProvinceCode())) {
            throwParam("省份编码不能为空");
        }
        if (StringUtils.isBlank(req.getProvinceName())) {
            throwParam("省份名称不能为空");
        }
        if (req.getSourceConfigId() == null
                && StringUtils.isBlank(req.getSourceName())
                && StringUtils.isBlank(req.getSourceUrl())
                && StringUtils.isBlank(req.getSourceFileName())) {
            throwParam("来源配置、来源地址或来源文件至少填写一项");
        }
    }

    private void normalizeSourceConfig(TariffSourceConfigSaveReq req) {
        req.setProvinceCode(StringUtils.trim(req.getProvinceCode()));
        req.setProvinceName(StringUtils.trim(req.getProvinceName()));
        req.setSourceName(StringUtils.trim(req.getSourceName()));
        req.setSourceType(StringUtils.upperCase(StringUtils.trim(req.getSourceType())));
        req.setSourceUrl(StringUtils.trimToNull(req.getSourceUrl()));
        req.setPublishRule(StringUtils.trimToNull(req.getPublishRule()));
        req.setRemark(StringUtils.trimToNull(req.getRemark()));
        if (req.getEnabled() == null) {
            req.setEnabled(1);
        }
    }

    private void normalizeSourceDocument(TariffSourceDocumentSaveReq req) {
        req.setYearMonth(StringUtils.trim(req.getYearMonth()));
        req.setVersion(StringUtils.trim(req.getVersion()));
        req.setProvinceCode(StringUtils.trim(req.getProvinceCode()));
        req.setProvinceName(StringUtils.trim(req.getProvinceName()));
        req.setSourceType(StringUtils.upperCase(StringUtils.trimToNull(req.getSourceType())));
        req.setSourceName(StringUtils.trimToNull(req.getSourceName()));
        req.setSourceUrl(StringUtils.trimToNull(req.getSourceUrl()));
        req.setSourceFileName(StringUtils.trimToNull(req.getSourceFileName()));
        req.setSourceFilePath(StringUtils.trimToNull(req.getSourceFilePath()));
        req.setSourceFileHash(StringUtils.trimToNull(req.getSourceFileHash()));
        req.setDocumentTitle(StringUtils.trimToNull(req.getDocumentTitle()));
        req.setDocumentNo(StringUtils.trimToNull(req.getDocumentNo()));
        req.setPublishTime(StringUtils.trimToNull(req.getPublishTime()));
        req.setEffectiveStart(StringUtils.trimToNull(req.getEffectiveStart()));
        req.setEffectiveEnd(StringUtils.trimToNull(req.getEffectiveEnd()));
        req.setStatus(normalizeStatus(req.getStatus()));
        req.setOperatorId(StringUtils.trimToNull(req.getOperatorId()));
        req.setOperatorName(StringUtils.trimToNull(req.getOperatorName()));
        req.setRemark(StringUtils.trimToNull(req.getRemark()));
    }

    private void fillDocumentSourceFromConfig(TariffSourceDocumentSaveReq req) {
        if (req.getSourceConfigId() == null) {
            return;
        }
        TariffSourceConfigResp config = getSourceConfig(req.getSourceConfigId());
        if (StringUtils.isBlank(req.getSourceType())) {
            req.setSourceType(config.getSourceType());
        }
        if (StringUtils.isBlank(req.getSourceName())) {
            req.setSourceName(config.getSourceName());
        }
        if (StringUtils.isBlank(req.getSourceUrl())) {
            req.setSourceUrl(config.getSourceUrl());
        }
    }

    private String normalizeStatus(String status) {
        String value = StringUtils.upperCase(StringUtils.trimToNull(status));
        if (value == null) {
            return STATUS_DRAFT;
        }
        if (STATUS_DRAFT.equals(value) || STATUS_PUBLISHED.equals(value) || STATUS_ARCHIVED.equals(value)) {
            return value;
        }
        throwParam("来源文档状态不合法");
        return STATUS_DRAFT;
    }

    private void throwParam(String msg) {
        throw new BaseException(StatusCode.C.getCode(), msg);
    }
}
