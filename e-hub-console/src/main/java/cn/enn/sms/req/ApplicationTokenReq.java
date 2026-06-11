package cn.enn.sms.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTokenReq {
    private String appId;
    private String appSecret;
    private String timestamp;
}
