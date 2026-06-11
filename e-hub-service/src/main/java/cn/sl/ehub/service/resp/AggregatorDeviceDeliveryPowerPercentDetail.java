package cn.sl.ehub.service.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 设备申报功率比例详情
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备申报功率比例详情")
public class AggregatorDeviceDeliveryPowerPercentDetail {

    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("资源类型ID")
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;
    @ApiModelProperty("企业编码")
    @Column(name = "station_id")
    private String stationId;
    @ApiModelProperty("设备ID")
    @Column(name = "device_base_id")
    private String deviceBaseId;
    @ApiModelProperty("设备编码")
    @Column(name = "device_id")
    private String deviceId;
    @ApiModelProperty("日期")
    @Column(name = "date")
    private String date;
    @ApiModelProperty("时间")
    @Column(name = "time")
    private String time;
    @ApiModelProperty("额定功率")
    @Column(name = "power")
    private Double power;
    @ApiModelProperty("申报功率")
    @Column(name = "delivery_power")
    private Double deliveryPower;
    @ApiModelProperty("比例")
    @Column(name = "percent")
    private Double percent;
    @ApiModelProperty("下发设备ID")
    @Column(name = "iot_device_base_id")
    private String iotDeviceBaseId;
}
