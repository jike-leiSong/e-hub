package cn.sl.ehub.console.service;

import cn.sl.ehub.common.vo.ResultVO;
import cn.enn.sms.req.DemandResponseGuangZhouReq;
import cn.enn.sms.req.SendMessageReq;
import cn.enn.sms.resp.AliveClient;

import java.util.List;

/**
 * @Description: 短信业务接口
 * @Author sl
 * @Date 2026-05-28
 */

public interface ISmsService {


    ResultVO<String> smsSendBatch();

    ResultVO<String> smsSendSpecified(String type, String phone);

    String getToken();

    List<AliveClient> getAliveClientList(String entId);

    ResultVO<Boolean> sendSocket(SendMessageReq req);

    ResultVO<String> smsEntSendBatch();

    ResultVO<String> smsEntSendSpecified(String timeType, String phone);

    ResultVO<String> smsEntWarningSend(String entId);
}
