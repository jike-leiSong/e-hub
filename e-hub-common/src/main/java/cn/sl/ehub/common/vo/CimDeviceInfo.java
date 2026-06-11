package cn.sl.ehub.common.vo;

import lombok.Data;

/**
 * cim设备信息
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
public class CimDeviceInfo {

    private String id;
    private String deviceId;
    private String aliasCode;
    private String name;
    private String cimCode;
    private String stationId;
    private String trdPtyCode;
    private String managerEntId;
    private Double ratedPower;
    private String productCode;
}
