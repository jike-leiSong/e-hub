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
@ApiModel("IoT设备组测点元数据")
@Table(name = "iot_device_group_point_metadata")
public class IotDeviceGroupPointMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty("设备组类型编码")
    @Column(name = "device_group_type")
    private String deviceGroupType;

    @ApiModelProperty("设备组类型名称")
    @Column(name = "device_group_type_name")
    private String deviceGroupTypeName;

    @ApiModelProperty("测点编码")
    @Column(name = "property_code")
    private String propertyCode;

    @ApiModelProperty("测点名称")
    @Column(name = "property_name")
    private String propertyName;

    @ApiModelProperty("测点名称英文")
    @Column(name = "property_name_en")
    private String propertyNameEn;

    @ApiModelProperty("数据类型")
    @Column(name = "data_type")
    private String dataType;

    @ApiModelProperty("数据类型名称")
    @Column(name = "data_type_name")
    private String dataTypeName;

    @ApiModelProperty("值类型")
    @Column(name = "value_type")
    private String valueType;

    @ApiModelProperty("读写角色")
    @Column(name = "read_write_role")
    private String readWriteRole;

    @ApiModelProperty("单位")
    @Column(name = "unit")
    private String unit;

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

    @ApiModelProperty("删除标识：0正常，1删除")
    @Column(name = "deleted")
    private Integer deleted;
}
