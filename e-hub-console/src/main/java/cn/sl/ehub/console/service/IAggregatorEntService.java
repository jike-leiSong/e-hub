package cn.sl.ehub.console.service;

import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.service.resp.EntUserDetailResp;
import cn.sl.ehub.service.vo.AggregatorEnt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业信息Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IAggregatorEntService {

    /**
     * 查询企业信息
     *
     * @param entId
     * @return
     */
    AggregatorEnt getAggregatorEnt(String entId);

    /**
     * 查询企业信息
     *
     * @param aggregatorId
     * @return
     */
    List<AggregatorEnt> getAggregatorEntList(String aggregatorId);

    /**
     * 获取响应计划的企业信息
     * @param aggregatorId
     * @return
     */
    List<AggregatorEnt> getAggregatorPlanRunEntList(String aggregatorId);

    /**
     * 查询企业信息
     *
     * @param entId
     * @return
     */
    String getAggregatorIdByEntId(String entId);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @return
     */
    int getCount(String aggregatorId);

    /**
     * 查询企业信息
     *
     * @return
     */
    List<AggregatorEnt> getAggregatorEntList();

    /**
     * 查询企业信息
     *
     * @param entIdList
     * @return
     */
    List<AggregatorEnt> getAggregatorEntList(List<String> entIdList);

    /**
     * 首页用户分布查询
     *
     * @param aggregatorId
     * @return
     */
    List<EntUserDetailResp> getEntUserDetailRespList(String aggregatorId);

    List<Double> selectPercentDistinct(String aggregatorId);

    List<EntUserDetailResp> selectEntAndDeviceList(String entId, List<String> yearList);

    List<EntUserDetailResp> selectEntAndDeviceListByAggregatorId(String aggregatorId,String entId, List<String> yearList);

    List<EntUserDetailResp> selectEntAndDeviceListByStartYear(String entId, String startYear);
    List<EntUserDetailResp> selectEntAndDeviceListByStartYearAggregatorId(String aggregatorId,String entId, String startYear);

    List<EntUserDetailResp> selectEntAndDeviceListByEndYear(String entId, String endYear);

    List<EntUserDetailResp> selectEntAndDeviceListByEndYearAggregatorId(String aggregatorId,String entId, String endYear);

    int deleteAgreement(String entId);

    List<EntUserDetailResp> selectEntUserDetailWithDevice(String aggregatorId, String entId, Double powerGetterThan, Double powerLessThan, Double percent);

    int updateAggregatorEntAgreementInfo(UpdateEntReq req);

    /**
     * 添加企业
     *
     * @param aggregatorEntList
     * @return
     */
    int addAggregatorEntList(List<AggregatorEnt> aggregatorEntList);
}
