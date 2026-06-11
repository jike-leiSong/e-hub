package cn.sl.ehub.service.mapper;

import cn.sl.ehub.service.resp.AggregatorEntApplyPlanResp;
import cn.sl.ehub.service.vo.AggregatorEntApplyPlan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 企业用户申报计划Mapper
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Repository
public interface AggregatorEntApplyPlanMapper extends Mapper<AggregatorEntApplyPlan> {

    /**
     * 查询数据
     *
     * @param entId
     * @param saveStatus
     * @return
     */
    List<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespList(@Param("entId") String entId, @Param("saveStatus") Boolean saveStatus);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @return
     */
    List<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespListByPlanStatus(@Param("entId") String entId, @Param("planStatus") Boolean planStatus);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param endDate
     * @return
     */
    List<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespListByFinish(@Param("entId") String entId, @Param("planStatus") Boolean planStatus, @Param("endDate") String endDate);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param date
     * @return
     */
    List<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespListByNow(@Param("entId") String entId, @Param("planStatus") Boolean planStatus, @Param("date") String date);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param date
     * @return
     */
    List<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespListByNowByLimitOne(@Param("entId") String entId, @Param("planStatus") Boolean planStatus, @Param("date") String date);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param startDate
     * @return
     */
    List<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespListByNoStart(@Param("entId") String entId, @Param("planStatus") Boolean planStatus, @Param("startDate") String startDate);

    /**
     * 查询数据
     *
     * @param entId
     * @return
     */
    List<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespListByEntId(@Param("entId") String entId);

    /**
     * 查询数据
     *
     * @param entId
     * @param planStatus
     * @param date
     * @return
     */
    List<AggregatorEntApplyPlanResp> getAggregatorEntApplyPlanRespListByPlanStatusAndDate(@Param("entId") String entId, @Param("planStatus") Boolean planStatus, @Param("date") String date);
}
