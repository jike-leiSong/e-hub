package cn.sl.ehub.common.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Data
public class DeviceGroupInfo {

    private String deviceGroupCode;
    private String deviceGroupId;
    private String deviceGroupName;
    private String type;
    private List<DeviceGroupDeviceInfo> deviceList;
}
