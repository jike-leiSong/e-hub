package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.dto.tariff.AgentPriceAreaOption;
import cn.sl.ehub.service.dto.tariff.AgentPriceHeaderResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceQueryReq;
import cn.sl.ehub.service.dto.tariff.AgentPriceSourceResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceValuePointResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceVersionResp;
import cn.sl.ehub.service.dto.tariff.FpgjPointResp;
import cn.sl.ehub.service.dto.tariff.TariffAgentPriceDataInsertRow;
import cn.sl.ehub.service.dto.tariff.TariffAgentPriceInsertRow;
import cn.sl.ehub.service.dto.tariff.TariffAgentPriceStoredPoint;
import cn.sl.ehub.service.dto.tariff.TariffAgentPriceStoredRow;
import cn.sl.ehub.service.dto.tariff.TariffFpgjTypeDataInsertRow;
import cn.sl.ehub.service.dto.tariff.TariffFpgjTypeInsertRow;
import cn.sl.ehub.service.dto.tariff.TariffRulePricePreviewResp;
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

    int deleteAgentPriceDataByScope(@Param("version") String version,
                                    @Param("provinceCode") String provinceCode,
                                    @Param("secondType") String secondType,
                                    @Param("thirdType") String thirdType);

    int deleteAgentPricesByScope(@Param("version") String version,
                                 @Param("provinceCode") String provinceCode,
                                 @Param("secondType") String secondType,
                                 @Param("thirdType") String thirdType);

    int deleteAgentPriceDataByPriceRows(@Param("version") String version,
                                        @Param("provinceCode") String provinceCode,
                                        @Param("secondType") String secondType,
                                        @Param("thirdType") String thirdType,
                                        @Param("list") List<TariffRulePricePreviewResp> list);

    int deleteAgentPricesByPriceRows(@Param("version") String version,
                                     @Param("provinceCode") String provinceCode,
                                     @Param("secondType") String secondType,
                                     @Param("thirdType") String thirdType,
                                     @Param("list") List<TariffRulePricePreviewResp> list);

    int deleteFpgjTypeDataByScope(@Param("version") String version,
                                  @Param("provinceCode") String provinceCode,
                                  @Param("secondType") String secondType);

    int deleteFpgjTypesByScope(@Param("version") String version,
                               @Param("provinceCode") String provinceCode,
                               @Param("secondType") String secondType);

    int countAgentPricesBySecondScope(@Param("version") String version,
                                      @Param("provinceCode") String provinceCode,
                                      @Param("secondType") String secondType);

    int insertFpgjType(@Param("row") TariffFpgjTypeInsertRow row);

    int batchInsertFpgjTypeData(@Param("list") List<TariffFpgjTypeDataInsertRow> list);

    int batchInsertAgentPrice(@Param("list") List<TariffAgentPriceInsertRow> list);

    int batchInsertAgentPriceData(@Param("list") List<TariffAgentPriceDataInsertRow> list);

    List<TariffAgentPriceStoredRow> selectAgentPriceRowsByScope(@Param("version") String version,
                                                                @Param("provinceCode") String provinceCode,
                                                                @Param("secondType") String secondType,
                                                                @Param("thirdType") String thirdType);

    List<TariffAgentPriceStoredPoint> selectAgentPricePointsByScope(@Param("version") String version,
                                                                    @Param("provinceCode") String provinceCode,
                                                                    @Param("secondType") String secondType,
                                                                    @Param("thirdType") String thirdType);
}
