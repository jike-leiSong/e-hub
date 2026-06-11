package cn.enn.sms.service;

import cn.enn.sms.req.ApplicationTokenReq;
import cn.enn.sms.req.SendMessageReq;
import cn.enn.sms.resp.SmsResultVO;

public interface SmsService {
    SmsResultVO<String> getToken(ApplicationTokenReq req);
    SmsResultVO<Object> messageSend(SendMessageReq req);
    Object getAliveClient(Object req);
}
