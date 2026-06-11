package cn.enn.iot.cto;

import lombok.Data;

@Data
public class CmdSetDTO {
    private String commandId;
    private String deviceId;
    private CmdSetData data;
}
