package cn.sl.ehub.service.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 更新用户设备信息实体
 *
 * @Author sl
 * @phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("更新用户设备信息实体")
public class UpdateEntDeviceReq {

    @ApiModelProperty("聚合商ID")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    private String entId;
    @ApiModelProperty("企业编码")
    private String stationId;
    @ApiModelProperty("资源类型Id")
    private String resourceTypeId;
    @ApiModelProperty("设备ID")
    private String deviceBaseId;
    @ApiModelProperty("设备编码")
    private String deviceId;
    @ApiModelProperty("设备名称")
    private String deviceName;
    @ApiModelProperty("户号")
    private String accountNo;
    @ApiModelProperty("额定功率")
    private Double power;
    @ApiModelProperty("最高运行负荷")
    private Double maxPower;
    @ApiModelProperty("响应能力")
    private Double responsePower;
}
