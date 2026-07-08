package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.dto.tariff.TariffSourceConfigQueryReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceConfigResp;
import cn.sl.ehub.service.dto.tariff.TariffSourceConfigSaveReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentQueryReq;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentResp;
import cn.sl.ehub.service.dto.tariff.TariffSourceDocumentSaveReq;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TariffSourceMapper {

    List<TariffSourceConfigResp> selectSourceConfigs(@Param("req") TariffSourceConfigQueryReq req);

    TariffSourceConfigResp selectSourceConfigById(@Param("id") Long id);

    int insertSourceConfig(@Param("req") TariffSourceConfigSaveReq req);

    int updateSourceConfig(@Param("req") TariffSourceConfigSaveReq req);

    int updateSourceConfigEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);

    List<TariffSourceDocumentResp> selectSourceDocuments(@Param("req") TariffSourceDocumentQueryReq req);

    TariffSourceDocumentResp selectSourceDocumentById(@Param("id") Long id);

    int insertSourceDocument(@Param("req") TariffSourceDocumentSaveReq req);

    int updateSourceDocument(@Param("req") TariffSourceDocumentSaveReq req);

    int updateSourceDocumentStatus(@Param("id") Long id, @Param("status") String status);
}
