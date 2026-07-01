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

@Data
@ApiModel("IoT设备测点")
@Table(name = "iot_device_point")
public class IotDevicePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty("主键ID")
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("设备ID")
    @Column(name = "device_id")
    private Long deviceId;

    @ApiModelProperty("租户ID")
    @Column(name = "tenant_id")
    private Long tenantId;

    @ApiModelProperty("测点编码")
    @Column(name = "property_code")
    private String propertyCode;

    @ApiModelProperty("测点名称")
    @Column(name = "property_name")
    private String propertyName;

    @ApiModelProperty("第三方测点编码")
    @Column(name = "third_party_code")
    private String thirdPartyCode;

    @ApiModelProperty("数据类型")
    @Column(name = "data_type")
    private String dataType;

    @ApiModelProperty("数据类型名称")
    @Column(name = "data_type_name")
    private String dataTypeName;

    @ApiModelProperty("值类型")
    @Column(name = "value_type")
    private String valueType;

    @ApiModelProperty("单位")
    @Column(name = "unit")
    private String unit;

    @ApiModelProperty("采集频率，秒")
    @Column(name = "data_frequency")
    private Integer dataFrequency;

    @ApiModelProperty("是否核心测点")
    @Column(name = "required_flag")
    private Integer requiredFlag;

    @ApiModelProperty("读写角色")
    @Column(name = "read_write_role")
    private String readWriteRole;

    @ApiModelProperty("上报方式")
    @Column(name = "up_way")
    private String upWay;

    @ApiModelProperty("上报方式名称")
    @Column(name = "up_way_name")
    private String upWayName;

    @ApiModelProperty("上报周期")
    @Column(name = "up_period")
    private String upPeriod;

    @ApiModelProperty("上报周期名称")
    @Column(name = "up_period_name")
    private String upPeriodName;

    @ApiModelProperty("下限值")
    @Column(name = "value_lower_limit")
    private String valueLowerLimit;

    @ApiModelProperty("上限值")
    @Column(name = "value_high_limit")
    private String valueHighLimit;

    @ApiModelProperty("死区类型")
    @Column(name = "dead_zone_type")
    private Integer deadZoneType;

    @ApiModelProperty("类型")
    @Column(name = "type")
    private Integer type;

    @ApiModelProperty("状态：1启用，0停用")
    @Column(name = "status")
    private Integer status;

    @ApiModelProperty("删除标识：0正常，1删除")
    @Column(name = "deleted")
    private Integer deleted;

    @ApiModelProperty("排序")
    @Column(name = "sort")
    private Integer sort;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Transient
    @ApiModelProperty("读写权限名称")
    private String readWriteRoleName;

    public String getPointCode() {
        return propertyCode;
    }

    public void setPointCode(String pointCode) {
        this.propertyCode = pointCode;
    }

    public String getPointName() {
        return propertyName;
    }

    public void setPointName(String pointName) {
        this.propertyName = pointName;
    }
}
