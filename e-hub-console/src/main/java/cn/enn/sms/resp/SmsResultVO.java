package cn.enn.sms.resp;

import lombok.Data;

@Data
public class SmsResultVO<T> {
    private Integer code;
    private String message;
    private T data;
}
