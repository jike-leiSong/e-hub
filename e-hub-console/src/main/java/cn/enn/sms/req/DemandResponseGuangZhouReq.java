package cn.enn.sms.req;

import lombok.Data;

@Data
public class DemandResponseGuangZhouReq {
    private String entId;
    private String message;
    private String phone;
}
