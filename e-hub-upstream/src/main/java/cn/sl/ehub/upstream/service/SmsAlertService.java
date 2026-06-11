package cn.sl.ehub.upstream.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信服务（临时替代类）
 * 原：cn.enn.sms.service.SmsService
 *
 * 说明：仅记录日志，不发送真实短信
 * TODO: 后续可对接钉钉、企业微信等告警渠道
 */
@Service
@Slf4j
public class SmsAlertService {

    /**
     * 发送短信告警
     */
    public void sendSms(String phone, String message) {
        log.warn("【告警通知】手机号：{}，内容：{}", phone, message);
    }

    /**
     * 批量发送短信
     */
    public void sendBatchSms(String[] phones, String message) {
        for (String phone : phones) {
            sendSms(phone, message);
        }
    }
}
