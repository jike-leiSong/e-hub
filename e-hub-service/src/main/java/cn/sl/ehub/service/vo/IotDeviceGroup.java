package cn.sl.ehub.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("IoT设备组")
@Table(name = "iot_device_group")
public class IotDeviceGroup {

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

    @ApiModelProperty("设备组编码")
    @Column(name = "device_group_code")
    private String deviceGroupCode;

    @ApiModelProperty("设备组名称")
    @Column(name = "device_group_name")
    private String deviceGroupName;

    @ApiModelProperty("设备组类型编码")
    @Column(name = "device_group_type")
    private String deviceGroupType;

    @ApiModelProperty("设备组类型名称")
    @Column(name = "device_group_type_name")
    private String deviceGroupTypeName;

    @ApiModelProperty("能源类型")
    @Column(name = "energy_type")
    private String energyType;

    @ApiModelProperty("网关ID")
    @Column(name = "gateway_id")
    private Long gatewayId;

    @ApiModelProperty("是否虚拟默认设备组")
    @Column(name = "virtual_flag")
    private Integer virtualFlag;

    @ApiModelProperty("状态")
    @Column(name = "status")
    private Integer status;

    @ApiModelProperty("删除标识")
    @Column(name = "deleted")
    private Integer deleted;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @ApiModelProperty("创建时间")
    @Column(name = "create_time")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @Column(name = "update_time")
    private Date updateTime;

    @Transient
    @ApiModelProperty("企业名称")
    private String entName;

    @Transient
    @ApiModelProperty("网关名称")
    private String gatewayName;

    @Transient
    @ApiModelProperty("设备数量")
    private Integer deviceCount;

    @Transient
    @ApiModelProperty("设备组参数")
    private List<IotDeviceGroupParam> paramList;

    @Transient
    @ApiModelProperty("设备组测点")
    private List<IotDeviceGroupPoint> pointList;
}
