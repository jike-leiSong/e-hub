package cn.sl.ehub.console.service;

import cn.sl.ehub.service.req.EntMonthElectricProfitReq;
import cn.sl.ehub.service.resp.EntMonthElectricProfitResp;

import java.util.List;

/**
 * 外部调用接口Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IExternalDataService {

    /**
     * 每月各企业有效调节电量和收益
     *
     * @param req
     * @return
     */
    List<EntMonthElectricProfitResp> getEntDataRespList(EntMonthElectricProfitReq req);
}
