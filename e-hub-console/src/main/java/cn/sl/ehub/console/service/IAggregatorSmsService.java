package cn.sl.ehub.console.service;/**
 * @ProjectName: load-aggregator
 * @Package: cn.sl.ehub.upstream.service
 * @ClassName: IAggregatorSmsService
 * @Author sl
 * @Description: 聚合商用户联系方式
 * @Date 2026-05-28
 * @Version: 1.0
 */

import cn.sl.ehub.service.req.UpdateEntPhoneReq;
import cn.sl.ehub.service.vo.AggregatorSms;

import java.util.List;

/**
 * @author sl
 * @date 2026-05-28
 */
public interface IAggregatorSmsService {

    List<AggregatorSms> getAggregatorSms();

    List<AggregatorSms> getEntWarningSend(String entId);

    int save(List<UpdateEntPhoneReq> phoneList, String entId);
}
