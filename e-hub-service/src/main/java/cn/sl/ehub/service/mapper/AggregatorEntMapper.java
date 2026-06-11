package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.service.resp.EntUserDetailResp;
import cn.sl.ehub.service.vo.AggregatorEnt;
import cn.sl.ehub.service.vo.AggregatorEntDevice;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 企业用户查询
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorEntMapper extends Mapper<AggregatorEnt> {


    /**
     * 首页用户分布查询
     *
     * @param aggregatorId
     * @return
     */
    List<EntUserDetailResp> getEntUserDetailRespList(@Param("aggregatorId") String aggregatorId);

    List<EntUserDetailResp> selectEntUserDetailWithDevice(@Param("aggregatorId") String aggregatorId,
                                                          @Param("entId") String entId,
                                                          @Param("powerGetterThan") Double powerGetterThan,
                                                          @Param("powerLessThan") Double powerLessThan,
                                                          @Param("percent") Double percent);

    List<Double> selectPercentDistinct(@Param("aggregatorId") String aggregatorId);

    /**
     * 查询数据
     *
     * @param entId
     * @param yearList
     * @return
     */
    List<EntUserDetailResp> selectEntAndDeviceList(@Param("entId") String entId, @Param("yearList") List<String> yearList);

    /**
     * 查询数据
     *
     * @param entId
     * @param yearList
     * @return
     */
    List<EntUserDetailResp> selectEntAndDeviceListByAggregatorId(@Param("aggregatorId") String aggregatorId,@Param("entId") String entId, @Param("yearList") List<String> yearList);

    /**
     * 查询数据
     *
     * @param entId
     * @param startYear
     * @return
     */
    List<EntUserDetailResp> selectEntAndDeviceListByStartYear(@Param("entId") String entId, @Param("startYear") String startYear);



    /**
     * 查询数据
     *
     * @param entId
     * @param startYear
     * @return
     */
    List<EntUserDetailResp> selectEntAndDeviceListByStartYearAggregatorId(@Param("aggregatorId") String aggregatorId,@Param("entId") String entId, @Param("startYear") String startYear);
    /**
     * 查询数据
     *
     * @param entId
     * @param endYear
     * @return
     */
    List<EntUserDetailResp> selectEntAndDeviceListByEndYear(@Param("aggregatorId") String aggregatorId, @Param("entId") String entId, @Param("endYear") String endYear);

    /**
     * 查询数据
     *
     * @param entId
     * @param endYear
     * @return
     */
    List<EntUserDetailResp> selectEntAndDeviceListByEndYearAggregatorId(@Param("entId") String entId, @Param("endYear") String endYear);
    /**
     * 删除合同
     *
     * @param entId
     * @return
     */
    int deleteAgreement(String entId);

    /**
     * 更新数据
     *
     * @param req
     * @return
     */
    int updateAggregatorEntAgreementInfo(UpdateEntReq req);

    /**
     * 获取在线企业
     * @return
     */
    List<AggregatorEnt> getOnlineAggregatorEntList();

    List<AggregatorEnt> getOnlineAggregatorEntListByResourTypeId(String resourTypeId);

    List<AggregatorEntDevice> getOnlineEnergyStationListByResourTypeId(String resourTypeId);

}
