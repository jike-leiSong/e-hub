package cn.enn.sms.req;

import lombok.Data;

import java.util.List;

@Data
public class SendMessageReq {
    private String message;
    private String phone;
    private String type;
    private String token;
    private String entId;
    private List<String> openIds;
    private String applicationName;
    private String messageType;
    private SmsRange smsRange;
}
