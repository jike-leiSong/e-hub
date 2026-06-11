package cn.sl.ehub.common.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Data
public class DeviceGroupDeviceInfo {

    private String deviceBrand;
    private String deviceCode;
    private String deviceDescription;
    private String deviceId;
    private String deviceModel;
    private String deviceName;
    private String deviceTypeCode;
    private String ifRealDevice;
    private List<DeviceGroupPointInfo> pointList;
    private List<DeviceTagInfo> tagList;
}
