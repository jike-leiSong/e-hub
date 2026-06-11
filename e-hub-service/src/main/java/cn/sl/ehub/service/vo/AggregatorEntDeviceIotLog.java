package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 设备下发记录
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Data
@ApiModel("设备下发记录")
@Table(name = "aggregator_ent_device_iot_log")
public class AggregatorEntDeviceIotLog {

    @Id
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Integer id;
    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;
    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;
    @ApiModelProperty("企业编码")
    @Column(name = "station_id")
    private String stationId;
    @ApiModelProperty("资源类型ID")
    @Column(name = "resource_type_id")
    private String resourceTypeId;
    @ApiModelProperty("设备ID")
    @Column(name = "device_base_id")
    private String deviceBaseId;
    @ApiModelProperty("设备编码")
    @Column(name = "device_id")
    private String deviceId;
    @ApiModelProperty("设备名称")
    @Column(name = "device_name")
    private String deviceName;
    @ApiModelProperty("设备类型")
    @Column(name = "device_type")
    private String deviceType;
    @ApiModelProperty("下发时间")
    @Column(name = "send_time")
    private String sendTime;
    @ApiModelProperty("执行结果")
    @Column(name = "result_msg")
    private String resultMsg;
}
