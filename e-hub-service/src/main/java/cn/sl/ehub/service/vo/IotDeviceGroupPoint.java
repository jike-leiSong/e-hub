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
@ApiModel("IoT设备组测点")
@Table(name = "iot_device_group_point")
public class IotDeviceGroupPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("租户ID")
    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "device_group_id")
    private Long deviceGroupId;

    @ApiModelProperty("测点编码")
    @Column(name = "property_code")
    private String propertyCode;

    @ApiModelProperty("测点名称")
    @Column(name = "property_name")
    private String propertyName;

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

    @ApiModelProperty("读写权限")
    @Column(name = "read_write_role")
    private String readWriteRole;

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

    @ApiModelProperty("排序")
    @Column(name = "sort")
    private Integer sort;

    @ApiModelProperty("状态")
    @Column(name = "status")
    private Integer status;

    @ApiModelProperty("删除标识")
    @Column(name = "deleted")
    private Integer deleted;

    @ApiModelProperty("备注")
    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

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
