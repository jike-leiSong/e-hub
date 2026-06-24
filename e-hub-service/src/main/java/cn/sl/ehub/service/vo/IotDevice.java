package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@ApiModel("IoT设备")
@Table(name = "iot_device")
public class IotDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("聚合商ID")
    @Column(name = "aggregator_id")
    private String aggregatorId;

    @ApiModelProperty("企业ID")
    @Column(name = "ent_id")
    private String entId;

    @ApiModelProperty("项目ID")
    @Column(name = "project_id")
    private Long projectId;

    @ApiModelProperty("标准设备编码")
    @Column(name = "device_code")
    private String deviceCode;

    @ApiModelProperty("设备名称")
    @Column(name = "device_name")
    private String deviceName;

    @ApiModelProperty("设备类型编码")
    @Column(name = "device_type_code")
    private String deviceTypeCode;

    @ApiModelProperty("设备类型名称")
    @Column(name = "device_type_name")
    private String deviceTypeName;

    @ApiModelProperty("厂商")
    @Column(name = "manufacturer")
    private String manufacturer;

    @ApiModelProperty("型号")
    @Column(name = "model")
    private String model;

    @ApiModelProperty("资产状态：1启用，0停用")
    @Column(name = "asset_status")
    private Integer assetStatus;

    @ApiModelProperty("在线状态：1在线，0离线")
    @Column(name = "online_status")
    private Integer onlineStatus;

    @ApiModelProperty("最近数据时间")
    @Column(name = "last_data_time")
    private Date lastDataTime;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @ApiModelProperty("删除标识：0正常，1删除")
    @Column(name = "deleted")
    private Integer deleted;

    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @Column(name = "update_time")
    private Date updateTime;
}
