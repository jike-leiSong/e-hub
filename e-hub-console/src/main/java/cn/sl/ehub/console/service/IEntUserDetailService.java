package cn.sl.ehub.console.service;

import cn.enn.cim.resp.DeviceBaseInfo;
import cn.sl.ehub.console.model.vo.OptionVO;
import cn.sl.ehub.console.model.vo.PageResultVO;
import cn.sl.ehub.service.req.UpdateEntDeviceReq;
import cn.sl.ehub.service.req.UpdateEntReq;
import cn.sl.ehub.service.resp.EntUserDetailResp;
import cn.sl.ehub.common.vo.CimDeviceInfo;

import java.util.List;

/**
 * 企业用户查询
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
public interface IEntUserDetailService {

    /**
     * 首页用户分布查询
     *
     * @param aggregatorId
     * @return
     */
    List<EntUserDetailResp> getEntUserDetailRespList(String aggregatorId);

    List<OptionVO> getEntOptions(String aggregatorId, String resourceTypeId);

    PageResultVO<EntUserDetailResp> getEntUserDetailWithDevice(String aggregatorId, String entId, Double powerGetterThan,
                                                               Double powerLessThan, Double percent, Integer pageIndex,
                                                               Integer pageSize);

    List<OptionVO> getPercentOptions(String aggregatorId);

    /**
     * 查询数据
     *
     * @param entId
     * @param startYear
     * @param endYear
     * @return
     */
    List<EntUserDetailResp> getEntAndDeviceRespList(String aggregatorId,String entId, String startYear, String endYear);

    /**
     * 删除合同
     *
     * @param entId
     * @return
     */
    Boolean deleteAgreement(String entId);

    /**
     * 查询数据
     *
     * @param aggregatorId
     * @param entId
     * @param stationId
     * @return
     */
    List<UpdateEntDeviceReq> getCimDeviceList(String aggregatorId, String entId, String stationId);

    /**
     * 更新用户信息
     *
     * @param req
     * @return
     */
    Boolean updateEnt(UpdateEntReq req);

    /**
     * 同步信息
     *
     * @param aggregatorId
     * @return
     */
    Boolean autoUpdateEnt(String aggregatorId);
}
