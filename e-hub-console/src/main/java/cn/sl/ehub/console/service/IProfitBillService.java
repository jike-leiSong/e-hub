package cn.sl.ehub.console.service;

import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.resp.ProfitBillDetailDateResp;

import java.util.List;

/**
 * 收益账单Service
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IProfitBillService {

    /**
     * 查询总收益
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @return
     */
    Double getTotalProfit(String entId, String startDate, String endDate);

    /**
     * 查询收益账单
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @param pageIndex
     * @param pageSize
     * @return
     */
    PageResultVO<ProfitBillDetailDateResp> getProfitBill(String entId, String startDate, String endDate, Integer pageIndex, Integer pageSize);

    /**
     * 查询收益账单
     *
     * @param entId
     * @param startDate
     * @param endDate
     * @return
     */
    List<ProfitBillDetailDateResp> getProfitBill(String entId, String startDate, String endDate);
}
