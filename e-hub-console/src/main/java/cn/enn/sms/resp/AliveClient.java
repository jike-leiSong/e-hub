package cn.enn.sms.resp;

import lombok.Data;

@Data
public class AliveClient {
    private String clientId;
    private String clientName;
    private String status;
    private String openId;
}
