package cn.enn.iot.vo;

import lombok.Data;

@Data
public class IotCallBackQueryVo {
    private String commandId;
    private String deviceId;
    private Integer status;
}
