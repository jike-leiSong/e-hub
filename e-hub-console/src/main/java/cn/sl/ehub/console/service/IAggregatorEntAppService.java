package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.req.NewUserAdjustmentGraphReq;
import cn.sl.ehub.console.model.req.RunPlanTodayReq;
import cn.sl.ehub.console.model.vo.EntExistResourceTypeVO;
import cn.sl.ehub.console.model.vo.HistoryQueryGraphVO;
import cn.sl.ehub.console.model.vo.RunPlanTodayVO;

import java.util.List;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public interface IAggregatorEntAppService {

    /**
     * @description 用户今日运行计划
     * @param
     * @return
     */
    RunPlanTodayVO getRunPlanChart(RunPlanTodayReq runPlanTodayReq);

    List<EntExistResourceTypeVO> getEntExistResourceTypeVO(String entId);

}
