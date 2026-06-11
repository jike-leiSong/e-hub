package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 设备申报功率比例
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备申报功率比例")
@Table(name = "aggregator_device_delivery_power_percent")
public class AggregatorDeviceDeliveryPowerPercent {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("资源类型ID")
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    @ApiModelProperty("日期")
    @Column(name = "date")
    private String date;
    @ApiModelProperty("时间")
    @Column(name = "time")
    private String time;
    @ApiModelProperty("详情")
    @Column(name = "detail")
    private String detail;
    @ApiModelProperty("详情")
    @Column(name = "detail_byte")
    private byte[] detailByte;
}
