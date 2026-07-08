package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.dto.tariff.AgentPriceAreaOption;
import cn.sl.ehub.service.dto.tariff.AgentPriceHeaderResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceQueryReq;
import cn.sl.ehub.service.dto.tariff.AgentPriceSourceResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceValuePointResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceVersionResp;
import cn.sl.ehub.service.dto.tariff.FpgjPointResp;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TariffAgentPriceMapper {

    List<String> selectVersions();

    List<AgentPriceVersionResp> selectVersionOptions(@Param("provinceCode") String provinceCode,
                                                     @Param("version") String version);

    List<FpgjPointResp> selectFpgjData(@Param("req") AgentPriceQueryReq req);

    List<BigDecimal> selectAgentPriceData(@Param("req") AgentPriceQueryReq req);

    List<AgentPriceValuePointResp> selectAgentPricePointData(@Param("req") AgentPriceQueryReq req);

    AgentPriceHeaderResp selectAgentPriceHeader(@Param("req") AgentPriceQueryReq req);

    List<AgentPriceAreaOption> selectAreaOptions(@Param("version") String version);

    List<String> selectUserTypes(@Param("req") AgentPriceQueryReq req);

    List<String> selectSfTypes(@Param("req") AgentPriceQueryReq req);

    List<String> selectDyLevels(@Param("req") AgentPriceQueryReq req);

    AgentPriceSourceResp selectSourceDocument(@Param("req") AgentPriceQueryReq req);

    AgentPriceSourceResp selectSourceImportBatch(@Param("req") AgentPriceQueryReq req);

    AgentPriceSourceResp selectSourceConfigFallback(@Param("req") AgentPriceQueryReq req);
}
